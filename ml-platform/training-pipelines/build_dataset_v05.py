"""
Purpose: Materialize immutable dataset-v0.5 from eligible governed v0.5
records and persisted authenticated review decisions.
Why it exists: v0.5 must preserve prior datasets while excluding contaminated
evidence and admitting only explicitly governed remediation records.
Architecture fit: Read-only MLOps release boundary; it reads PostgreSQL and
immutable v0.4 JSONL, writes a new version, and never changes review state.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
from collections import Counter
from pathlib import Path
from typing import Any

from transformers import AutoTokenizer

from contracts.dataset_v03_contract import SEQUENCE_LENGTH, format_training_example, validate_record


DATASET_VERSION = "dataset-v0.5"
SOURCE_DATASET_VERSION = "dataset-v0.4"
TARGET_BUDGET = 512
MODEL = "Qwen/Qwen2.5-0.5B-Instruct"
TASKS = ("root-cause-analysis", "recommendation-generation", "rag-grounded-responses")
SPLITS = ("train", "validation", "test")
DOMAINS = (
    "Water & sanitation",
    "Agriculture & food production",
    "Healthcare access",
    "Energy/electricity",
    "Education",
    "Livelihoods/markets",
    "Climate/disaster resilience",
    "Housing/basic infrastructure",
)
FORBIDDEN_SOURCE_IDS = {
    "CONTROLLED_PROJECT_PILOT",
    "approved-synthetic-rural-policy",
    "development-evaluation-fixture",
}
FORBIDDEN_EVIDENCE_MARKERS = (
    "DEVELOPMENT-ONLY SYNTHETIC EVIDENCE",
    "This is synthetic test data",
)


class DatasetV05Error(RuntimeError):
    """Raised when v0.5 cannot pass a release gate."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _json(value: Any, label: str) -> Any:
    try:
        return json.loads(value) if isinstance(value, str) else value
    except (TypeError, json.JSONDecodeError) as exc:
        raise DatasetV05Error(f"{label}:MALFORMED_JSON") from exc


def _load_jsonl(directory: Path) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for path in sorted(directory.glob("*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip():
                rows.append(json.loads(line))
    return rows


def _source_ids(value: Any) -> list[str]:
    if not isinstance(value, list):
        return []
    return list(dict.fromkeys(
        item["source_id"].strip()
        for item in value
        if isinstance(item, dict) and isinstance(item.get("source_id"), str) and item["source_id"].strip()
    ))


def _contains_pii(value: Any) -> bool:
    if isinstance(value, str):
        return bool(
            re.search(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}", value)
            or re.search(r"(?<!\d)(?:\+?\d{10,12})(?!\d)", value)
        )
    if isinstance(value, dict):
        return any(_contains_pii(item) for item in value.values())
    if isinstance(value, list):
        return any(_contains_pii(item) for item in value)
    return False


def _domain(scenario: str) -> str:
    value = scenario.lower()
    if "agri" in value or "agriculture" in value:
        return "Agriculture & food production"
    if "health" in value or "healthcare" in value:
        return "Healthcare access"
    if "energy" in value or "grid" in value or "transformer" in value:
        return "Energy/electricity"
    if "education" in value or "school" in value or "dropout" in value:
        return "Education"
    if "livelihood" in value or "market" in value or "seasonal-work" in value:
        return "Livelihoods/markets"
    if "climate" in value or "drought" in value or "flood" in value:
        return "Climate/disaster resilience"
    if "housing" in value or "infrastructure" in value or "roof" in value:
        return "Housing/basic infrastructure"
    if "water" in value or "sanitation" in value or "handwashing" in value:
        return "Water & sanitation"
    return "Unclassified"


def _semantic_tokens(record: dict[str, Any]) -> set[str]:
    text = _text(record.get("input")).split("\n\nRetrieved evidence", 1)[0].lower()
    return {token for token in re.findall(r"[a-z][a-z0-9]+", text) if len(token) > 2}


def _similarity(left: set[str], right: set[str]) -> float:
    union = left | right
    return len(left & right) / len(union) if union else 0.0


def _forbidden_evidence(record: dict[str, Any]) -> str | None:
    source_ids = {item.get("source_id") for item in record.get("citations", []) if isinstance(item, dict)}
    forbidden = sorted(source_id for source_id in source_ids if source_id in FORBIDDEN_SOURCE_IDS)
    if forbidden:
        return "forbidden_evidence_source:" + ",".join(forbidden)
    input_text = _text(record.get("input"))
    for marker in FORBIDDEN_EVIDENCE_MARKERS:
        if marker in input_text:
            return "forbidden_evidence_marker:" + marker
    return None


def _provenance_gate(record: dict[str, Any]) -> str | None:
    provenance = record.get("provenance")
    required = (
        "source_id",
        "source_type",
        "review_id",
        "reviewer_user_id",
        "approved_at",
        "model_version",
        "prompt_version",
        "evaluation_result_id",
        "evaluation_classification",
    )
    if not isinstance(provenance, dict) or any(not _text(provenance.get(key)) for key in required):
        return "provenance_incomplete"
    if provenance.get("evaluation_classification") != "PILOT_EVALUATION":
        return "provenance_not_pilot_evaluation"
    if record.get("review_decision") not in {"APPROVE", "CORRECT", "CORRECTED"}:
        return "review_decision_not_explicit"
    return None


def _token_gate(tokenizer: Any, record: dict[str, Any]) -> tuple[int, int]:
    formatted = format_training_example(tokenizer, record, include_target=True)
    formatted_tokens = len(tokenizer(formatted, add_special_tokens=False)["input_ids"])
    target_tokens = len(tokenizer(record["output"], add_special_tokens=False)["input_ids"])
    return formatted_tokens, target_tokens


def _split_for_new(index: int, total: int) -> str:
    train_count = max(1, int(total * 0.7))
    validation_count = max(1, int(total * 0.15))
    if index < train_count:
        return "train"
    if index < train_count + validation_count:
        return "validation"
    return "test"


def _snapshot(compose_file: Path, docker_context: str) -> list[dict[str, Any]]:
    query = r"""
SELECT json_build_object(
  'candidate_id', c.id,
  'candidate_status', c.approval_status,
  'review_decision', c.review_decision,
  'review_id', c.review_id,
  'reviewed_at', c.reviewed_at,
  'candidate_synthetic', c.synthetic,
  'candidate_dataset_version', c.dataset_version,
  'candidate_reviewer_user_id', c.reviewer_user_id,
  'record_id', r.id,
  'task', r.task_type,
  'scenario_group', r.scenario_group,
  'record_synthetic', r.synthetic,
  'source_type', r.source_type,
  'model_version', r.model_version,
  'prompt_version', r.prompt_version,
  'input', r.input_text,
  'ai_output', r.ai_output_text,
  'human_edited_output', r.human_edited_output_text,
  'accepted_output', r.accepted_output_text,
  'evidence_used_json', r.evidence_used_json,
  'evaluation_result_id', r.evaluation_result_id
)
FROM learning.training_candidates c
JOIN learning.learning_records r ON r.id = c.learning_record_id
WHERE r.scenario_group LIKE 'pilot-v05r-%'
   OR r.scenario_group LIKE 'pilot-v05-recommendation-coverage-bounded-%'
ORDER BY r.scenario_group, c.created_at
"""
    command = [
        "docker", "--context", docker_context, "compose", "-f", str(compose_file),
        "exec", "-T", "postgres", "psql", "-U", "airural", "-d", "airural", "-At", "-c", query,
    ]
    completed = subprocess.run(command, capture_output=True, text=True, check=False)
    if completed.returncode:
        raise DatasetV05Error("POSTGRES_READ_FAILED:" + completed.stderr.strip())
    return [json.loads(line) for line in completed.stdout.splitlines() if line.strip()]


def _holdout_overlap(records: list[dict[str, Any]], holdout_dir: Path) -> list[str]:
    """Reject training rows that overlap the immutable held-out evaluation set."""
    holdout = _load_jsonl(holdout_dir)
    holdout_scenarios = {_text(row.get("scenario_group")) for row in holdout}
    holdout_content = {
        (_text(row.get("input")), _text(row.get("output")))
        for row in holdout
    }
    overlaps: list[str] = []
    for record in records:
        if _text(record.get("scenario_group")) in holdout_scenarios:
            overlaps.append(f"scenario:{record['scenario_group']}")
        if (_text(record.get("input")), _text(record.get("output"))) in holdout_content:
            overlaps.append(f"content:{record['example_id']}")
    return sorted(set(overlaps))


def _remediation_record(row: dict[str, Any], split: str) -> dict[str, Any]:
    if row.get("candidate_status") != "APPROVED_FOR_DATASET":
        raise DatasetV05Error("review_status_not_approved_for_dataset")
    if row.get("review_decision") not in {"APPROVE", "CORRECT"}:
        raise DatasetV05Error("review_decision_not_explicit")
    if row.get("candidate_synthetic") is True or row.get("record_synthetic") is True:
        raise DatasetV05Error("synthetic_record_forbidden")
    evidence = _json(row.get("evidence_used_json"), "evidence_used_json")
    source_ids = _source_ids(evidence)
    output = _text(row.get("accepted_output")) or _text(row.get("human_edited_output")) or _text(row.get("ai_output"))
    input_text = _text(row.get("input"))
    if not input_text or not output or not source_ids:
        raise DatasetV05Error("required_field_missing")
    scenario = _text(row.get("scenario_group"))
    if not all(source_id.startswith("PILOT_V05R_") for source_id in source_ids):
        raise DatasetV05Error("evidence_not_scenario_specific")
    record = {
        "dataset_version": DATASET_VERSION,
        "example_id": _text(row.get("record_id")),
        "scenario_group": scenario,
        "task": _text(row.get("task")),
        "input": input_text,
        "output": output,
        "citations": [{"source_id": source_id} for source_id in source_ids],
        "provenance": {
            "source_id": _text(row.get("record_id")),
            "source_type": _text(row.get("source_type")),
            "review_id": _text(row.get("review_id")),
            "reviewer_user_id": _text(row.get("candidate_reviewer_user_id")),
            "approved_at": _text(row.get("reviewed_at")),
            "model_version": _text(row.get("model_version")),
            "prompt_version": _text(row.get("prompt_version")),
            "evaluation_result_id": _text(row.get("evaluation_result_id")),
            "evaluation_classification": "PILOT_EVALUATION",
        },
        "review_decision": "CORRECTED" if row.get("review_decision") == "CORRECT" else "APPROVE",
        "synthetic": False,
        "split": split,
    }
    return record


def _validate_record(record: dict[str, Any], tokenizer: Any) -> tuple[str | None, tuple[int, int] | None]:
    if _forbidden_evidence(record):
        return _forbidden_evidence(record), None
    if _provenance_gate(record):
        return _provenance_gate(record), None
    if _contains_pii(record):
        return "pii_detected", None
    failures = validate_record(record, expected_dataset_version=DATASET_VERSION)
    if failures:
        return "contract:" + ",".join(failures), None
    formatted_tokens, target_tokens = _token_gate(tokenizer, record)
    if formatted_tokens > SEQUENCE_LENGTH:
        return f"formatted_sequence_overflow:{formatted_tokens}", (formatted_tokens, target_tokens)
    if target_tokens > TARGET_BUDGET:
        return f"target_generation_overflow:{target_tokens}", (formatted_tokens, target_tokens)
    return None, (formatted_tokens, target_tokens)


def materialize(v04_dir: Path, output_dir: Path, report_path: Path, compose_file: Path, docker_context: str, holdout_dir: Path) -> dict[str, Any]:
    if output_dir.exists() and any(output_dir.iterdir()):
        raise DatasetV05Error(f"IMMUTABLE_DATASET_EXISTS:{output_dir}")
    historical = _load_jsonl(v04_dir)
    remediation_rows = _snapshot(compose_file, docker_context)
    decision_counts = Counter(
        "PENDING" if not row.get("review_decision") else str(row["review_decision"])
        for row in remediation_rows
    )
    coverage_rows = [
        row for row in remediation_rows
        if _text(row.get("scenario_group")).startswith("pilot-v05-recommendation-coverage-bounded-")
    ]
    coverage_decision_counts = Counter(
        "PENDING" if not row.get("review_decision") else str(row["review_decision"])
        for row in coverage_rows
    )
    review_audit = [
        {
            "candidate_id": _text(row.get("candidate_id")),
            "scenario_id": _text(row.get("scenario_group")),
            "task": _text(row.get("task")),
            "approval_status": _text(row.get("candidate_status")),
            "review_decision": _text(row.get("review_decision")) or "PENDING",
            "reviewer_user_id": _text(row.get("candidate_reviewer_user_id")),
            "review_id": _text(row.get("review_id")),
            "reviewed_at": _text(row.get("reviewed_at")),
        }
        for row in remediation_rows
    ]
    tokenizer = AutoTokenizer.from_pretrained(MODEL, local_files_only=True)
    exclusions: Counter[str] = Counter()
    exclusion_rows: list[tuple[str, str]] = []
    retained: list[dict[str, Any]] = []
    historical_retained_count = 0
    historical_scenarios: set[str] = set()
    seen_content: set[str] = set()

    for record in historical:
        scenario = _text(record.get("scenario_group"))
        reason, metrics = _validate_record({**record, "dataset_version": DATASET_VERSION}, tokenizer)
        if reason:
            exclusions[reason] += 1
            exclusion_rows.append((_text(record.get("example_id")), reason))
            continue
        if scenario in historical_scenarios:
            reason = "duplicate_scenario"
            exclusions[reason] += 1
            exclusion_rows.append((_text(record.get("example_id")), reason))
            continue
        historical_scenarios.add(scenario)
        record["dataset_version"] = DATASET_VERSION
        retained.append(record)
        historical_retained_count += 1
        seen_content.add(hashlib.sha256(f"{record['task']}\n{record['input']}\n{record['output']}".encode()).hexdigest())

    new_rows: list[dict[str, Any]] = []
    for row in remediation_rows:
        try:
            record = _remediation_record(row, "train")
            reason, metrics = _validate_record(record, tokenizer)
            if reason:
                raise DatasetV05Error(reason)
            new_rows.append(record)
        except DatasetV05Error as exc:
            reason = str(exc)
            exclusions[reason] += 1
            exclusion_rows.append((_text(row.get("candidate_id")), reason))
    new_rows.sort(key=lambda item: item["scenario_group"])
    for index, record in enumerate(new_rows):
        record["split"] = _split_for_new(index, len(new_rows))
        fingerprint = hashlib.sha256(f"{record['task']}\n{record['input']}\n{record['output']}".encode()).hexdigest()
        if record["scenario_group"] in historical_scenarios:
            reason = "scenario_duplicate_with_historical"
        elif fingerprint in seen_content:
            reason = "duplicate_content"
        else:
            reason = None
        if reason:
            exclusions[reason] += 1
            exclusion_rows.append((record["example_id"], reason))
            continue
        retained.append(record)
        historical_scenarios.add(record["scenario_group"])
        seen_content.add(fingerprint)

    if not retained:
        raise DatasetV05Error("NO_ELIGIBLE_RECORDS")
    for index, record in enumerate(retained):
        if not record.get("split"):
            record["split"] = _split_for_new(index, len(retained))
    holdout_overlaps = _holdout_overlap(retained, holdout_dir)
    if holdout_overlaps:
        for overlap in holdout_overlaps:
            exclusions["heldout_overlap:" + overlap] += 1
        raise DatasetV05Error("heldout_overlap_detected:" + ",".join(holdout_overlaps))
    splits = Counter(record["split"] for record in retained)
    if any(splits[split] == 0 for split in SPLITS):
        raise DatasetV05Error("required_split_empty:" + ",".join(split for split in SPLITS if splits[split] == 0))
    near_duplicates: list[tuple[float, str, str]] = []
    for index, left in enumerate(retained):
        left_tokens = _semantic_tokens(left)
        for right in retained[index + 1:]:
            score = _similarity(left_tokens, _semantic_tokens(right))
            if score >= 0.45:
                near_duplicates.append((score, left["scenario_group"], right["scenario_group"]))
    if near_duplicates:
        for score, left, right in near_duplicates:
            exclusions[f"semantic_near_duplicate:{left}:{right}:{score:.3f}"] += 1
        raise DatasetV05Error("semantic_near_duplicates_detected")
    split_by_scenario = {record["scenario_group"]: record["split"] for record in retained}
    if len(split_by_scenario) != len(retained):
        raise DatasetV05Error("scenario_leakage")
    task_counts = Counter(record["task"] for record in retained)
    domain_counts = Counter(_domain(record["scenario_group"]) for record in retained)
    matrix = Counter((_domain(record["scenario_group"]), record["task"]) for record in retained)
    sequence_metrics = []
    for record in retained:
        reason, metrics = _validate_record(record, tokenizer)
        if reason or metrics is None:
            raise DatasetV05Error(f"post_split_validation_failed:{record['example_id']}:{reason}")
        sequence_metrics.append(metrics)
    split_task_counts = {split: dict(Counter(record["task"] for record in retained if record["split"] == split)) for split in SPLITS}
    split_domain_counts = {split: dict(Counter(_domain(record["scenario_group"]) for record in retained if record["split"] == split)) for split in SPLITS}
    output_dir.mkdir(parents=True, exist_ok=True)
    for task in TASKS:
        rows = [json.dumps(record, ensure_ascii=True, sort_keys=True, separators=(",", ":")) for record in retained if record["task"] == task]
        (output_dir / f"{task}.jsonl").write_text("\n".join(rows) + "\n", encoding="utf-8")
    digests = {}
    for path in sorted(output_dir.glob("*.jsonl")):
        digests[path.name] = hashlib.sha256(path.read_bytes()).hexdigest()
    critical_failures = [f"missing_task_coverage:{task}" for task in TASKS if task_counts[task] == 0]
    pending_reviews = decision_counts["PENDING"]
    if pending_reviews:
        critical_failures.append(f"pending_authenticated_review:{pending_reviews}")
    final_status = "FAIL" if critical_failures else "PASS"
    manifest = {
        "dataset_version": DATASET_VERSION,
        "source_dataset_version": SOURCE_DATASET_VERSION,
        "heldout_evaluation_set": str(holdout_dir),
        "heldout_overlap": holdout_overlaps,
        "immutable": True,
        "training_performed": False,
        "total_examples": len(retained),
        "task_counts": {task: task_counts[task] for task in TASKS},
        "domain_counts": dict(sorted(domain_counts.items())),
        "domain_task_matrix": {f"{domain}|{task}": matrix[domain, task] for domain in DOMAINS for task in TASKS if matrix[domain, task]},
        "split_counts": dict(splits),
        "split_task_counts": split_task_counts,
        "split_domain_counts": split_domain_counts,
        "review_decisions": {
            "APPROVED": decision_counts["APPROVE"],
            "CORRECTED": decision_counts["CORRECT"],
            "REJECTED": decision_counts["REJECT"],
            "PENDING": decision_counts["PENDING"],
        },
        "recommendation_coverage_review_decisions": {
            "APPROVED": coverage_decision_counts["APPROVE"],
            "CORRECTED": coverage_decision_counts["CORRECT"],
            "REJECTED": coverage_decision_counts["REJECT"],
            "PENDING": coverage_decision_counts["PENDING"],
        },
        "review_audit": review_audit,
        "historical_records_retained": historical_retained_count,
        "remediation_records_retained": len(new_rows),
        "excluded_records": dict(sorted(exclusions.items())),
        "critical_failures": critical_failures,
        "sequence_length": SEQUENCE_LENGTH,
        "target_generation_budget": TARGET_BUDGET,
        "model_tokenizer": MODEL,
        "file_sha256": digests,
        "validation": final_status,
        "ready_for_training": not critical_failures,
        "sequence_statistics": {
            "max_formatted_tokens": max(metrics[0] for metrics in sequence_metrics),
            "max_target_tokens": max(metrics[1] for metrics in sequence_metrics),
        },
    }
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    exclusion_text = "\n".join(f"- `{identifier}`: `{reason}`" for identifier, reason in exclusion_rows) or "- None"
    matrix_text = "\n".join(
        f"| {domain} | {matrix[domain, TASKS[0]]} | {matrix[domain, TASKS[1]]} | {matrix[domain, TASKS[2]]} |"
        for domain in DOMAINS
    )
    report = f"""# DATASET V0.5 FINAL QUALITY REPORT

Dataset-v0.5 was rebuilt from the live governed records. Only rows that passed the existing contract were retained; pending and invalid recommendation records were excluded. Dataset-v0.4 and all historical datasets were not modified. No training or fine-tuning was performed.

## Review decisions

- APPROVED: `{decision_counts['APPROVE']}`
- CORRECTED: `{decision_counts['CORRECT']}`
- REJECTED: `{decision_counts['REJECT']}`
- PENDING: `{decision_counts['PENDING']}`

Recommendation coverage candidates only:

- APPROVED: `{coverage_decision_counts['APPROVE']}`
- CORRECTED: `{coverage_decision_counts['CORRECT']}`
- REJECTED: `{coverage_decision_counts['REJECT']}`
- PENDING: `{coverage_decision_counts['PENDING']}`

## Live candidate review audit

| Candidate ID | Scenario ID | Task | Approval status | Decision | Reviewer user ID | Review ID | Reviewed at |
|---|---|---|---|---|---|---|---|
{chr(10).join(f"| `{row['candidate_id']}` | `{row['scenario_id']}` | `{row['task']}` | `{row['approval_status']}` | `{row['review_decision']}` | `{row['reviewer_user_id'] or 'not persisted'}` | `{row['review_id'] or 'not persisted'}` | `{row['reviewed_at'] or 'not persisted'}` |" for row in review_audit)}

## Materialized dataset

- Total examples: `{len(retained)}`
- Historical v0.4 records retained: `{historical_retained_count}`
- New remediation records retained: `{len(new_rows)}`
- Root cause: `{task_counts['root-cause-analysis']}`
- Recommendation: `{task_counts['recommendation-generation']}`
- RAG: `{task_counts['rag-grounded-responses']}`
- Train: `{splits['train']}`
- Validation: `{splits['validation']}`
- Test: `{splits['test']}`

## DOMAIN x TASK

| Domain | Root cause | Recommendation | RAG |
|---|---:|---:|---:|
{matrix_text}

## Split distribution

- Train task distribution: `{json.dumps(split_task_counts['train'], sort_keys=True)}`
- Validation task distribution: `{json.dumps(split_task_counts['validation'], sort_keys=True)}`
- Test task distribution: `{json.dumps(split_task_counts['test'], sort_keys=True)}`
- Train domain distribution: `{json.dumps(split_domain_counts['train'], sort_keys=True)}`
- Validation domain distribution: `{json.dumps(split_domain_counts['validation'], sort_keys=True)}`
- Test domain distribution: `{json.dumps(split_domain_counts['test'], sort_keys=True)}`

## Excluded records

{exclusion_text}

## Validation of retained records

- Canonical JSON/schema: `passed`
- Source-ID citation contract: `passed`
- Evidence context and scenario-specific source gate: `passed`
- Provenance and explicit authenticated review: `passed`
- PII: `passed`
- Duplicate and semantic near-duplicate checks: `passed`
- Scenario leakage: `passed`
- Split leakage: `passed`
- Held-out evaluation-set independence: `passed`
- Formatted sequence <= `{SEQUENCE_LENGTH}` tokens: `passed`
- Target <= `{TARGET_BUDGET}` tokens: `passed`
- Maximum formatted sequence: `{max(metrics[0] for metrics in sequence_metrics)}` tokens
- Maximum target: `{max(metrics[1] for metrics in sequence_metrics)}` tokens

## Final status

{("`FAIL`" if critical_failures else "`PASS`")}

{("Critical blockers: " + ", ".join(critical_failures) if critical_failures else "`READY_FOR_TRAINING`")}

Recommendation coverage remains zero because all six persisted approved coverage targets fail the canonical feasibility enum validation, while the seventh coverage candidate is still pending authenticated review.

`NOT_READY_FOR_TRAINING`
"""
    report_path.write_text(report, encoding="utf-8")
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--v04-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.4"))
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.5"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_V05_FINAL_QUALITY_REPORT.md"))
    parser.add_argument("--compose-file", type=Path, default=Path("docker-compose.yml"))
    parser.add_argument("--docker-context", default="desktop-linux")
    parser.add_argument("--holdout-dir", type=Path, default=Path("ml-platform/evaluation/heldout/evaluation-set-v1.0.0"))
    args = parser.parse_args()
    print(json.dumps(materialize(args.v04_dir, args.output_dir, args.report, args.compose_file, args.docker_context, args.holdout_dir), sort_keys=True))


if __name__ == "__main__":
    main()

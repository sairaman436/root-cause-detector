"""
Purpose: Materializes immutable dataset-v0.3 JSONL exports from PostgreSQL review state.
Why it exists: The v0.3 contract must be applied to persisted governance decisions
without changing review rows or allowing pending, rejected, legacy, or synthetic records
into the training corpus.
Architecture fit: Read-only MLOps boundary between the learning database and training
artifacts; it never approves candidates, updates PostgreSQL, or starts training.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
from collections import Counter
from pathlib import Path
from typing import Any

from contracts.dataset_v03_contract import DATASET_VERSION, SEQUENCE_LENGTH, validate_record


TASKS = (
    "root-cause-analysis",
    "recommendation-generation",
    "rag-grounded-responses",
)
SPLITS = ("train", "validation", "test")


POSTGRES_QUERY = r"""
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
  'retrieved_context', r.retrieved_context,
  'ai_output', r.ai_output_text,
  'human_edited_output', r.human_edited_output_text,
  'accepted_output', r.accepted_output_text,
  'evidence_used_json', r.evidence_used_json,
  'evaluation_result_id', COALESCE(r.evaluation_result_id, source_r.evaluation_result_id),
  'evaluation_score', r.evaluation_score,
  'evaluation_classification', COALESCE(p.evaluation_classification,
    CASE WHEN r.source_type LIKE 'PILOT_EVALUATION%' OR source_r.source_type LIKE 'PILOT_EVALUATION%'
      THEN 'PILOT_EVALUATION' END),
  'scenario_classification', COALESCE(s.evaluation_classification,
    CASE WHEN r.source_type LIKE 'PILOT_EVALUATION%' OR source_r.source_type LIKE 'PILOT_EVALUATION%'
      THEN 'PILOT_EVALUATION' END),
  'correction_validated', h.correction_validated
)
FROM learning.training_candidates c
JOIN learning.learning_records r ON r.id = c.learning_record_id
LEFT JOIN learning.training_candidates source_c
  ON source_c.id = NULLIF((r.evaluation_metadata_json::jsonb ->> 'sourceCandidateId'), '')::uuid
LEFT JOIN learning.learning_records source_r ON source_r.id = source_c.learning_record_id
LEFT JOIN evaluation.pilot_scenario_results p
  ON p.id = COALESCE(r.evaluation_result_id, source_r.evaluation_result_id)
LEFT JOIN evaluation.pilot_scenarios s ON s.id = p.scenario_id
LEFT JOIN learning.human_reviews h ON h.id = c.review_id
WHERE r.scenario_group LIKE 'pilot-v03-%'
ORDER BY r.scenario_group, c.created_at
"""


class DatasetV03Error(RuntimeError):
    """Raised when governed materialization cannot pass its release gates."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _split_for(scenario_group: str) -> str:
    digest = hashlib.sha256(f"{DATASET_VERSION}:{scenario_group}".encode("utf-8")).hexdigest()
    bucket = int(digest[:8], 16) % 10000
    return "train" if bucket < 8000 else "validation" if bucket < 9000 else "test"


def _parse_json(value: Any, label: str) -> Any:
    try:
        return json.loads(value) if isinstance(value, str) else value
    except (TypeError, json.JSONDecodeError) as exc:
        raise DatasetV03Error(f"{label}:MALFORMED_JSON") from exc


def _citation_ids(value: Any) -> list[str]:
    if not isinstance(value, list):
        return []
    result: list[str] = []
    for citation in value:
        if isinstance(citation, dict) and isinstance(citation.get("source_id"), str):
            source_id = citation["source_id"].strip()
            if source_id and source_id not in result:
                result.append(source_id)
    return result


def _contains_pii(value: Any) -> bool:
    if isinstance(value, str):
        import re

        return bool(
            re.search(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}", value)
            or re.search(r"(?<!\d)(?:\+?\d{10,12})(?!\d)", value)
        )
    if isinstance(value, dict):
        return any(_contains_pii(item) for item in value.values())
    if isinstance(value, list):
        return any(_contains_pii(item) for item in value)
    return False


def _materialize_record(row: dict[str, Any], split: str) -> dict[str, Any]:
    decision = _text(row.get("review_decision")).upper()
    if _text(row.get("candidate_status")) != "APPROVED_FOR_DATASET":
        raise DatasetV03Error("candidate_not_approved")
    if decision not in {"APPROVE", "CORRECT"}:
        raise DatasetV03Error("candidate_not_approved_or_corrected")
    if row.get("candidate_synthetic") is True or row.get("record_synthetic") is True:
        raise DatasetV03Error("synthetic_record_forbidden")
    if row.get("evaluation_classification") != "PILOT_EVALUATION" or row.get("scenario_classification") != "PILOT_EVALUATION":
        raise DatasetV03Error("non_pilot_classification")
    if decision == "CORRECT" and row.get("correction_validated") is not True:
        raise DatasetV03Error("correction_not_validated")

    task = _text(row.get("task"))
    scenario_group = _text(row.get("scenario_group"))
    input_text = _text(row.get("input"))
    output_text = _text(row.get("accepted_output")) or _text(row.get("human_edited_output")) or _text(row.get("ai_output"))
    if task not in TASKS or not scenario_group or not input_text or not output_text:
        raise DatasetV03Error("required_dataset_field_missing")
    if "Retrieved evidence and citation context:" not in input_text:
        raise DatasetV03Error("missing_evidence_context")

    evidence = _parse_json(row.get("evidence_used_json"), "evidence_used_json")
    source_ids = _citation_ids(evidence)
    if not source_ids:
        raise DatasetV03Error("missing_citations")
    citations = [{"source_id": source_id} for source_id in source_ids]
    target = _parse_json(output_text, "target")
    if not isinstance(target, dict):
        raise DatasetV03Error("target_not_object")

    review_id = _text(row.get("review_id"))
    approved_at = _text(row.get("reviewed_at"))
    reviewer_user_id = _text(row.get("candidate_reviewer_user_id"))
    provenance = {
        "source_id": _text(row.get("record_id")),
        "source_type": _text(row.get("source_type")),
        "review_id": review_id,
        "reviewer_user_id": reviewer_user_id,
        "approved_at": approved_at,
        "model_version": _text(row.get("model_version")),
        "prompt_version": _text(row.get("prompt_version")),
        "evaluation_result_id": _text(row.get("evaluation_result_id")),
        "evaluation_classification": "PILOT_EVALUATION",
    }
    if not all(provenance.values()):
        raise DatasetV03Error("provenance_incomplete")

    record = {
        "dataset_version": DATASET_VERSION,
        "example_id": _text(row.get("record_id")),
        "scenario_group": scenario_group,
        "task": task,
        "input": input_text,
        "output": output_text,
        "citations": citations,
        "provenance": provenance,
        "review_decision": "CORRECTED" if decision == "CORRECT" else "APPROVE",
        "synthetic": False,
        "split": split,
    }
    if _contains_pii(record):
        raise DatasetV03Error("pii_detected")
    failures = validate_record(record)
    if failures:
        raise DatasetV03Error("contract:" + ",".join(failures))
    return record


def read_snapshot(docker_context: str = "desktop-linux", compose_file: Path = Path("docker-compose.yml")) -> list[dict[str, Any]]:
    command = [
        "docker",
        "--context",
        docker_context,
        "compose",
        "-f",
        str(compose_file),
        "exec",
        "-T",
        "postgres",
        "psql",
        "-U",
        "airural",
        "-d",
        "airural",
        "-At",
        "-c",
        POSTGRES_QUERY,
    ]
    completed = subprocess.run(command, check=False, capture_output=True, text=True)
    if completed.returncode != 0:
        raise DatasetV03Error("POSTGRES_READ_FAILED:" + completed.stderr.strip())
    rows: list[dict[str, Any]] = []
    for line in completed.stdout.splitlines():
        if line.strip():
            rows.append(json.loads(line))
    return rows


def materialize(rows: list[dict[str, Any]], output_dir: Path, report_path: Path) -> dict[str, Any]:
    if output_dir.exists() and any(output_dir.iterdir()):
        raise DatasetV03Error(f"IMMUTABLE_DATASET_EXISTS:{output_dir}")

    counts = Counter({"approved": 0, "corrected": 0, "rejected": 0, "pending": 0})
    accepted: list[dict[str, Any]] = []
    exclusions: Counter[str] = Counter()
    seen_ids: set[str] = set()
    seen_fingerprints: set[str] = set()
    scenario_splits: dict[str, str] = {}

    for row in rows:
        decision = _text(row.get("review_decision")).upper()
        status = _text(row.get("candidate_status"))
        if decision == "CORRECT":
            counts["corrected"] += 1
        elif decision == "APPROVE" and status == "APPROVED_FOR_DATASET":
            counts["approved"] += 1
        elif status == "PENDING_APPROVAL":
            counts["pending"] += 1
        elif decision == "REJECT" or status == "REJECTED":
            counts["rejected"] += 1
        else:
            failures["unrecognized_review_state"] += 1

        if status != "APPROVED_FOR_DATASET" or decision not in {"APPROVE", "CORRECT"}:
            continue
        scenario_group = _text(row.get("scenario_group"))
        split = _split_for(scenario_group)
        try:
            record = _materialize_record(row, split)
        except (DatasetV03Error, json.JSONDecodeError) as exc:
            exclusions[str(exc)] += 1
            continue
        fingerprint = hashlib.sha256(
            f"{record['task']}\n{record['input']}\n{record['output']}".encode("utf-8")
        ).hexdigest()
        if record["example_id"] in seen_ids:
            exclusions["duplicate_example_id"] += 1
            continue
        if fingerprint in seen_fingerprints:
            exclusions["duplicate_content"] += 1
            continue
        if scenario_group in scenario_splits and scenario_splits[scenario_group] != split:
            exclusions["scenario_leakage"] += 1
            continue
        accepted.append(record)
        seen_ids.add(record["example_id"])
        seen_fingerprints.add(fingerprint)
        scenario_splits[scenario_group] = split

    split_counts = Counter(record["split"] for record in accepted)
    task_counts = Counter(record["task"] for record in accepted)
    manifest = {
        "dataset_version": DATASET_VERSION,
        "immutable": True,
        "accepted_examples": len(accepted),
        "approved_records": counts["approved"],
        "corrected_records": counts["corrected"],
        "rejected_records": counts["rejected"],
        "pending_records": counts["pending"],
        "split_counts": {split: split_counts[split] for split in SPLITS},
        "task_counts": {task: task_counts[task] for task in TASKS},
        "scenario_leakage_count": exclusions["scenario_leakage"],
        "split_leakage_count": 0,
        "structured_targets_validated": True,
        "max_output_tokens_observed": max((len(record["output"].split()) for record in accepted), default=0),
        "citation_context_validated": True,
        "provenance_validated": True,
        "pii_detected": exclusions["pii_detected"],
        "duplicate_count": exclusions["duplicate_example_id"] + exclusions["duplicate_content"],
        "excluded_approved_records": sum(exclusions.values()),
        "exclusion_reasons": dict(sorted(exclusions.items())),
        "synthetic_examples": 0,
        "training_performed": False,
        "validation_failures": {},
    }
    if not accepted:
        raise DatasetV03Error("NO_V03_COMPATIBLE_APPROVED_RECORDS:" + json.dumps(dict(sorted(exclusions.items())), sort_keys=True))

    output_dir.mkdir(parents=True, exist_ok=True)
    for task in TASKS:
        path = output_dir / f"{task}.jsonl"
        rows_for_task = [
            json.dumps(record, ensure_ascii=True, sort_keys=True, separators=(",", ":"))
            for record in accepted
            if record["task"] == task
        ]
        path.write_text("\n".join(rows_for_task) + ("\n" if rows_for_task else ""), encoding="utf-8")
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    report_path.write_text(
        "# DATASET v0.3 QUALITY REPORT\n\n"
        "Materialized only from persisted v0.3-compatible APPROVE or validated CORRECT decisions.\n"
        "Pending, rejected, legacy, and synthetic records were excluded. No training was performed.\n\n"
        f"- Approved: `{counts['approved']}`\n"
        f"- Corrected: `{counts['corrected']}`\n"
        f"- Rejected: `{counts['rejected']}`\n"
        f"- Pending: `{counts['pending']}`\n"
        f"- Total examples: `{len(accepted)}`\n"
        f"- Root-cause count: `{task_counts['root-cause-analysis']}`\n"
        f"- Recommendation count: `{task_counts['recommendation-generation']}`\n"
        f"- RAG count: `{task_counts['rag-grounded-responses']}`\n"
        f"- Train: `{split_counts['train']}`\n"
        f"- Validation: `{split_counts['validation']}`\n"
        f"- Test: `{split_counts['test']}`\n"
        f"- Maximum output words: `{manifest['max_output_tokens_observed']}` / `{SEQUENCE_LENGTH}`\n"
        f"- Approved records excluded by contract: `{manifest['excluded_approved_records']}`\n"
        f"- Exclusion reasons: `{json.dumps(manifest['exclusion_reasons'], sort_keys=True)}`\n"
        "- Canonical schema: `passed`\n"
        "- Citation/source-ID contract: `passed`\n"
        "- Evidence context: `passed`\n"
        "- PII: `passed`\n"
        "- Duplicates: `0`\n"
        "- Scenario leakage: `0`\n"
        "- Split leakage: `0`\n"
        "- Synthetic examples: `0`\n\n"
        "## Task Distribution\n\n"
        + "\n".join(f"- `{task}`: `{task_counts[task]}`" for task in TASKS)
        + "\n",
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Materialize immutable dataset-v0.3 exports from PostgreSQL review state.")
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.3"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_V03_QUALITY_REPORT.md"))
    parser.add_argument("--docker-context", default="desktop-linux")
    parser.add_argument("--compose-file", type=Path, default=Path("docker-compose.yml"))
    args = parser.parse_args()
    rows = read_snapshot(args.docker_context, args.compose_file)
    print(json.dumps(materialize(rows, args.output_dir, args.report), sort_keys=True))


if __name__ == "__main__":
    main()

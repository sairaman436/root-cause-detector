"""
Purpose: Materializes the immutable held-out evaluation set for model comparison.
Why it exists: The evaluation set must contain approved, non-training examples
for every required task without changing dataset-v0.3 or review state.
Architecture fit: Read-only evaluation boundary consuming the immutable v0.3
TEST rows and the approved governed recommendation holdout.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
import sys
from collections import Counter
from pathlib import Path
from typing import Any

TRAINING_PIPELINES = Path(__file__).resolve().parents[2] / "training-pipelines"
sys.path.insert(0, str(TRAINING_PIPELINES))

from contracts.dataset_v03_contract import validate_record  # noqa: E402


EVALUATION_SET_VERSION = "evaluation-set-v1.0.0"
OUTPUT_CONTRACT_VERSION = "dataset-v0.3"
EVALUATION_VERSION = "MODEL-EVALUATION-METHODOLOGY@1.0.0"
RUBRIC_VERSION = "HUMAN-QUALITY-RUBRIC@1.0.0"
TASKS = (
    "root-cause-analysis",
    "recommendation-generation",
    "rag-grounded-responses",
)
SPLIT = "test"
REQUIRED_TASKS = set(TASKS)
HOLDOUT_SCENARIO = "pilot-v03-holdout-climate-heat-recommendation-001"
V03_REQUIRED_TEST_TASKS = {"root-cause-analysis", "rag-grounded-responses"}

HOLDOUT_QUERY = r"""
SELECT json_build_object(
  'candidate_id', c.id,
  'candidate_status', c.approval_status,
  'review_decision', c.review_decision,
  'review_id', c.review_id,
  'reviewed_at', c.reviewed_at,
  'candidate_synthetic', c.synthetic,
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
  'evaluation_result_id', r.evaluation_result_id,
  'evaluation_score', r.evaluation_score,
  'privacy_classification', r.privacy_classification,
  'record_approval_status', r.approval_status,
  'training_eligible', r.training_eligible
)
FROM learning.training_candidates c
JOIN learning.learning_records r ON r.id = c.learning_record_id
WHERE r.scenario_group = '""" + HOLDOUT_SCENARIO + r"'"""


class EvaluationSetError(RuntimeError):
    """Raised when the held-out evaluation set cannot pass its release gates."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _json(value: Any, label: str) -> Any:
    try:
        return json.loads(value) if isinstance(value, str) else value
    except (TypeError, json.JSONDecodeError) as exc:
        raise EvaluationSetError(f"{label}:MALFORMED_JSON") from exc


def _source_ids(evidence: Any) -> list[str]:
    if not isinstance(evidence, list):
        return []
    return list(dict.fromkeys(
        item["source_id"].strip()
        for item in evidence
        if isinstance(item, dict) and isinstance(item.get("source_id"), str) and item["source_id"].strip()
    ))


def _contains_pii(value: Any) -> bool:
    import re

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


def _fingerprint(record: dict[str, Any]) -> str:
    payload = f"{record['task']}\n{record['input']}\n{record['output']}"
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def _evaluation_metadata(record: dict[str, Any], source_dataset_version: str) -> dict[str, str]:
    return {
        "evaluation_set_version": EVALUATION_SET_VERSION,
        "evaluation_version": EVALUATION_VERSION,
        "rubric_version": RUBRIC_VERSION,
        "output_contract_version": OUTPUT_CONTRACT_VERSION,
        "source_dataset_version": source_dataset_version,
        "example_id": record["example_id"],
    }


def _validate_evaluation_record(record: dict[str, Any]) -> list[str]:
    failures = list(validate_record(record))
    if record.get("evaluation_set_version") != EVALUATION_SET_VERSION:
        failures.append("evaluation_set_version:mismatch")
    if record.get("evaluation_version") != EVALUATION_VERSION:
        failures.append("evaluation_version:mismatch")
    if record.get("rubric_version") != RUBRIC_VERSION:
        failures.append("rubric_version:mismatch")
    if record.get("output_contract_version") != OUTPUT_CONTRACT_VERSION:
        failures.append("output_contract_version:mismatch")
    if record.get("split") != SPLIT:
        failures.append("split:not_test")
    if not isinstance(record.get("provenance"), dict):
        failures.append("provenance:missing")
    else:
        for field in (
            "source_id",
            "source_type",
            "review_id",
            "reviewer_user_id",
            "approved_at",
            "model_version",
            "prompt_version",
            "evaluation_result_id",
            "evaluation_classification",
        ):
            if not _text(record["provenance"].get(field)):
                failures.append(f"provenance:{field}:missing")
        if record["provenance"].get("evaluation_classification") != "PILOT_EVALUATION":
            failures.append("provenance:evaluation_classification")
    try:
        target = json.loads(record.get("output", ""))
        context = record["input"].split("Retrieved evidence and citation context:", 1)[1].strip()
        context_ids = set(_source_ids(json.loads(context)))
        target_ids = set(_source_ids(target.get("citations")))
        if not set(item["source_id"] for item in record["citations"]).issubset(context_ids):
            failures.append("citation_context:record_source_id_missing")
        if not target_ids.issubset(context_ids):
            failures.append("citation_context:target_source_id_missing")
    except (IndexError, TypeError, json.JSONDecodeError, AttributeError):
        failures.append("citation_context:unparseable")
    if _contains_pii(record):
        failures.append("privacy:pii_detected")
    return sorted(set(failures))


def _base_record(source: dict[str, Any], source_dataset_version: str) -> dict[str, Any]:
    decision = _text(source.get("review_decision")).upper()
    status = _text(source.get("candidate_status"))
    if status != "APPROVED_FOR_DATASET" or decision not in {"APPROVE", "CORRECT"}:
        raise EvaluationSetError("review:not_explicitly_approved")
    if source.get("candidate_synthetic") is True or source.get("record_synthetic") is True:
        raise EvaluationSetError("synthetic:forbidden")
    if source.get("scenario_group") != HOLDOUT_SCENARIO and source_dataset_version == "live-governed-evaluation":
        raise EvaluationSetError("holdout:scenario_mismatch")
    input_text = _text(source.get("input"))
    output_text = _text(source.get("accepted_output")) or _text(source.get("human_edited_output")) or _text(source.get("ai_output"))
    evidence = _json(source.get("evidence_used_json"), "evidence_used_json")
    source_ids = _source_ids(evidence)
    if not input_text or not output_text or not source_ids:
        raise EvaluationSetError("required_fields:missing")
    provenance = {
        "source_id": _text(source.get("record_id")),
        "source_type": _text(source.get("source_type")),
        "review_id": _text(source.get("review_id")),
        "reviewer_user_id": _text(source.get("candidate_reviewer_user_id")),
        "approved_at": _text(source.get("reviewed_at")),
        "model_version": _text(source.get("model_version")),
        "prompt_version": _text(source.get("prompt_version")),
        "evaluation_result_id": _text(source.get("evaluation_result_id")),
        "evaluation_classification": "PILOT_EVALUATION",
    }
    if not all(provenance.values()):
        raise EvaluationSetError("provenance:incomplete")
    record = {
        "dataset_version": OUTPUT_CONTRACT_VERSION,
        "evaluation_set_version": EVALUATION_SET_VERSION,
        "evaluation_version": EVALUATION_VERSION,
        "rubric_version": RUBRIC_VERSION,
        "output_contract_version": OUTPUT_CONTRACT_VERSION,
        "source_dataset_version": source_dataset_version,
        "example_id": _text(source.get("record_id")),
        "scenario_group": _text(source.get("scenario_group")),
        "task": _text(source.get("task")),
        "input": input_text,
        "retrieved_context": _text(source.get("retrieved_context")),
        "output": output_text,
        "citations": [{"source_id": source_id} for source_id in source_ids],
        "provenance": provenance,
        "review_decision": "CORRECTED" if decision == "CORRECT" else "APPROVE",
        "synthetic": False,
        "split": SPLIT,
    }
    failures = _validate_evaluation_record(record)
    if failures:
        raise EvaluationSetError(";".join(failures))
    return record


def _read_v03_test_records(v03_dir: Path) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    records: list[dict[str, Any]] = []
    all_records: list[dict[str, Any]] = []
    for path in sorted(v03_dir.glob("*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            record = json.loads(line)
            all_records.append(record)
            if record.get("split") == SPLIT:
                records.append(record)
    return records, all_records


def read_holdout(docker_context: str, compose_file: Path) -> dict[str, Any]:
    command = [
        "docker", "--context", docker_context, "compose", "-f", str(compose_file),
        "exec", "-T", "postgres", "psql", "-U", "airural", "-d", "airural", "-At", "-c", HOLDOUT_QUERY,
    ]
    completed = subprocess.run(command, check=False, capture_output=True, text=True)
    if completed.returncode != 0:
        raise EvaluationSetError("POSTGRES_READ_FAILED:" + completed.stderr.strip())
    rows = [json.loads(line) for line in completed.stdout.splitlines() if line.strip()]
    if len(rows) != 1:
        raise EvaluationSetError(f"holdout:expected_one_approved_row:{len(rows)}")
    return rows[0]


def _record_from_v03(record: dict[str, Any]) -> dict[str, Any]:
    result = dict(record)
    result["evaluation_set_version"] = EVALUATION_SET_VERSION
    result["evaluation_version"] = EVALUATION_VERSION
    result["rubric_version"] = RUBRIC_VERSION
    result["output_contract_version"] = OUTPUT_CONTRACT_VERSION
    result["source_dataset_version"] = OUTPUT_CONTRACT_VERSION
    result["split"] = SPLIT
    result["provenance"] = {
        **record["provenance"],
        "evaluation_version": EVALUATION_VERSION,
        "rubric_version": RUBRIC_VERSION,
        "evaluation_set_version": EVALUATION_SET_VERSION,
    }
    failures = _validate_evaluation_record(result)
    if failures:
        raise EvaluationSetError(f"v03:{record.get('scenario_group')}:{';'.join(failures)}")
    return result


def _read_export_records(export_dir: Path) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    if not export_dir.exists():
        return records
    for path in sorted(export_dir.glob("*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip():
                records.append(json.loads(line))
    return records


def materialize(
    v03_dir: Path,
    output_dir: Path,
    report_path: Path,
    docker_context: str,
    compose_file: Path,
    v01_dir: Path,
) -> dict[str, Any]:
    if output_dir.exists() and any(output_dir.iterdir()):
        raise EvaluationSetError(f"IMMUTABLE_EVALUATION_SET_EXISTS:{output_dir}")
    v03_test, v03_all = _read_v03_test_records(v03_dir)
    selected = list(v03_test)
    excluded: Counter[str] = Counter()
    if not V03_REQUIRED_TEST_TASKS.issubset({record.get("task") for record in selected}):
        excluded["v03_required_root_or_rag_test_missing"] += 1
    if any(record.get("scenario_group") == HOLDOUT_SCENARIO for record in v03_all):
        excluded["holdout_scenario_overlaps_v03"] += 1
    if any(record.get("split") != SPLIT for record in v03_all if record.get("scenario_group") in {item.get("scenario_group") for item in selected}):
        excluded["v03_test_scenario_split_leakage"] += 1
    if excluded:
        raise EvaluationSetError("SOURCE_GATES_FAILED:" + json.dumps(dict(excluded), sort_keys=True))

    records = [_record_from_v03(record) for record in selected]
    holdout_source = read_holdout(docker_context, compose_file)
    v01_records = _read_export_records(v01_dir)
    holdout_eval_id = _text(holdout_source.get("evaluation_result_id"))
    if any(
        record.get("scenario_group") == HOLDOUT_SCENARIO
        or record.get("provenance", {}).get("evaluation_result_id") == holdout_eval_id
        for record in v03_all + v01_records
    ):
        raise EvaluationSetError("holdout:already_present_in_training_or_v03_export")
    excluded["dataset-v0.3-train-or-validation"] = sum(1 for record in v03_all if record.get("split") != SPLIT)
    holdout = _base_record(holdout_source, "live-governed-evaluation")
    records.append(holdout)

    seen_ids: set[str] = set()
    seen_scenarios: set[str] = set()
    seen_fingerprints: set[str] = set()
    for record in records:
        if record["example_id"] in seen_ids:
            raise EvaluationSetError("duplicate:example_id")
        if record["scenario_group"] in seen_scenarios:
            raise EvaluationSetError("scenario_leakage:duplicate_scenario")
        fingerprint = _fingerprint(record)
        if fingerprint in seen_fingerprints:
            raise EvaluationSetError("duplicate:content")
        seen_ids.add(record["example_id"])
        seen_scenarios.add(record["scenario_group"])
        seen_fingerprints.add(fingerprint)

    task_counts = Counter(record["task"] for record in records)
    missing = REQUIRED_TASKS - set(task_counts)
    if missing:
        raise EvaluationSetError("task_coverage_missing:" + ",".join(sorted(missing)))
    output_dir.mkdir(parents=True, exist_ok=True)
    for task in TASKS:
        path = output_dir / f"{task}.jsonl"
        rows = [json.dumps(record, ensure_ascii=True, sort_keys=True, separators=(",", ":")) for record in records if record["task"] == task]
        path.write_text("\n".join(rows) + "\n", encoding="utf-8")
    manifest = {
        "evaluation_set_version": EVALUATION_SET_VERSION,
        "immutable": True,
        "output_contract_version": OUTPUT_CONTRACT_VERSION,
        "evaluation_version": EVALUATION_VERSION,
        "rubric_version": RUBRIC_VERSION,
        "total_test_examples": len(records),
        "task_counts": {task: task_counts[task] for task in TASKS},
        "split_counts": {"train": 0, "validation": 0, "test": len(records)},
        "scenario_leakage_count": 0,
        "split_leakage_count": 0,
        "duplicate_count": 0,
        "pii_detected": 0,
        "synthetic_examples": 0,
        "excluded_examples": dict(sorted(excluded.items())),
        "training_data_modified": False,
        "training_performed": False,
    }
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    report_path.write_text(
        "# EVALUATION SET v1.0.0 QUALITY REPORT\n\n"
        "Immutable held-out evaluation set for Base-versus-Fine-tuned comparison.\n"
        "It contains only approved non-training records and does not modify dataset-v0.3.\n\n"
        f"- Evaluation set version: `{EVALUATION_SET_VERSION}`\n"
        f"- Output contract: `{OUTPUT_CONTRACT_VERSION}`\n"
        f"- Evaluation version: `{EVALUATION_VERSION}`\n"
        f"- Rubric version: `{RUBRIC_VERSION}`\n"
        f"- Total test examples: `{len(records)}`\n"
        f"- Root-cause count: `{task_counts['root-cause-analysis']}`\n"
        f"- Recommendation count: `{task_counts['recommendation-generation']}`\n"
        f"- RAG count: `{task_counts['rag-grounded-responses']}`\n"
        "- Train: `0`\n"
        "- Validation: `0`\n"
        f"- Test: `{len(records)}`\n"
        "- Strict JSON/schema contract: `passed`\n"
        "- Citation/source-ID validity: `passed`\n"
        "- Evidence context: `passed`\n"
        "- PII: `passed`\n"
        "- Duplicates: `0`\n"
        "- Scenario leakage: `0`\n"
        "- Split leakage: `0`\n"
        "- Synthetic records: `0`\n"
        f"- Excluded dataset-v0.3 TRAIN/VALIDATION records: `{excluded['dataset-v0.3-train-or-validation']}` (not evaluation rows)\n"
        "- dataset-v0.3 modified: `no`\n"
        "- Training performed: `no`\n",
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Materialize the immutable held-out evaluation set.")
    parser.add_argument("--v03-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.3"))
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/evaluation/heldout/evaluation-set-v1.0.0"))
    parser.add_argument("--report", type=Path, default=Path("EVALUATION_SET_V1_QUALITY_REPORT.md"))
    parser.add_argument("--docker-context", default="desktop-linux")
    parser.add_argument("--compose-file", type=Path, default=Path("docker-compose.yml"))
    parser.add_argument("--v01-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.1"))
    args = parser.parse_args()
    print(json.dumps(materialize(args.v03_dir, args.output_dir, args.report, args.docker_context, args.compose_file, args.v01_dir), sort_keys=True))


if __name__ == "__main__":
    main()

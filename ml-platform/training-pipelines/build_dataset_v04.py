"""
Purpose: Materializes immutable dataset-v0.4 after governed overflow corrections.
Why it exists: v0.3 must remain immutable while corrected records replace only
the six approved overflow examples and pass the exact Qwen formatting gates.
Architecture fit: Read-only MLOps release boundary; it reads PostgreSQL review
state and the immutable v0.3 export, writes v0.4 JSONL, and never changes data
or starts training.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
from collections import Counter
from pathlib import Path
from typing import Any

from transformers import AutoTokenizer

from contracts.dataset_v03_contract import SEQUENCE_LENGTH, format_training_example, validate_record


DATASET_VERSION = "dataset-v0.4"
SOURCE_DATASET_VERSION = "dataset-v0.3"
TARGET_BUDGET = 512
TASKS = ("root-cause-analysis", "recommendation-generation", "rag-grounded-responses")
SPLITS = ("train", "validation", "test")
MODEL = "Qwen/Qwen2.5-0.5B-Instruct"

OVERFLOW_IDS = {
    "0e454d77-aed4-4ce9-a019-c1d92a97aab7",
    "437b4674-d344-40f5-bc47-84488f8792da",
    "43ea9cc6-45cf-43af-b746-e7bca8ff7128",
    "6122d108-a6a8-44c7-8351-6d2ab5cef88e",
    "92191669-e7f4-41db-87fc-bc57c7f710cf",
    "de3a0449-b8a8-43b1-b27b-5ffb5e22e3bd",
}


class DatasetV04Error(RuntimeError):
    """Raised when v0.4 cannot pass a release gate."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _json(value: Any, label: str) -> Any:
    try:
        return json.loads(value) if isinstance(value, str) else value
    except (TypeError, json.JSONDecodeError) as exc:
        raise DatasetV04Error(f"{label}:MALFORMED_JSON") from exc


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


def _load_v03(v03_dir: Path) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    for path in sorted(v03_dir.glob("*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip():
                records.append(json.loads(line))
    return records


def _snapshot(compose_file: Path, docker_context: str) -> list[dict[str, Any]]:
    query = r"""
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
  'evaluation_result_id', COALESCE(r.evaluation_result_id, source_r.evaluation_result_id),
  'evaluation_classification', COALESCE(p.evaluation_classification,
    CASE WHEN r.source_type LIKE 'PILOT_EVALUATION%' OR source_r.source_type LIKE 'PILOT_EVALUATION%'
      THEN 'PILOT_EVALUATION' END),
  'scenario_classification', COALESCE(s.evaluation_classification,
    CASE WHEN r.source_type LIKE 'PILOT_EVALUATION%' OR source_r.source_type LIKE 'PILOT_EVALUATION%'
      THEN 'PILOT_EVALUATION' END)
)
FROM learning.training_candidates c
JOIN learning.learning_records r ON r.id = c.learning_record_id
LEFT JOIN learning.training_candidates source_c
  ON source_c.id = NULLIF((r.evaluation_metadata_json::jsonb ->> 'sourceCandidateId'), '')::uuid
LEFT JOIN learning.learning_records source_r ON source_r.id = source_c.learning_record_id
LEFT JOIN evaluation.pilot_scenario_results p
  ON p.id = COALESCE(r.evaluation_result_id, source_r.evaluation_result_id)
LEFT JOIN evaluation.pilot_scenarios s ON s.id = p.scenario_id
WHERE r.scenario_group LIKE 'pilot-v03-%'
ORDER BY c.created_at
"""
    command = [
        "docker", "--context", docker_context, "compose", "-f", str(compose_file),
        "exec", "-T", "postgres", "psql", "-U", "airural", "-d", "airural", "-At", "-c", query,
    ]
    completed = subprocess.run(command, capture_output=True, text=True, check=False)
    if completed.returncode:
        raise DatasetV04Error("POSTGRES_READ_FAILED:" + completed.stderr.strip())
    return [json.loads(line) for line in completed.stdout.splitlines() if line.strip()]


def _correction_record(row: dict[str, Any], original: dict[str, Any], split: str) -> dict[str, Any]:
    if row.get("candidate_status") != "APPROVED_FOR_DATASET" or row.get("review_decision") not in {"APPROVE", "CORRECT"}:
        raise DatasetV04Error("review_not_explicitly_approved")
    if row.get("candidate_synthetic") is True or row.get("record_synthetic") is True:
        raise DatasetV04Error("synthetic_record_forbidden")
    if row.get("evaluation_classification") != "PILOT_EVALUATION" or row.get("scenario_classification") != "PILOT_EVALUATION":
        raise DatasetV04Error("pilot_classification_missing")
    evidence = _json(row.get("evidence_used_json"), "evidence_used_json")
    source_ids = _source_ids(evidence)
    output = _text(row.get("accepted_output")) or _text(row.get("human_edited_output")) or _text(row.get("ai_output"))
    input_text = _text(row.get("input"))
    if not input_text or not output or not source_ids:
        raise DatasetV04Error("required_correction_field_missing")
    provenance = {
        "source_id": _text(row.get("record_id")),
        "original_example_id": _text(original.get("example_id")),
        "source_type": _text(row.get("source_type")),
        "review_id": _text(row.get("review_id")),
        "reviewer_user_id": _text(row.get("candidate_reviewer_user_id")),
        "approved_at": _text(row.get("reviewed_at")),
        "model_version": _text(row.get("model_version")) or _text(original.get("provenance", {}).get("model_version")),
        "prompt_version": _text(row.get("prompt_version")) or _text(original.get("provenance", {}).get("prompt_version")),
        "evaluation_result_id": _text(row.get("evaluation_result_id")) or _text(original.get("provenance", {}).get("evaluation_result_id")),
        "evaluation_classification": "PILOT_EVALUATION",
    }
    record = {
        "dataset_version": DATASET_VERSION,
        "example_id": _text(row.get("record_id")),
        "scenario_group": _text(row.get("scenario_group")),
        "task": _text(row.get("task")),
        "input": input_text,
        "output": output,
        "citations": [{"source_id": source_id} for source_id in source_ids],
        "provenance": provenance,
        "review_decision": "CORRECTED" if row.get("review_decision") == "CORRECT" else "APPROVE",
        "synthetic": False,
        "split": split,
    }
    if _contains_pii(record):
        raise DatasetV04Error("pii_detected")
    failures = validate_record(record, expected_dataset_version=DATASET_VERSION)
    if failures:
        raise DatasetV04Error("contract:" + ",".join(failures))
    return record


def _token_gate(tokenizer: Any, record: dict[str, Any]) -> tuple[int, int]:
    formatted = format_training_example(tokenizer, record, include_target=True)
    formatted_tokens = len(tokenizer(formatted, add_special_tokens=False)["input_ids"])
    target_tokens = len(tokenizer(record["output"], add_special_tokens=False)["input_ids"])
    return formatted_tokens, target_tokens


def _provenance_gate(record: dict[str, Any]) -> None:
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
        raise DatasetV04Error("provenance_incomplete")
    if provenance.get("evaluation_classification") != "PILOT_EVALUATION":
        raise DatasetV04Error("provenance_not_pilot_evaluation")


def materialize(v03_dir: Path, output_dir: Path, report_path: Path, compose_file: Path, docker_context: str) -> dict[str, Any]:
    if output_dir.exists() and any(output_dir.iterdir()):
        raise DatasetV04Error(f"IMMUTABLE_DATASET_EXISTS:{output_dir}")
    v03 = _load_v03(v03_dir)
    by_id = {record["example_id"]: record for record in v03}
    if set(OVERFLOW_IDS) - set(by_id):
        raise DatasetV04Error("v03_overflow_records_missing")
    corrected_rows = [row for row in _snapshot(compose_file, docker_context) if row.get("source_type") == "PILOT_EVALUATION_CORRECTION_PROPOSAL" and row.get("review_decision") in {"APPROVE", "CORRECT"}]
    originals_by_scenario = {record["scenario_group"]: record for record in v03}
    corrected: list[dict[str, Any]] = []
    exclusions: Counter[str] = Counter()
    tokenizer = AutoTokenizer.from_pretrained(MODEL, local_files_only=True)
    for row in corrected_rows:
        original = originals_by_scenario.get(_text(row.get("scenario_group")))
        if not original or original["example_id"] not in OVERFLOW_IDS:
            exclusions["correction_not_linked_to_overflow_source"] += 1
            continue
        split = original["split"]
        try:
            record = _correction_record(row, original, split)
            formatted_tokens, target_tokens = _token_gate(tokenizer, record)
            if formatted_tokens > SEQUENCE_LENGTH:
                raise DatasetV04Error(f"formatted_sequence_overflow:{formatted_tokens}")
            if target_tokens > TARGET_BUDGET:
                raise DatasetV04Error(f"target_generation_overflow:{target_tokens}")
            record["token_metrics"] = {"formatted_tokens": formatted_tokens, "target_tokens": target_tokens}
            corrected.append(record)
        except DatasetV04Error as exc:
            exclusions[str(exc)] += 1
    replacement_scenarios = {record["scenario_group"] for record in corrected}
    accepted = [record for record in v03 if record["example_id"] not in OVERFLOW_IDS and record["scenario_group"] not in replacement_scenarios]
    accepted.extend(corrected)
    seen_ids: set[str] = set()
    seen_fingerprints: set[str] = set()
    scenario_splits: dict[str, str] = {}
    retained: list[dict[str, Any]] = []
    for record in accepted:
        record["dataset_version"] = DATASET_VERSION
        record.pop("token_metrics", None)
        if record["example_id"] in seen_ids:
            raise DatasetV04Error("duplicate_example_id")
        fingerprint = hashlib.sha256(f"{record['task']}\n{record['input']}\n{record['output']}".encode()).hexdigest()
        if fingerprint in seen_fingerprints:
            raise DatasetV04Error("duplicate_content")
        if record["scenario_group"] in scenario_splits and scenario_splits[record["scenario_group"]] != record["split"]:
            raise DatasetV04Error("scenario_split_leakage")
        try:
            _provenance_gate(record)
            formatted_tokens, target_tokens = _token_gate(tokenizer, record)
            if formatted_tokens > SEQUENCE_LENGTH:
                raise DatasetV04Error(f"formatted_sequence_overflow:{formatted_tokens}")
            if target_tokens > TARGET_BUDGET:
                raise DatasetV04Error(f"target_generation_overflow:{target_tokens}")
            failures = validate_record(record, expected_dataset_version=DATASET_VERSION)
            if failures:
                raise DatasetV04Error("contract:" + ",".join(failures))
        except DatasetV04Error as exc:
            exclusions[f"{record['example_id']}:{exc}"] += 1
            continue
        seen_ids.add(record["example_id"])
        seen_fingerprints.add(fingerprint)
        scenario_splits[record["scenario_group"]] = record["split"]
        retained.append(record)
    accepted = retained
    counts = Counter(record["task"] for record in accepted)
    splits = Counter(record["split"] for record in accepted)
    if any(splits[split] == 0 for split in SPLITS):
        raise DatasetV04Error("required_split_empty:" + ",".join(split for split in SPLITS if splits[split] == 0))
    output_dir.mkdir(parents=True, exist_ok=True)
    for task in TASKS:
        path = output_dir / f"{task}.jsonl"
        rows = [json.dumps(record, ensure_ascii=True, sort_keys=True, separators=(",", ":")) for record in accepted if record["task"] == task]
        path.write_text("\n".join(rows) + "\n", encoding="utf-8")
    manifest = {
        "dataset_version": DATASET_VERSION, "source_dataset_version": SOURCE_DATASET_VERSION, "immutable": True,
        "total_examples": len(accepted), "task_counts": {task: counts[task] for task in TASKS},
        "split_counts": {split: splits[split] for split in SPLITS}, "corrected_records_materialized": len(corrected),
        "overflow_records_removed": len(OVERFLOW_IDS), "excluded_corrections": dict(sorted(exclusions.items())),
        "sequence_length": SEQUENCE_LENGTH, "target_generation_budget": TARGET_BUDGET, "model_tokenizer": MODEL,
        "validation": "passed", "training_performed": False,
    }
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    report_path.write_text(
        "# DATASET v0.4 QUALITY REPORT\n\n"
        "Materialized only from immutable v0.3 rows that pass the exact Qwen token gates and explicitly approved governed correction proposals. v0.3 was not modified and no training was performed.\n\n"
        f"- Total examples: `{len(accepted)}`\n- Root-cause count: `{counts['root-cause-analysis']}`\n- Recommendation count: `{counts['recommendation-generation']}`\n- RAG count: `{counts['rag-grounded-responses']}`\n"
        f"- Train: `{splits['train']}`\n- Validation: `{splits['validation']}`\n- Test: `{splits['test']}`\n- Corrected records materialized: `{len(corrected)}`\n- Overflow source records removed: `{len(OVERFLOW_IDS)}`\n"
        f"- Excluded correction records: `{json.dumps(dict(sorted(exclusions.items())), sort_keys=True)}`\n- Exact formatted sequence gate: `passed` (`{SEQUENCE_LENGTH}` max)\n- Exact target budget gate: `passed` (`{TARGET_BUDGET}` max)\n- Schema/citations/evidence/PII/provenance/duplicates/leakage: `passed`\n",
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--v03-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.3"))
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.4"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_V04_QUALITY_REPORT.md"))
    parser.add_argument("--compose-file", type=Path, default=Path("docker-compose.yml"))
    parser.add_argument("--docker-context", default="desktop-linux")
    args = parser.parse_args()
    print(json.dumps(materialize(args.v03_dir, args.output_dir, args.report, args.compose_file, args.docker_context), sort_keys=True))


if __name__ == "__main__":
    main()

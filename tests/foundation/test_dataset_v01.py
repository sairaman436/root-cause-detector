"""
Purpose: Verifies the dataset-v0.1 materializer's governance and leakage controls.
Why it exists: Training data must be reproducible and must reject unsafe or unapproved records before export.
Architecture fit: Foundation-level tests for the existing MLOps training-pipeline boundary.
"""

import importlib.util
import json
from pathlib import Path


MODULE_PATH = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "build_dataset_v01.py"
SPEC = importlib.util.spec_from_file_location("dataset_v01", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _record(example_id, task, group, decision="APPROVE", output="validated output", **overrides):
    record = {
        "id": example_id,
        "task": task,
        "scenario_group": group,
        "input": "What should the field team inspect?",
        "ai_output": "initial answer",
        "accepted_output": output,
        "review_decision": decision,
        "approval_status": "APPROVED_FOR_FUTURE_DATASET",
        "training_eligible": True,
        "privacy_classification": "INTERNAL",
        "citations": [{"source_id": "policy-1", "valid": True, "supports_claim": True}],
        "provenance": {
            "source_id": "learning-1",
            "source_type": "HUMAN_REVIEW",
            "review_id": "review-1",
            "approved_at": "2026-08-10T00:00:00Z",
            "model_version": "qwen-test",
            "prompt_version": "prompt-test",
        },
    }
    record.update(overrides)
    return record


def _write(path: Path, records):
    path.write_text("\n".join(json.dumps(record) for record in records) + "\n", encoding="utf-8")


def test_empty_repository_export_is_schema_valid(tmp_path):
    output = tmp_path / "dataset-v0.1"
    report = tmp_path / "DATASET_QUALITY_REPORT.md"
    manifest = MODULE.build_dataset(None, output, report)

    assert manifest["accepted_examples"] == 0
    assert manifest["rejected_examples"] == 0
    assert manifest["split_counts"] == {"train": 0, "validation": 0, "test": 0}
    assert manifest["split_leakage_count"] == 0
    assert all((output / f"{task}.jsonl").exists() for task in MODULE.TASKS)


def test_accepts_approved_and_validated_correction_and_rejects_unsafe_input(tmp_path):
    source = tmp_path / "source.jsonl"
    output = tmp_path / "dataset-v0.1"
    report = tmp_path / "DATASET_QUALITY_REPORT.md"
    records = [
        _record("approved-1", "root-cause-analysis", "scenario-a"),
        _record(
            "corrected-1",
            "recommendation-generation",
            "scenario-a",
            decision="CORRECTED",
            accepted_output="corrected recommendation",
            correction_validated=True,
        ),
        _record("rag-1", "rag-grounded-responses", "scenario-b"),
        _record("pii-1", "root-cause-analysis", "scenario-c", input="Contact officer@example.com",),
        _record("rejected-1", "root-cause-analysis", "scenario-d", decision="REJECT", approval_status="REJECTED"),
    ]
    _write(source, records)

    manifest = MODULE.build_dataset(source, output, report)

    assert manifest["accepted_examples"] == 3
    assert manifest["rejected_examples"] == 2
    assert manifest["validation_failures"]["pii_detected"] == 1
    assert manifest["validation_failures"]["not_human_approved"] == 1
    assert manifest["split_leakage_count"] == 0
    exported = (output / "recommendation-generation.jsonl").read_text(encoding="utf-8").strip().splitlines()
    assert json.loads(exported[0])["quality"]["corrected"] is True
    assert json.loads(exported[0])["dataset_version"] == "dataset-v0.1"


def test_same_scenario_group_always_maps_to_one_split():
    split = MODULE._split_for("related-scenario", "dataset-v0.1")
    assert split == MODULE._split_for("related-scenario", "dataset-v0.1")


def test_synthetic_records_are_not_allowed_in_production_dataset(tmp_path):
    source = tmp_path / "source.jsonl"
    _write(source, [_record("synthetic-1", "root-cause-analysis", "synthetic-group", synthetic=True)])

    manifest = MODULE.build_dataset(source, tmp_path / "dataset", tmp_path / "report.md", version="dataset-v0.1")

    assert manifest["accepted_examples"] == 0
    assert manifest["validation_failures"]["synthetic_not_allowed_in_production_dataset"] == 1

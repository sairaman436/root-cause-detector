"""
Purpose: Verifies pending review queue creation and promotion safety.
Why it exists: Training data must never be auto-approved or receive synthetic examples as production data.
Architecture fit: Foundation tests for the review bridge into the existing dataset materializer.
"""

import importlib.util
import json
from pathlib import Path

import pytest


MODULE_PATH = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "review_training_corpus.py"
SPEC = importlib.util.spec_from_file_location("review_queue", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


SOURCE = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "dataset-v0.1-dev-synthetic" / "source" / "synthetic_seed.jsonl"


def test_queue_keeps_all_examples_pending(tmp_path):
    output = tmp_path / "review_queue.jsonl"
    counts = MODULE.create_queue(SOURCE, output)
    rows = [json.loads(line) for line in output.read_text(encoding="utf-8").splitlines()]

    assert counts == {"available": 9, "pending": 9, "approved": 0, "corrected": 0, "rejected": 0}
    assert len(rows) == 9
    assert all(row["review"]["decision"] is None for row in rows)
    assert all(row["review_state"] == "PENDING_HUMAN_REVIEW" for row in rows)
    assert all(row["promotion_eligible"] is False for row in rows)
    assert all(row["original_record"]["review_decision"] == "VALIDATED" for row in rows)
    assert rows[0]["review_item"]["input"]
    assert rows[0]["review_item"]["ai_output"]
    assert rows[0]["review_item"]["evidence"]
    assert rows[0]["review_item"]["provenance"]["source_id"]


def test_decide_requires_human_action_and_preserves_correction(tmp_path):
    queue = tmp_path / "review_queue.jsonl"
    MODULE.create_queue(SOURCE, queue)

    with pytest.raises(MODULE.ReviewQueueError, match="CORRECTED_OUTPUT_AND_VALIDATION_REQUIRED"):
        MODULE.record_decision(queue, "synthetic-train-root-cause-001", "CORRECT", "review-1", "reviewer-1", "2026-08-10T12:00:00Z", "Needs correction")

    counts = MODULE.record_decision(
        queue,
        "synthetic-train-root-cause-001",
        "CORRECT",
        "review-1",
        "reviewer-1",
        "2026-08-10T12:00:00Z",
        "Corrected for development review.",
        corrected_output="Human-corrected development output.",
        correction_validated=True,
    )
    row = next(json.loads(line) for line in queue.read_text(encoding="utf-8").splitlines() if "synthetic-train-root-cause-001" in line)

    assert counts["corrected"] == 1
    assert row["review_state"] == "HUMAN_CORRECTED"
    assert row["original_record"]["ai_output"] == "Initial fictional analysis for development validation."
    assert row["review"]["corrected_output"] == "Human-corrected development output."
    assert row["promotion_eligible"] is False


def test_reject_requires_reason_and_stays_outside_training(tmp_path):
    queue = tmp_path / "review_queue.jsonl"
    MODULE.create_queue(SOURCE, queue)

    with pytest.raises(MODULE.ReviewQueueError, match="REJECTION_REASON_REQUIRED"):
        MODULE.record_decision(queue, "synthetic-train-rag-001", "REJECT", "review-2", "reviewer-2", "2026-08-10T12:00:00Z", "")

    counts = MODULE.record_decision(queue, "synthetic-train-rag-001", "REJECT", "review-2", "reviewer-2", "2026-08-10T12:00:00Z", "Synthetic fixture is not suitable for training.")
    assert counts["rejected"] == 1


def test_pending_queue_cannot_materialize_production_dataset(tmp_path):
    queue = tmp_path / "review_queue.jsonl"
    MODULE.create_queue(SOURCE, queue)

    with pytest.raises(MODULE.ReviewQueueError):
        MODULE.materialize_queue(queue, tmp_path / "dataset-v0.1", tmp_path / "report.md")

    assert not (tmp_path / "dataset-v0.1").exists()


def test_synthetic_approval_cannot_materialize_production_dataset(tmp_path):
    queue = tmp_path / "review_queue.jsonl"
    MODULE.create_queue(SOURCE, queue)
    rows = [json.loads(line) for line in queue.read_text(encoding="utf-8").splitlines()]
    for row in rows:
        row["review"] = {
            "decision": "APPROVE",
            "review_id": f"human-review-{row['example_id']}",
            "reviewer": "human-reviewer",
            "reviewed_at": "2026-08-10T12:00:00Z",
            "comments": "Explicit development review only.",
            "corrected_output": None,
            "correction_validated": False,
        }
    queue.write_text("\n".join(json.dumps(row) for row in rows) + "\n", encoding="utf-8")

    with pytest.raises(MODULE.ReviewQueueError):
        MODULE.materialize_queue(queue, tmp_path / "dataset-v0.1", tmp_path / "report.md")

    assert not (tmp_path / "dataset-v0.1").exists()

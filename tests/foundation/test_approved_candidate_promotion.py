"""
Purpose: Verifies the authenticated candidate export boundary into dataset-v0.1.
Why it exists: Promotion must reject empty, pending, synthetic, incomplete, and ungrounded records before writing production artifacts.
Architecture fit: Tests the MLOps adapter while reusing build_dataset_v01.py for final validation.
"""

import importlib.util
from pathlib import Path

import pytest


MODULE_PATH = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "promote_approved_candidates.py"
SPEC = importlib.util.spec_from_file_location("approved_candidate_promotion", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _export(decision="APPROVE", synthetic=False):
    return {
        "datasetVersion": "dataset-v0.1",
        "examples": [{
            "exampleId": "real-example-1",
            "task": "root-cause-analysis",
            "scenarioGroup": "village-water-1",
            "input": "What should the officer verify?",
            "output": "Verify the work order and field evidence.",
            "citations": [{"source_id": "policy-1", "valid": True, "supports_claim": True}],
            "provenance": {"source_id": "learning-1", "source_type": "REAL_GOVERNED_INTERACTION", "review_id": "review-1", "approved_at": "2026-08-10T12:00:00Z", "model_version": "qwen", "prompt_version": "prompt-1"},
            "reviewDecision": decision,
            "synthetic": synthetic,
        }],
    }


def test_empty_export_is_blocked_without_creating_dataset(tmp_path):
    with pytest.raises(MODULE.CandidateExportError, match="NO_APPROVED_REAL_CANDIDATES"):
        MODULE.materialize_export({"datasetVersion": "dataset-v0.1", "examples": []}, tmp_path / "dataset", tmp_path / "report.md")
    assert not (tmp_path / "dataset").exists()


def test_synthetic_export_is_blocked(tmp_path):
    with pytest.raises(MODULE.CandidateExportError, match="SYNTHETIC_CANDIDATE_IN_EXPORT"):
        MODULE.materialize_export(_export(synthetic=True), tmp_path / "dataset", tmp_path / "report.md")


def test_pending_export_is_blocked(tmp_path):
    with pytest.raises(MODULE.CandidateExportError, match="UNAPPROVED_CANDIDATE_IN_EXPORT"):
        MODULE.materialize_export(_export(decision="PENDING"), tmp_path / "dataset", tmp_path / "report.md")


def test_approved_real_export_passes_existing_materializer(tmp_path):
    manifest = MODULE.materialize_export(_export(), tmp_path / "dataset", tmp_path / "report.md")
    assert manifest["accepted_examples"] == 1
    assert manifest["split_leakage_count"] == 0
    assert manifest["training_performed"] is False

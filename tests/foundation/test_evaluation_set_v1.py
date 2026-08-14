"""
Purpose: Protects the held-out evaluation-set contract and task coverage.
Why it exists: A future change must not turn the comparison set into a
training leak or silently remove one of the required task classes.
Architecture fit: Foundation tests for the evaluation/MLOps boundary.
"""

from __future__ import annotations

import importlib.util
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
MODULE_PATH = ROOT / "ml-platform/evaluation/heldout/validate_evaluation_set_v1.py"
SPEC = importlib.util.spec_from_file_location("validate_evaluation_set_v1", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def test_materialized_evaluation_set_is_valid_and_has_all_tasks() -> None:
    result = MODULE.validate(ROOT / "ml-platform/evaluation/heldout/evaluation-set-v1.0.0")
    assert result["valid"] is True, result
    assert result["records"] == 4
    assert result["task_counts"] == {
        "root-cause-analysis": 1,
        "recommendation-generation": 1,
        "rag-grounded-responses": 2,
    }
    assert result["split_counts"] == {"train": 0, "validation": 0, "test": 4}

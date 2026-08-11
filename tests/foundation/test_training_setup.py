"""
Purpose: Verifies the training setup's configuration and safety gates without downloading model weights.
Why it exists: A long-running training worker must refuse empty, unapproved, or leaky data before loading a model.
Architecture fit: Foundation tests for the isolated MLOps LoRA/QLoRA boundary.
"""

import importlib.util
from pathlib import Path

import pytest


MODULE_PATH = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "run_lora_qlora_smoke_test.py"
SPEC = importlib.util.spec_from_file_location("training_setup", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)

CONFIG_PATH = Path(__file__).parents[2] / "ml-platform" / "training-pipelines" / "configs" / "qwen2.5-0.5b-instruct-lora-qlora.toml"


def test_qwen_config_is_reproducible_and_uses_qlora():
    config = MODULE.load_config(CONFIG_PATH)

    assert config["model"]["base_model"] == "Qwen/Qwen2.5-0.5B-Instruct"
    assert config["experiment"]["dataset_version"] == "dataset-v0.1"
    assert config["quantization"]["method"] == "qlora_4bit"
    assert config["lora"]["rank"] > 0
    assert config["checkpoint"]["resume_supported"] is True


def test_empty_dataset_is_rejected_before_model_loading(tmp_path):
    config = MODULE.load_config(CONFIG_PATH)
    empty_dataset = tmp_path / "empty-dataset"
    empty_dataset.mkdir()
    (empty_dataset / "manifest.json").write_text(
        '{"dataset_version":"dataset-v0.1","accepted_examples":0,"split_leakage_count":0}',
        encoding="utf-8",
    )
    config["experiment"]["dataset_dir"] = str(empty_dataset)

    with pytest.raises(MODULE.TrainingSetupError, match="DATASET_EMPTY_NO_APPROVED_EXAMPLES"):
        MODULE.validate_dataset(config)

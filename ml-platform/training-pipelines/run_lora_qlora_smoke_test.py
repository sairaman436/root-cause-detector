"""
Purpose: Runs one bounded dataset-to-checkpoint LoRA/QLoRA smoke test.
Why it exists: The training setup must prove tokenizer formatting, adapter injection, backward propagation, validation-loss capture, and checkpoint writing before a long run is allowed.
Architecture fit: Isolated MLOps worker utility; it never changes production models and refuses empty or unapproved datasets.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import random
import sys
from pathlib import Path
from typing import Any

from contracts.dataset_v03_contract import format_training_example


class TrainingSetupError(RuntimeError):
    """Raised when the configured training smoke test cannot safely run."""


def load_config(path: Path) -> dict[str, Any]:
    try:
        import tomllib
    except ImportError as exc:  # pragma: no cover - Python 3.11+ is required.
        raise TrainingSetupError("PYTHON_TOMLLIB_UNAVAILABLE") from exc
    with path.open("rb") as stream:
        return tomllib.load(stream)


def _read_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise TrainingSetupError(f"DATASET_FILE_MISSING:{path}")
    rows: list[dict[str, Any]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            row = json.loads(line)
        except json.JSONDecodeError as exc:
            raise TrainingSetupError(f"DATASET_INVALID_JSON:{path}:{line_number}") from exc
        if not isinstance(row, dict):
            raise TrainingSetupError(f"DATASET_ROW_NOT_OBJECT:{path}:{line_number}")
        rows.append(row)
    return rows


def validate_dataset(config: dict[str, Any]) -> tuple[list[dict[str, Any]], list[dict[str, Any]], dict[str, Any]]:
    expected_version = config["experiment"]["dataset_version"]
    dataset_dir = Path(config["experiment"]["dataset_dir"])
    manifest_path = dataset_dir / "manifest.json"
    if not manifest_path.exists():
        raise TrainingSetupError(f"DATASET_MANIFEST_MISSING:{manifest_path}")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("dataset_version") != expected_version:
        raise TrainingSetupError("DATASET_VERSION_MISMATCH")
    if manifest.get("accepted_examples", 0) <= 0:
        raise TrainingSetupError("DATASET_EMPTY_NO_APPROVED_EXAMPLES")
    if manifest.get("split_leakage_count", 1) != 0:
        raise TrainingSetupError("DATASET_SPLIT_LEAKAGE")

    def load_files(names: list[str], expected_split: str) -> list[dict[str, Any]]:
        rows: list[dict[str, Any]] = []
        for name in names:
            for row in _read_jsonl(dataset_dir / name):
                if row.get("dataset_version") != expected_version:
                    raise TrainingSetupError("DATASET_ROW_VERSION_MISMATCH")
                if row.get("split") != expected_split:
                    continue
                if not row.get("input", "").strip() or not row.get("output", "").strip():
                    raise TrainingSetupError("DATASET_EMPTY_INPUT_OR_OUTPUT")
                if not row.get("provenance") or not row.get("citations"):
                    raise TrainingSetupError("DATASET_LINEAGE_OR_CITATION_MISSING")
                rows.append(row)
        return rows

    train = load_files(config["data"]["train_files"], "train")
    validation = load_files(config["data"]["validation_files"], "validation")
    if not train or not validation:
        raise TrainingSetupError("DATASET_REQUIRES_TRAIN_AND_VALIDATION_EXAMPLES")
    return train, validation, manifest


def format_example(tokenizer: Any, row: dict[str, Any]) -> str:
    return format_training_example(tokenizer, row, include_target=True)


def _seed_everything(seed: int, torch: Any) -> None:
    random.seed(seed)
    torch.manual_seed(seed)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)
    if hasattr(torch, "use_deterministic_algorithms"):
        torch.use_deterministic_algorithms(True, warn_only=True)


def _dtype(torch: Any, name: str) -> Any:
    return {"bfloat16": torch.bfloat16, "float16": torch.float16, "float32": torch.float32}[name]


def _on_model_device(batch: dict[str, Any], model: Any) -> dict[str, Any]:
    device = next(model.parameters()).device
    return {key: value.to(device) if hasattr(value, "to") else value for key, value in batch.items()}


def run_smoke(config_path: Path) -> dict[str, Any]:
    config = load_config(config_path)
    train_rows, validation_rows, manifest = validate_dataset(config)
    try:
        import torch
        from peft import LoraConfig, PeftModel, get_peft_model, prepare_model_for_kbit_training
        from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
    except ImportError as exc:
        raise TrainingSetupError(f"TRAINING_DEPENDENCY_MISSING:{exc.name}") from exc

    seed = int(config["experiment"]["seed"])
    _seed_everything(seed, torch)
    model_config = config["model"]
    quant_config = config["quantization"]
    quantization = quant_config["method"].lower()
    model_kwargs: dict[str, Any] = {"trust_remote_code": bool(model_config["trust_remote_code"])}
    if quantization == "qlora_4bit":
        if not torch.cuda.is_available():
            raise TrainingSetupError("QLORA_4BIT_REQUIRES_CUDA_FOR_SMOKE_TEST")
        model_kwargs["quantization_config"] = BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_quant_type=quant_config["quant_type"],
            bnb_4bit_use_double_quant=bool(quant_config["double_quantization"]),
            bnb_4bit_compute_dtype=_dtype(torch, quant_config["compute_dtype"]),
        )
        model_kwargs["device_map"] = "auto"
    elif quantization not in {"none", "lora"}:
        raise TrainingSetupError(f"UNSUPPORTED_QUANTIZATION:{quantization}")

    tokenizer = AutoTokenizer.from_pretrained(model_config["base_model"], trust_remote_code=bool(model_config["trust_remote_code"]))
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    sequence_length = int(config["data"]["sequence_length"])
    encoded = tokenizer(
        [format_example(tokenizer, row) for row in train_rows[:2]],
        return_tensors="pt",
        padding=True,
        truncation=True,
        max_length=sequence_length,
    )
    encoded["labels"] = encoded["input_ids"].clone()

    model = AutoModelForCausalLM.from_pretrained(model_config["base_model"], **model_kwargs)
    if quantization == "qlora_4bit":
        model = prepare_model_for_kbit_training(model)
    adapter_config = LoraConfig(
        r=int(config["lora"]["rank"]),
        lora_alpha=int(config["lora"]["alpha"]),
        lora_dropout=float(config["lora"]["dropout"]),
        target_modules=list(config["lora"]["target_modules"]),
        bias="none",
        task_type="CAUSAL_LM",
    )
    resume = config["experiment"].get("resume_from_checkpoint", "").strip()
    model = PeftModel.from_pretrained(model, resume, is_trainable=True) if resume else get_peft_model(model, adapter_config)
    if bool(config["training"]["gradient_checkpointing"]):
        model.gradient_checkpointing_enable()
        model.config.use_cache = False
    model.train()
    output_dir = Path(config["experiment"]["output_dir"])
    checkpoint_dir = output_dir / "checkpoint-smoke"
    checkpoint_dir.mkdir(parents=True, exist_ok=True)
    optimizer = torch.optim.AdamW((parameter for parameter in model.parameters() if parameter.requires_grad), lr=float(config["training"]["learning_rate"]))
    optimizer_state = checkpoint_dir / "optimizer.pt"
    if resume and optimizer_state.exists():
        optimizer.load_state_dict(torch.load(optimizer_state, map_location="cpu"))
    result = model(**_on_model_device(encoded, model))
    loss = result.loss
    if loss is None or not torch.isfinite(loss):
        raise TrainingSetupError("FORWARD_LOSS_INVALID")
    loss.backward()
    optimizer.step()
    optimizer.zero_grad(set_to_none=True)
    model.eval()
    with torch.no_grad():
        validation_text = format_example(tokenizer, validation_rows[0])
        validation_batch = tokenizer(validation_text, return_tensors="pt", truncation=True, max_length=sequence_length)
        validation_batch["labels"] = validation_batch["input_ids"].clone()
        validation_loss = model(**_on_model_device(validation_batch, model)).loss
    model.save_pretrained(checkpoint_dir)
    tokenizer.save_pretrained(checkpoint_dir)
    torch.save(optimizer.state_dict(), optimizer_state)
    metadata = {
        "base_model": model_config["base_model"],
        "dataset_version": manifest["dataset_version"],
        "seed": seed,
        "quantization": quantization,
        "lora": config["lora"],
        "train_loss": float(loss.detach().cpu()),
        "validation_loss": float(validation_loss.detach().cpu()),
        "checkpoint": str(checkpoint_dir),
        "resume_supported": bool(config["checkpoint"]["resume_supported"]),
        "optimizer_state_saved": optimizer_state.exists(),
        "training_performed": False,
        "smoke_step_only": True,
    }
    (checkpoint_dir / "experiment-metadata.json").write_text(json.dumps(metadata, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    metadata["checkpoint_sha256"] = _sha256_tree(checkpoint_dir)
    return metadata


def _sha256_tree(path: Path) -> str:
    digest = hashlib.sha256()
    for file in sorted(item for item in path.rglob("*") if item.is_file()):
        digest.update(file.relative_to(path).as_posix().encode("utf-8"))
        digest.update(file.read_bytes())
    return digest.hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the bounded LoRA/QLoRA setup smoke test.")
    parser.add_argument("--config", type=Path, required=True)
    parser.add_argument("--smoke", action="store_true", help="Required explicit flag; prevents accidental long runs.")
    args = parser.parse_args()
    if not args.smoke:
        print("REFUSED: pass --smoke; this utility does not implement a full training run", file=sys.stderr)
        return 2
    try:
        print(json.dumps(run_smoke(args.config), sort_keys=True))
        return 0
    except TrainingSetupError as exc:
        print(f"SMOKE_TEST_BLOCKED: {exc}", file=sys.stderr)
        return 3


if __name__ == "__main__":
    raise SystemExit(main())

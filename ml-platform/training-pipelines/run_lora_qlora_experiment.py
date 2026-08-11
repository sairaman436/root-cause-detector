"""
Purpose: Runs exactly one bounded Qwen LoRA/QLoRA baseline experiment.
Why it exists: The approved training setup previously proved only a smoke step; this runner performs one reproducible, resumable experiment without changing the base model or dataset.
Architecture fit: Isolated MLOps worker utility that reuses the existing dataset/configuration gates and produces an auditable adapter and comparison report.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
import shutil
import sys
import time
from pathlib import Path
from typing import Any

from contracts.dataset_v03_contract import format_training_example, validate_generated_target, validate_record


ROOT = Path(__file__).resolve().parents[2]
SMOKE_MODULE_PATH = Path(__file__).with_name("run_lora_qlora_smoke_test.py")


def _load_smoke_module() -> Any:
    import importlib.util

    spec = importlib.util.spec_from_file_location("training_setup", SMOKE_MODULE_PATH)
    if spec is None or spec.loader is None:
        raise RuntimeError("TRAINING_SETUP_MODULE_UNAVAILABLE")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


SETUP = _load_smoke_module()
TrainingSetupError = SETUP.TrainingSetupError


def _sha256_tree(path: Path) -> str:
    digest = hashlib.sha256()
    for file in sorted(item for item in path.rglob("*") if item.is_file()):
        digest.update(file.relative_to(path).as_posix().encode("utf-8"))
        digest.update(file.read_bytes())
    return digest.hexdigest()


def _dataset_digest(dataset_dir: Path) -> str:
    return _sha256_tree(dataset_dir)


def _load_split(config: dict[str, Any], split: str) -> list[dict[str, Any]]:
    dataset_dir = Path(config["experiment"]["dataset_dir"])
    names = config["data"].get(f"{split}_files") or config["data"].get("train_files", [])
    rows: list[dict[str, Any]] = []
    for name in names:
        for row in SETUP._read_jsonl(dataset_dir / name):
            if row.get("split") == split:
                rows.append(row)
    return rows


def _validate_preflight(config: dict[str, Any]) -> tuple[list[dict[str, Any]], list[dict[str, Any]], list[dict[str, Any]], dict[str, Any], str]:
    train, validation, manifest = SETUP.validate_dataset(config)
    test = _load_split(config, "test")
    sequence_length = int(config["data"]["sequence_length"])
    if manifest.get("structured_targets_validated") is not True:
        raise TrainingSetupError("DATASET_STRUCTURED_TARGET_GATE_FAILED")
    if manifest.get("citation_context_validated") is not True:
        raise TrainingSetupError("DATASET_CITATION_CONTEXT_GATE_FAILED")
    for row in train + validation + test:
        if config["experiment"]["dataset_version"] == "dataset-v0.3":
            contract_errors = validate_record(row, sequence_length)
            if contract_errors:
                raise TrainingSetupError(f"DATASET_V03_CONTRACT_FAILED:{contract_errors[0]}")
        try:
            target = json.loads(str(row.get("output", "")))
        except json.JSONDecodeError as exc:
            raise TrainingSetupError("DATASET_STRUCTURED_TARGET_INVALID") from exc
        if not isinstance(target, dict):
            raise TrainingSetupError("DATASET_STRUCTURED_TARGET_NOT_OBJECT")
        if len(str(row["output"]).split()) > sequence_length:
            raise TrainingSetupError("DATASET_TARGET_EXCEEDS_SEQUENCE_LENGTH")
        if row.get("task") == "rag-grounded-responses":
            input_text = str(row.get("input", ""))
            if "Retrieved evidence and citation context" not in input_text or not row.get("citations"):
                raise TrainingSetupError("DATASET_RAG_CITATION_CONTEXT_GATE_FAILED")
    if not test:
        raise TrainingSetupError("DATASET_REQUIRES_TEST_EXAMPLE")
    if len({row.get("scenario_group") for row in train + validation + test}) != len(train + validation + test):
        raise TrainingSetupError("DATASET_SCENARIO_LEAKAGE")
    if any(row.get("split") != "train" for row in train):
        raise TrainingSetupError("TRAIN_SPLIT_INVALID")
    if any(row.get("split") != "validation" for row in validation):
        raise TrainingSetupError("VALIDATION_SPLIT_INVALID")
    if any(row.get("split") != "test" for row in test):
        raise TrainingSetupError("TEST_SPLIT_INVALID")
    dataset_dir = Path(config["experiment"]["dataset_dir"])
    return train, validation, test, manifest, _dataset_digest(dataset_dir)


def _imports() -> tuple[Any, Any, Any, Any, Any, Any, Any]:
    try:
        import torch
        from peft import LoraConfig, PeftModel, get_peft_model, prepare_model_for_kbit_training
        from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
    except ImportError as exc:
        raise TrainingSetupError(f"TRAINING_DEPENDENCY_MISSING:{exc.name}") from exc
    if not torch.cuda.is_available():
        raise TrainingSetupError("QLORA_4BIT_REQUIRES_CUDA")
    if not torch.cuda.is_bf16_supported():
        raise TrainingSetupError("CONFIGURED_BFLOAT16_UNSUPPORTED")
    return torch, LoraConfig, PeftModel, get_peft_model, prepare_model_for_kbit_training, AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig


def _model_kwargs(config: dict[str, Any], torch: Any, BitsAndBytesConfig: Any) -> dict[str, Any]:
    model_config = config["model"]
    quant_config = config["quantization"]
    if quant_config["method"].lower() != "qlora_4bit":
        raise TrainingSetupError("EXPERIMENT_REQUIRES_QLORA_4BIT")
    compute_dtype = SETUP._dtype(torch, quant_config["compute_dtype"])
    return {
        "trust_remote_code": bool(model_config["trust_remote_code"]),
        "quantization_config": BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_quant_type=quant_config["quant_type"],
            bnb_4bit_use_double_quant=bool(quant_config["double_quantization"]),
            bnb_4bit_compute_dtype=compute_dtype,
        ),
        "device_map": "auto",
        "torch_dtype": SETUP._dtype(torch, model_config["torch_dtype"]),
    }


def _batch(tokenizer: Any, text: str, sequence_length: int, torch: Any) -> dict[str, Any]:
    encoded = tokenizer(text, return_tensors="pt", padding=True, truncation=True, max_length=sequence_length)
    encoded["labels"] = encoded["input_ids"].clone()
    return encoded


def _prompt(tokenizer: Any, row: dict[str, Any]) -> str:
    return format_training_example(tokenizer, row, include_target=False)


def _resource_snapshot(torch: Any) -> dict[str, Any]:
    result: dict[str, Any] = {"gpu": torch.cuda.get_device_name(0) if torch.cuda.is_available() else None}
    if torch.cuda.is_available():
        result.update(
            peak_gpu_allocated_mb=round(torch.cuda.max_memory_allocated() / 1024 / 1024, 2),
            peak_gpu_reserved_mb=round(torch.cuda.max_memory_reserved() / 1024 / 1024, 2),
        )
    try:
        import psutil

        result["process_rss_mb"] = round(psutil.Process().memory_info().rss / 1024 / 1024, 2)
    except ImportError:
        result["process_rss_mb"] = None
    return result


def _load_base(config: dict[str, Any], torch: Any, AutoModelForCausalLM: Any, BitsAndBytesConfig: Any) -> Any:
    return AutoModelForCausalLM.from_pretrained(config["model"]["base_model"], **_model_kwargs(config, torch, BitsAndBytesConfig))


def _evaluate_model(model: Any, tokenizer: Any, row: dict[str, Any], config: dict[str, Any], torch: Any) -> dict[str, Any]:
    model.eval()
    device = next(model.parameters()).device
    encoded = tokenizer(_prompt(tokenizer, row), return_tensors="pt", truncation=True, max_length=int(config["data"]["sequence_length"]))
    encoded = {key: value.to(device) for key, value in encoded.items()}
    if torch.cuda.is_available():
        torch.cuda.reset_peak_memory_stats()
    started = time.perf_counter()
    with torch.inference_mode():
        output = model.generate(**encoded, max_new_tokens=256, do_sample=False, pad_token_id=tokenizer.eos_token_id)
    elapsed_ms = round((time.perf_counter() - started) * 1000, 2)
    generated = tokenizer.decode(output[0][encoded["input_ids"].shape[1] :], skip_special_tokens=True).strip()
    source_ids = {str(citation.get("source_id")) for citation in row.get("citations", []) if citation.get("source_id")}
    source_mentions = sorted(source_id for source_id in source_ids if source_id in generated)
    contract_errors = validate_generated_target(row["task"], generated, source_ids)
    structured = not contract_errors
    return {
        "output_validity": bool(generated),
        "structured_output_success": structured,
        "evidence_grounding_source_mentions": source_mentions,
        "evidence_grounding": "STRUCTURAL_SOURCE_ID_SUBSET" if not contract_errors else "CONTRACT_FAILED",
        "citation_correctness": "STRUCTURAL_SOURCE_ID_SUBSET" if not contract_errors else "CONTRACT_FAILED",
        "contract_errors": contract_errors,
        "unsupported_claim_rate": "NOT_SCORED_REQUIRES_HUMAN_OR_REFERENCE_LABELS",
        "root_cause_quality": "NOT_SCORED_REQUIRES_HUMAN_RUBRIC",
        "recommendation_quality": "NOT_APPLICABLE_TEST_TASK_ROOT_CAUSE_ANALYSIS",
        "uncertainty_handling": "NOT_SCORED_REQUIRES_HUMAN_RUBRIC",
        "latency_ms": elapsed_ms,
        "resource_usage": _resource_snapshot(torch),
        "output": generated,
    }


def _evaluate_base_and_adapter(config: dict[str, Any], test_row: dict[str, Any], checkpoint: Path, torch: Any, AutoModelForCausalLM: Any, AutoTokenizer: Any, BitsAndBytesConfig: Any, PeftModel: Any) -> tuple[dict[str, Any], dict[str, Any]]:
    tokenizer = AutoTokenizer.from_pretrained(config["model"]["base_model"], trust_remote_code=bool(config["model"]["trust_remote_code"]))
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    if torch.cuda.is_available():
        torch.cuda.reset_peak_memory_stats()
    base = _load_base(config, torch, AutoModelForCausalLM, BitsAndBytesConfig)
    base_result = _evaluate_model(base, tokenizer, test_row, config, torch)
    del base
    torch.cuda.empty_cache()
    fine = _load_base(config, torch, AutoModelForCausalLM, BitsAndBytesConfig)
    fine = PeftModel.from_pretrained(fine, checkpoint, is_trainable=False)
    fine_result = _evaluate_model(fine, tokenizer, test_row, config, torch)
    del fine
    torch.cuda.empty_cache()
    return base_result, fine_result


def _write_report(result: dict[str, Any], report_path: Path) -> None:
    base = result["base_model_result"]
    fine = result["fine_tuned_model_result"]
    report = f"""# Fine-Tuning Experiment {result['experiment_name']}

Status: **EXPERIMENTAL**. This run uses a small approved dataset and is not evidence of meaningful generalization.

## Configuration

- Base model: `{result['base_model']}`
- Dataset: `{result['dataset_version']}`
- Experiment: `{result['experiment_name']}`
- Split counts: train `{result['split_counts']['train']}`, validation `{result['split_counts']['validation']}`, test `{result['split_counts']['test']}`
- Quantization: `{result['quantization']}` (`{result['quant_type']}`, double quantization `{result['double_quantization']}`, compute dtype `{result['compute_dtype']}`)
- LoRA: rank `{result['lora']['rank']}`, alpha `{result['lora']['alpha']}`, dropout `{result['lora']['dropout']}`, targets `{', '.join(result['lora']['target_modules'])}`
- Learning rate: `{result['learning_rate']}`
- Batch size / gradient accumulation: `{result['batch_size']} / {result['gradient_accumulation_steps']}`
- Sequence length: `{result['sequence_length']}`
- Epochs: `{result['epochs']}`
- Seed: `{result['seed']}`
- Hardware: `{result['hardware']}`
- Training duration: `{result['training_duration_seconds']} seconds`
- Optimizer updates: `{result['optimizer_updates']}`
- Adapter checkpoint: `{result['checkpoint']}`

## Training Result

- Training loss by epoch: `{result['training_loss_by_epoch']}`
- Validation loss by epoch: `{result['validation_loss_by_epoch']}`
- Best validation loss: `{result['best_validation_loss']}`
- Checkpoint SHA-256: `{result['checkpoint_sha256']}`
- Dataset digest unchanged: `{result['dataset_digest_before'] == result['dataset_digest_after']}`

## Held-Out Test Comparison

The same unchanged TEST row, prompt construction, decoding settings, and evaluation checks were used for both models. Human-rubric metrics are reported as not scored rather than inferred.

| Metric | Base Qwen | Fine-tuned Qwen |
|---|---:|---:|
| Output validity | `{base['output_validity']}` | `{fine['output_validity']}` |
| Structured output success | `{base['structured_output_success']}` | `{fine['structured_output_success']}` |
| Evidence grounding check | `{base['evidence_grounding']}` | `{fine['evidence_grounding']}` |
| Citation correctness | `{base['citation_correctness']}` | `{fine['citation_correctness']}` |
| Unsupported-claim rate | `{base['unsupported_claim_rate']}` | `{fine['unsupported_claim_rate']}` |
| Root-cause quality | `{base['root_cause_quality']}` | `{fine['root_cause_quality']}` |
| Recommendation quality | `{base['recommendation_quality']}` | `{fine['recommendation_quality']}` |
| Uncertainty handling | `{base['uncertainty_handling']}` | `{fine['uncertainty_handling']}` |
| Latency (ms) | `{base['latency_ms']}` | `{fine['latency_ms']}` |

### Outputs

Base output:

```text
{base['output']}
```

Fine-tuned output:

```text
{fine['output']}
```

## Resource Usage

- Base inference: `{json.dumps(base['resource_usage'], sort_keys=True)}`
- Fine-tuned inference: `{json.dumps(fine['resource_usage'], sort_keys=True)}`
- Training: `{json.dumps(result['training_resource_usage'], sort_keys=True)}`

## Limitations and Decision

- One TEST example cannot support a statistical quality or generalization claim.
- Root-cause quality, recommendation quality, citation correctness, and unsupported-claim rate require the existing human/reference evaluation rubric and are not fabricated here.
- The held-out TEST contains one example, so grounding and quality checks remain limited and are not production evaluation.
- This adapter is not deployed and does not replace the base model.
- Pipeline readiness for a larger dataset: **TECHNICALLY READY FOR A CONTROLLED LARGER RUN; QUALITY READINESS NOT ESTABLISHED**.
"""
    report_path.write_text(report, encoding="utf-8")


def run_experiment(config_path: Path) -> dict[str, Any]:
    config = SETUP.load_config(config_path)
    train, validation, test, manifest, digest_before = _validate_preflight(config)
    output_dir = Path(config["experiment"]["output_dir"])
    if output_dir.exists() and any(output_dir.iterdir()):
        raise TrainingSetupError(f"EXPERIMENT_OUTPUT_EXISTS:{output_dir}")
    output_dir.mkdir(parents=True, exist_ok=False)
    torch, LoraConfig, PeftModel, get_peft_model, prepare_model_for_kbit_training, AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig = _imports()
    SETUP._seed_everything(int(config["experiment"]["seed"]), torch)
    model_config = config["model"]
    sequence_length = int(config["data"]["sequence_length"])
    tokenizer = AutoTokenizer.from_pretrained(model_config["base_model"], trust_remote_code=bool(model_config["trust_remote_code"]))
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    model = _load_base(config, torch, AutoModelForCausalLM, BitsAndBytesConfig)
    model = prepare_model_for_kbit_training(model)
    model = get_peft_model(model, LoraConfig(
        r=int(config["lora"]["rank"]),
        lora_alpha=int(config["lora"]["alpha"]),
        lora_dropout=float(config["lora"]["dropout"]),
        target_modules=list(config["lora"]["target_modules"]),
        bias="none",
        task_type="CAUSAL_LM",
    ))
    if bool(config["training"]["gradient_checkpointing"]):
        model.gradient_checkpointing_enable()
        model.config.use_cache = False
    optimizer = torch.optim.AdamW((parameter for parameter in model.parameters() if parameter.requires_grad), lr=float(config["training"]["learning_rate"]))
    model.train()
    started = time.perf_counter()
    training_losses: list[float] = []
    validation_losses: list[float] = []
    optimizer_updates = 0
    grad_accum = int(config["training"]["gradient_accumulation_steps"])
    optimizer.zero_grad(set_to_none=True)
    for epoch in range(int(config["training"]["epochs"])):
        total = 0.0
        for row in train:
            batch = SETUP._on_model_device(_batch(tokenizer, format_training_example(tokenizer, row), sequence_length, torch), model)
            result = model(**batch)
            loss = result.loss
            if loss is None or not torch.isfinite(loss):
                raise TrainingSetupError("TRAINING_LOSS_INVALID")
            total += float(loss.detach().cpu())
            (loss / grad_accum).backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), float(config["training"]["max_grad_norm"]))
        optimizer.step()
        optimizer_updates += 1
        optimizer.zero_grad(set_to_none=True)
        training_loss = total / len(train)
        model.eval()
        with torch.no_grad():
            validation_loss = sum(float(model(**SETUP._on_model_device(_batch(tokenizer, format_training_example(tokenizer, row), sequence_length, torch), model)).loss.detach().cpu()) for row in validation) / len(validation)
        model.train()
        training_losses.append(training_loss)
        validation_losses.append(validation_loss)
    duration = round(time.perf_counter() - started, 2)
    checkpoint = output_dir / "checkpoint-final"
    model.save_pretrained(checkpoint)
    tokenizer.save_pretrained(checkpoint)
    torch.save(optimizer.state_dict(), checkpoint / "optimizer.pt")
    (checkpoint / "training-config.toml").write_text(config_path.read_text(encoding="utf-8"), encoding="utf-8")
    del model
    torch.cuda.empty_cache()
    base_result, fine_result = _evaluate_base_and_adapter(config, test[0], checkpoint, torch, AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig, PeftModel)
    digest_after = _dataset_digest(Path(config["experiment"]["dataset_dir"]))
    result = {
        "base_model": model_config["base_model"],
        "experiment_name": config["experiment"].get("name", "UNNAMED"),
        "dataset_version": manifest["dataset_version"],
        "split_counts": {"train": len(train), "validation": len(validation), "test": len(test)},
        "quantization": config["quantization"]["method"],
        "quant_type": config["quantization"]["quant_type"],
        "double_quantization": config["quantization"]["double_quantization"],
        "compute_dtype": config["quantization"]["compute_dtype"],
        "lora": config["lora"],
        "learning_rate": config["training"]["learning_rate"],
        "batch_size": config["training"]["batch_size"],
        "gradient_accumulation_steps": grad_accum,
        "sequence_length": sequence_length,
        "epochs": config["training"]["epochs"],
        "seed": config["experiment"]["seed"],
        "hardware": torch.cuda.get_device_name(0),
        "training_duration_seconds": duration,
        "optimizer_updates": optimizer_updates,
        "training_loss_by_epoch": training_losses,
        "validation_loss_by_epoch": validation_losses,
        "best_validation_loss": min(validation_losses),
        "checkpoint": str(checkpoint),
        "checkpoint_sha256": _sha256_tree(checkpoint),
        "dataset_digest_before": digest_before,
        "dataset_digest_after": digest_after,
        "training_resource_usage": _resource_snapshot(torch),
        "base_model_result": base_result,
        "fine_tuned_model_result": fine_result,
    }
    (output_dir / "experiment-results.json").write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    report_path = ROOT / config["experiment"].get("report_path", "FINE_TUNING_EXPERIMENT.md")
    _write_report(result, report_path)
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description="Run exactly one Qwen QLoRA experiment.")
    parser.add_argument("--config", type=Path, required=True)
    args = parser.parse_args()
    try:
        result = run_experiment(args.config)
        print(json.dumps(result, indent=2, sort_keys=True))
        return 0
    except TrainingSetupError as exc:
        print(f"EXPERIMENT_BLOCKED: {exc}", file=sys.stderr)
        return 3


if __name__ == "__main__":
    raise SystemExit(main())

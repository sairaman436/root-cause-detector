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

from contracts.dataset_v03_contract import (
    format_inference_example,
    format_inference_repair_example,
    format_training_example,
    validate_generated_target,
    validate_record,
    v03_json_schema,
)


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


def _load_rows(dataset_dir: Path, names: list[str], split: str) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for name in names:
        for row in SETUP._read_jsonl(dataset_dir / name):
            if row.get("split") == split:
                rows.append(row)
    return rows


def _load_split(config: dict[str, Any], split: str) -> list[dict[str, Any]]:
    dataset_dir = Path(config["experiment"]["dataset_dir"])
    names = config["data"].get(f"{split}_files") or config["data"].get("train_files", [])
    return _load_rows(dataset_dir, names, split)


def _load_holdout(config: dict[str, Any]) -> tuple[list[dict[str, Any]], dict[str, Any], str]:
    holdout_config = config.get("holdout", {})
    holdout_dir = Path(holdout_config["directory"])
    manifest_path = holdout_dir / "manifest.json"
    if not manifest_path.exists():
        raise TrainingSetupError(f"HOLDOUT_MANIFEST_MISSING:{manifest_path}")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    expected_version = holdout_config["evaluation_set_version"]
    if manifest.get("evaluation_set_version") != expected_version:
        raise TrainingSetupError("HOLDOUT_VERSION_MISMATCH")
    if manifest.get("immutable") is not True or manifest.get("training_data_modified") is not False:
        raise TrainingSetupError("HOLDOUT_IMMUTABILITY_GATE_FAILED")
    names = holdout_config.get("files") or [file.name for file in sorted(holdout_dir.glob("*.jsonl"))]
    rows = _load_rows(holdout_dir, names, "test")
    if len(rows) != int(holdout_config["expected_examples"]):
        raise TrainingSetupError(f"HOLDOUT_EXAMPLE_COUNT_MISMATCH:{len(rows)}")
    return rows, manifest, _sha256_tree(holdout_dir)


def _validate_preflight(config: dict[str, Any]) -> tuple[list[dict[str, Any]], list[dict[str, Any]], list[dict[str, Any]], dict[str, Any], dict[str, Any], str, str]:
    train, validation, manifest = SETUP.validate_dataset(config)
    dataset_test = _load_split(config, "test")
    holdout, holdout_manifest, holdout_digest = _load_holdout(config)
    sequence_length = int(config["data"]["sequence_length"])
    expected_counts = config.get("preflight", {})
    if len(train) != int(expected_counts.get("train_examples", len(train))) or len(validation) != int(expected_counts.get("validation_examples", len(validation))):
        raise TrainingSetupError("DATASET_SPLIT_COUNT_GATE_FAILED")
    if len(dataset_test) != int(expected_counts.get("dataset_test_examples", len(dataset_test))):
        raise TrainingSetupError("DATASET_TEST_SPLIT_COUNT_GATE_FAILED")
    if holdout_manifest.get("total_test_examples") != len(holdout):
        raise TrainingSetupError("HOLDOUT_MANIFEST_COUNT_MISMATCH")
    for key in ("evaluation_version", "rubric_version", "output_contract_version"):
        if holdout_manifest.get(key) != expected_counts.get(key):
            raise TrainingSetupError(f"HOLDOUT_{key.upper()}_MISMATCH")
    if manifest.get("structured_targets_validated") is not True:
        raise TrainingSetupError("DATASET_STRUCTURED_TARGET_GATE_FAILED")
    if manifest.get("citation_context_validated") is not True:
        raise TrainingSetupError("DATASET_CITATION_CONTEXT_GATE_FAILED")
    for row in train + validation + dataset_test + holdout:
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
    if not holdout:
        raise TrainingSetupError("HOLDOUT_REQUIRES_EXAMPLES")
    dataset_rows = train + validation + dataset_test
    if len({row.get("scenario_group") for row in dataset_rows}) != len(dataset_rows):
        raise TrainingSetupError("DATASET_SCENARIO_LEAKAGE")
    if len({row.get("scenario_group") for row in holdout}) != len(holdout):
        raise TrainingSetupError("HOLDOUT_SCENARIO_LEAKAGE")
    training_rows = train + validation
    if {row.get("scenario_group") for row in training_rows} & {row.get("scenario_group") for row in holdout}:
        raise TrainingSetupError("TRAINING_OR_DATASET_TEST_CONTAMINATES_HOLDOUT")
    if {row.get("example_id") for row in training_rows} & {row.get("example_id") for row in holdout}:
        raise TrainingSetupError("EXAMPLE_ID_CONTAMINATES_HOLDOUT")
    if any(row.get("split") != "train" for row in train):
        raise TrainingSetupError("TRAIN_SPLIT_INVALID")
    if any(row.get("split") != "validation" for row in validation):
        raise TrainingSetupError("VALIDATION_SPLIT_INVALID")
    if any(row.get("split") != "test" for row in dataset_test):
        raise TrainingSetupError("TEST_SPLIT_INVALID")
    dataset_dir = Path(config["experiment"]["dataset_dir"])
    return train, validation, holdout, manifest, holdout_manifest, _dataset_digest(dataset_dir), holdout_digest


def _imports() -> tuple[Any, Any, Any, Any, Any, Any, Any, Any]:
    try:
        import torch
        import bitsandbytes  # noqa: F401
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
    return format_inference_example(tokenizer, row)


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


def _constrained_generator(model: Any, tokenizer: Any) -> tuple[Any, Any]:
    """Create the mandatory Outlines Transformers JSON-schema generator."""

    try:
        import outlines
        from outlines.models.transformers import from_transformers
    except ImportError as exc:
        raise TrainingSetupError(f"CONSTRAINED_DECODING_DEPENDENCY_MISSING:{exc.name}") from exc
    return from_transformers(model, tokenizer), outlines.json_schema


def _evaluate_model(
    model: Any,
    tokenizer: Any,
    row: dict[str, Any],
    config: dict[str, Any],
    torch: Any,
    constrained_generator: Any,
    schema_factory: Any,
) -> dict[str, Any]:
    model.eval()
    device = next(model.parameters()).device
    sequence_length = int(config["data"]["sequence_length"])
    if torch.cuda.is_available():
        torch.cuda.reset_peak_memory_stats()
    started = time.perf_counter()
    source_ids = {str(citation.get("source_id")) for citation in row.get("citations", []) if citation.get("source_id")}
    constrained_output_type = schema_factory(v03_json_schema(row["task"], source_ids))
    prompt = _prompt(tokenizer, row)
    generated = ""
    contract_errors: list[str] = []
    repair_attempts = 0
    for attempt in range(2):
        encoded = tokenizer(prompt, return_tensors="pt", truncation=False)
        if encoded["input_ids"].shape[1] > sequence_length:
            raise ValueError(
                f"INFERENCE_PROMPT_EXCEEDS_SEQUENCE_LENGTH:{encoded['input_ids'].shape[1]}>{sequence_length}"
            )
        prompt_length = encoded["input_ids"].shape[1]
        encoded = {key: value.to(device) for key, value in encoded.items()}
        del encoded, prompt_length
        generated = str(
            constrained_generator(
                prompt,
                constrained_output_type,
                max_new_tokens=512,
                do_sample=False,
                eos_token_id=tokenizer.eos_token_id,
                pad_token_id=tokenizer.pad_token_id or tokenizer.eos_token_id,
            )
        ).strip()
        contract_errors = validate_generated_target(row["task"], generated, source_ids)
        if not contract_errors:
            break
        if attempt == 0:
            repair_attempts = 1
            prompt = format_inference_repair_example(tokenizer, row, contract_errors, generated)
    elapsed_ms = round((time.perf_counter() - started) * 1000, 2)
    source_mentions = sorted(source_id for source_id in source_ids if source_id in generated)
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
        "task": row["task"],
        "scenario_group": row["scenario_group"],
        "repair_attempts": repair_attempts,
    }


def _aggregate_evaluation(results: list[dict[str, Any]]) -> dict[str, Any]:
    contract_passes = sum(result["structured_output_success"] for result in results)
    latencies = [result["latency_ms"] for result in results]
    return {
        "examples": len(results),
        "task_counts": {task: sum(result["task"] == task for result in results) for task in {
            "root-cause-analysis",
            "recommendation-generation",
            "rag-grounded-responses",
        }},
        "structured_output_successes": contract_passes,
        "structured_output_success_rate": round(contract_passes / len(results), 4) if results else 0.0,
        "citation_contract_successes": sum(result["citation_correctness"] != "CONTRACT_FAILED" for result in results),
        "evidence_grounding_successes": sum(result["evidence_grounding"] != "CONTRACT_FAILED" for result in results),
        "average_latency_ms": round(sum(latencies) / len(latencies), 2) if latencies else None,
        "min_latency_ms": min(latencies) if latencies else None,
        "max_latency_ms": max(latencies) if latencies else None,
        "quality_metrics": "PENDING_HUMAN_QUALITY_RUBRIC_SCORING",
        "recommendation_quality": "PENDING_HUMAN_QUALITY_RUBRIC_SCORING",
    }


def _classify_result(base: dict[str, Any], fine: dict[str, Any]) -> str:
    base_rate = base["aggregate"]["structured_output_success_rate"]
    fine_rate = fine["aggregate"]["structured_output_success_rate"]
    if fine_rate > base_rate:
        return "IMPROVED"
    if fine_rate < base_rate:
        return "REGRESSED"
    base_latency = base["aggregate"]["average_latency_ms"]
    fine_latency = fine["aggregate"]["average_latency_ms"]
    if base_latency and fine_latency and fine_latency > base_latency * 1.10:
        return "REGRESSED"
    return "NO_SIGNIFICANT_CHANGE"


def _evaluate_base_and_adapter(config: dict[str, Any], test_rows: list[dict[str, Any]], checkpoint: Path, torch: Any, AutoModelForCausalLM: Any, AutoTokenizer: Any, BitsAndBytesConfig: Any, PeftModel: Any) -> tuple[dict[str, Any], dict[str, Any]]:
    tokenizer = AutoTokenizer.from_pretrained(config["model"]["base_model"], trust_remote_code=bool(config["model"]["trust_remote_code"]))
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    if torch.cuda.is_available():
        torch.cuda.reset_peak_memory_stats()
    base = _load_base(config, torch, AutoModelForCausalLM, BitsAndBytesConfig)
    base_generator, schema_factory = _constrained_generator(base, tokenizer)
    base_results = [_evaluate_model(base, tokenizer, row, config, torch, base_generator, schema_factory) for row in test_rows]
    del base
    torch.cuda.empty_cache()
    fine = _load_base(config, torch, AutoModelForCausalLM, BitsAndBytesConfig)
    fine = PeftModel.from_pretrained(fine, checkpoint, is_trainable=False)
    fine_generator, schema_factory = _constrained_generator(fine, tokenizer)
    fine_results = [_evaluate_model(fine, tokenizer, row, config, torch, fine_generator, schema_factory) for row in test_rows]
    del fine
    torch.cuda.empty_cache()
    return {"per_example": base_results, "aggregate": _aggregate_evaluation(base_results)}, {"per_example": fine_results, "aggregate": _aggregate_evaluation(fine_results)}


def _write_report(result: dict[str, Any], report_path: Path) -> None:
    base = result["base_model_result"]
    fine = result["fine_tuned_model_result"]
    report = f"""# Fine-Tuning Experiment {result['experiment_name']}

Status: **EXPERIMENTAL**. This run uses a small approved dataset and is not evidence of meaningful generalization.

## Configuration

- Base model: `{result['base_model']}`
- Dataset: `{result['dataset_version']}`
- Dataset digest: `{result['dataset_digest_before']}` (unchanged: `{result['dataset_digest_before'] == result['dataset_digest_after']}`)
- Evaluation set: `{result['evaluation_set_version']}`
- Evaluation-set digest: `{result['holdout_digest_before']}` (unchanged: `{result['holdout_digest_before'] == result['holdout_digest_after']}`)
- Evaluation methodology: `{result['evaluation_version']}`
- Human rubric: `{result['rubric_version']}`
- Prompt version: `{result['prompt_version']}`
- Inference configuration: `{json.dumps(result['inference_configuration'], sort_keys=True)}`
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
- Dataset digest before/after: `{result['dataset_digest_before']}` / `{result['dataset_digest_after']}`
- Evaluation-set digest before/after: `{result['holdout_digest_before']}` / `{result['holdout_digest_after']}`
- Checkpoint SHA-256: `{result['checkpoint_sha256']}`
- Dataset digest unchanged: `{result['dataset_digest_before'] == result['dataset_digest_after']}`

## Held-Out Test Comparison

The same unchanged `{result['evaluation_set_version']}` TEST rows, prompt construction, constrained-decoding settings, generation limits, and evaluation checks were used for both models. Human-rubric metrics are reported as pending rather than inferred.

| Metric | Base Qwen | Fine-tuned Qwen |
|---|---:|---:|
| Structured output success | `{base['aggregate']['structured_output_successes']}/{base['aggregate']['examples']}` | `{fine['aggregate']['structured_output_successes']}/{fine['aggregate']['examples']}` |
| Citation/source-ID contract | `{base['aggregate']['citation_contract_successes']}/{base['aggregate']['examples']}` | `{fine['aggregate']['citation_contract_successes']}/{fine['aggregate']['examples']}` |
| Evidence grounding check | `{base['aggregate']['evidence_grounding_successes']}/{base['aggregate']['examples']}` | `{fine['aggregate']['evidence_grounding_successes']}/{fine['aggregate']['examples']}` |
| Unsupported-claim rate | `PENDING HUMAN SCORING` | `PENDING HUMAN SCORING` |
| Root-cause quality | `PENDING HUMAN SCORING` | `PENDING HUMAN SCORING` |
| Recommendation quality | `PENDING HUMAN SCORING` | `PENDING HUMAN SCORING` |
| RAG response quality | `PENDING HUMAN SCORING` | `PENDING HUMAN SCORING` |
| Uncertainty handling | `PENDING HUMAN SCORING` | `PENDING HUMAN SCORING` |
| Average latency (ms) | `{base['aggregate']['average_latency_ms']}` | `{fine['aggregate']['average_latency_ms']}` |

### Per-example Structural Results

| Task | Scenario | Base contract | Fine-tuned contract | Base latency (ms) | Fine-tuned latency (ms) |
|---|---|---|---|---:|---:|
{chr(10).join(f"| `{b['task']}` | `{b['scenario_group']}` | `{b['structured_output_success']}` | `{f['structured_output_success']}` | `{b['latency_ms']}` | `{f['latency_ms']}` |" for b, f in zip(base['per_example'], fine['per_example']))}

## Resource Usage

- Base inference by test example: `{json.dumps([item['resource_usage'] for item in base['per_example']], sort_keys=True)}`
- Fine-tuned inference by test example: `{json.dumps([item['resource_usage'] for item in fine['per_example']], sort_keys=True)}`
- Training: `{json.dumps(result['training_resource_usage'], sort_keys=True)}`

## Limitations and Decision

- The `{result['evaluation_set_version']}` TEST contains four examples: one root-cause, one recommendation, and two RAG examples. This remains a small sample and cannot support a statistical generalization claim.
- Cross-test overlap disclosure: `{len(result['dataset_test_holdout_overlap'])}` held-out example IDs also appear in the immutable dataset-v0.3 TEST split; none overlap TRAIN or VALIDATION. This is a test-set independence limitation, not training contamination.
- Human quality scores for both model outputs were not fabricated. The existing human baseline report records zero completed reviewer scores, so rubric dimensions remain pending.
- Root-cause quality, recommendation quality, RAG/evidence quality, uncertainty handling, practical usefulness, and unsupported-claim rate require authenticated human scoring under `HUMAN-QUALITY-RUBRIC@1.0.0`.
- This adapter is not deployed and does not replace the base model.
- Pipeline readiness for a larger dataset: **TECHNICALLY READY FOR A CONTROLLED LARGER RUN; QUALITY READINESS NOT ESTABLISHED**.

## Final Classification

**{result['classification']}**

This classification is based on available structural contract and performance evidence only. It is not a claim of statistical quality improvement because the test set is small and human quality metrics are pending.
"""
    report_path.write_text(report, encoding="utf-8")


def run_experiment(config_path: Path) -> dict[str, Any]:
    config = SETUP.load_config(config_path)
    train, validation, test, manifest, holdout_manifest, digest_before, holdout_digest_before = _validate_preflight(config)
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
        micro_steps = 0
        for row_index, row in enumerate(train, 1):
            batch = SETUP._on_model_device(_batch(tokenizer, format_training_example(tokenizer, row), sequence_length, torch), model)
            result = model(**batch)
            loss = result.loss
            if loss is None or not torch.isfinite(loss):
                raise TrainingSetupError("TRAINING_LOSS_INVALID")
            total += float(loss.detach().cpu())
            (loss / grad_accum).backward()
            micro_steps += 1
            if micro_steps % grad_accum == 0 or row_index == len(train):
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
    base_result, fine_result = _evaluate_base_and_adapter(config, test, checkpoint, torch, AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig, PeftModel)
    digest_after = _dataset_digest(Path(config["experiment"]["dataset_dir"]))
    holdout_digest_after = _sha256_tree(Path(config["holdout"]["directory"]))
    dataset_test_rows = _load_split(config, "test")
    dataset_test_holdout_overlap = sorted(
        {row.get("example_id") for row in dataset_test_rows}
        & {row.get("example_id") for row in test}
    )
    result = {
        "base_model": model_config["base_model"],
        "experiment_name": config["experiment"].get("name", "UNNAMED"),
        "dataset_version": manifest["dataset_version"],
        "split_counts": {"train": len(train), "validation": len(validation), "test": len(test)},
        "dataset_digest_before": digest_before,
        "dataset_digest_after": digest_after,
        "holdout_digest_before": holdout_digest_before,
        "holdout_digest_after": holdout_digest_after,
        "dataset_test_holdout_overlap": dataset_test_holdout_overlap,
        "evaluation_set_version": holdout_manifest["evaluation_set_version"],
        "evaluation_version": holdout_manifest["evaluation_version"],
        "rubric_version": holdout_manifest["rubric_version"],
        "prompt_version": config["inference"]["prompt_version"],
        "inference_configuration": config["inference"],
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
        "training_resource_usage": _resource_snapshot(torch),
        "base_model_result": base_result,
        "fine_tuned_model_result": fine_result,
        "classification": _classify_result(base_result, fine_result),
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

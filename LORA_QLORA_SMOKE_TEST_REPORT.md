# LoRA/QLoRA Smoke-Test Report

## Selected Base Model

`Qwen/Qwen2.5-0.5B-Instruct` is selected for setup validation because the repository already uses Qwen as its default local model family and this profile targets the standard Transformers causal-language-model and PEFT contract. The existing SONAR adapter remains experimental and is not selected for fine-tuning setup until its custom checkpoint is verified against PEFT.

## Dataset Compatibility

The dataset schema is compatible in principle with supervised causal-language-model formatting: each JSONL record has an input, validated output, citations, provenance, dataset version, and split. The current `dataset-v0.1` manifest contains zero accepted examples and zero train/validation/test rows, so it is not executable as a training dataset.

## Smoke-Test Result

`BLOCKED` before tokenizer/model loading with `DATASET_EMPTY_NO_APPROVED_EXAMPLES`.

No model weights were downloaded. No training step, checkpoint, or model artifact was produced. This is an intentional safety stop, not a fabricated pass.

## Reproducible Command

```text
python -m pip install -e ".\\ml-platform\\training-pipelines[training]"
python .\\ml-platform\\training-pipelines\\run_lora_qlora_smoke_test.py --config .\\ml-platform\\training-pipelines\\configs\\qwen2.5-0.5b-instruct-lora-qlora.toml --smoke
```

## Remaining Blockers

1. Produce a governed non-empty `dataset-v0.1` export containing approved or validated human-corrected examples in both train and validation splits.
2. Run the smoke test on a CUDA host with the isolated training dependencies installed; 4-bit QLoRA is intentionally rejected on CPU.
3. Verify the selected Qwen model license, tokenizer revision, GPU memory budget, and PEFT target modules before any long run.

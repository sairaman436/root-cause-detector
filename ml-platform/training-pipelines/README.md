# Training Pipelines

## Purpose

Defines the isolated, reproducible worker boundary for future LoRA and QLoRA adapter training.

## Why It Exists

Training requires GPU-specific dependencies, tokenizer/model compatibility checks, checkpoint recovery, validation-loss tracking, and experiment lineage that must remain separate from application runtime services.

## Architecture Fit

The pipeline consumes only the approved `dataset-v0.1` JSONL artifacts produced by `build_dataset_v01.py`. It complements the Spring `training` and `finetuning` metadata modules: those modules govern authorization and lifecycle records, while this boundary will execute a worker job when a non-empty approved dataset exists.

## Selected Setup

The only active reproducible profile is `configs/qwen2.5-0.5b-instruct-lora-qlora.toml`. It targets `Qwen/Qwen2.5-0.5B-Instruct`, uses LoRA adapter injection, and enables 4-bit NF4 QLoRA on CUDA.

## Smoke Test

Install isolated training dependencies, then run:

```text
python -m pip install -e ".\\ml-platform\\training-pipelines[training]"
python .\\ml-platform\\training-pipelines\\run_lora_qlora_smoke_test.py --config .\\ml-platform\\training-pipelines\\configs\\qwen2.5-0.5b-instruct-lora-qlora.toml --smoke
```

The command refuses to run without `--smoke`, requires non-empty train and validation splits, loads the tokenizer and base model, injects LoRA/QLoRA adapters, performs one forward/backward optimizer step, records validation loss, and writes a checkpoint with experiment metadata. It does not run a full training loop.

## Configuration

All training controls are in the TOML profile: base model, dataset version, sequence length, batch size, gradient accumulation, learning rate, epochs, LoRA rank/alpha/dropout, quantization, output directory, seed, checkpoint cadence, and resume path.

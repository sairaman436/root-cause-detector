# ML GPU Readiness Report

## Environment Changes

- The application environment was not changed. Global Python 3.14 still has the previously observed CPU-only PyTorch installation.
- Created the ignored `.venv-training-gpu` environment with Python 3.13.2 for training-only validation.
- Installed and verified `torch 2.11.0+cu126`, `bitsandbytes 0.50.0`, `peft 0.20.0`, `transformers 5.15.0`, `accelerate 1.14.0`, and `safetensors 0.8.0` in that isolated environment.
- Added `ml-platform/training-pipelines/configs/qwen2.5-0.5b-instruct-lora-qlora-dev-synthetic.toml`. It is separate from the production profile and points only to `dataset-v0.1-dev-synthetic`.
- Added `.venv-training-gpu/` to `.gitignore` so model caches, packages, and smoke artifacts cannot be committed.
- The production profile and production `dataset-v0.1` were not modified. No full training job was run.

## GPU/CUDA Status

- NVIDIA device detected: RTX 3050 6GB Laptop GPU, 6144 MiB.
- NVIDIA driver: `610.62`.
- Isolated PyTorch: `2.11.0+cu126` with CUDA `12.6`.
- `torch.cuda.is_available()`: `True`.
- CUDA device count: `1`.
- CUDA tensor operation succeeded; `[1.0, 2.0] * [1.0, 2.0]` reduced to `5.0` on the GPU.

## bitsandbytes Status

- `bitsandbytes 0.50.0` imports successfully in the isolated environment.
- Its functional module loaded with CUDA-enabled PyTorch.
- No bitsandbytes installation was added to the application runtime.

## LoRA/QLoRA Status

- PEFT loaded successfully and constructed a `LoraConfig`.
- The validated smoke profile uses Qwen `Qwen/Qwen2.5-0.5B-Instruct`, 4-bit NF4 quantization, double quantization, float16 compute, LoRA rank 8, alpha 16, and dropout 0.05.
- The QLoRA path completed model loading and adapter creation on the RTX 3050.

## Smoke-Test Result

- Command: `run_lora_qlora_smoke_test.py --config ml-platform/training-pipelines/configs/qwen2.5-0.5b-instruct-lora-qlora-dev-synthetic.toml --smoke`.
- Dataset: `dataset-v0.1-dev-synthetic` only; 3 train, 3 validation, and 3 test examples. The production dataset was not read for training.
- Result: **PASS**.
- One forward/backward optimizer step completed successfully.
- Validation loss was computed: `5.329551696777344`.
- Smoke-step training loss: `5.865695476531982`.
- A checkpoint was written with adapter weights, tokenizer/configuration metadata, optimizer state, and experiment metadata. The checkpoint SHA-256 was `16dbd9fa3e8de334e92e33f63261efcc5c835ee481073c88986be2b8131188b6`.
- The smoke metadata records `training_performed: false` and `smoke_step_only: true`.
- Targeted repository tests: `6 passed` (`tests/foundation/test_training_setup.py` and `tests/foundation/test_dataset_v01.py`).
- `pip check`: passed. `git diff --check`: passed.

## Remaining Blockers

- Production `dataset-v0.1` remains empty. A real fine-tuning experiment cannot begin until a non-empty, approved production dataset is available.
- No full training run, model evaluation, model promotion, or deployment was performed.
- Triton is not installed in the isolated environment; this emitted a warning during PyTorch import but did not affect the CUDA or QLoRA smoke test. It is not required for this milestone.
- Hugging Face access was unauthenticated and Windows caching used degraded non-symlink behavior. Neither blocked the local smoke test; a configured token and Developer Mode may improve future download/cache behavior.

# ML Pipeline Recovery Report

## Root Cause Of Empty Dataset

The production dataset was empty by design because every governed source stage inspected in the live PostgreSQL instance was empty:

- `learning.learning_records`: `0`
- `learning.human_reviews`: `0`
- `learning.training_candidates`: `0`
- `learning.approval_workflows`: `0`
- `evaluation.evaluation_runs`: `0`
- `evaluation.evaluation_metrics`: `0`

The existing dataset service can register and validate datasets, but there was no approved learning-record export or evaluation-to-human-review-to-dataset materialization source. No approved examples were available to recover, so no real training data was fabricated.

## Dataset Created/Recovered

Created a separate development-only artifact: `dataset-v0.1-dev-synthetic`.

- Synthetic examples: `9`
- Root-cause analysis: `3`
- Recommendation generation: `3`
- RAG-grounded responses: `3`
- Train: `3`
- Validation: `3`
- Test: `3`
- Split leakage: `0`
- Validation failures: `0`
- Training performed: `false`

The seed records are explicitly marked `synthetic` and `development_only`. The validator now rejects synthetic records when building production-labelled dataset versions.

The original production-labelled `dataset-v0.1` remains empty and unchanged.

## Qwen Inference Result

Standalone test through the existing local Ollama runtime:

- Model: `qwen2.5:0.5b`
- Prompt: `Reply with exactly READY.`
- Output: `READY`
- Output validity: passed exact-match check
- Warm elapsed time: `419 ms`
- Ollama load duration: `23.55 ms`
- Prompt evaluation duration: `208.80 ms`
- Generation duration: `29.16 ms`
- Observed Ollama container memory: `299.9 MiB`
- Observed container CPU after request: `101.81%`

The Hugging Face Qwen tokenizer `Qwen/Qwen2.5-0.5B-Instruct` also loaded successfully and encoded the diagnostic prompt into `5` tokens. This does not yet prove that the Ollama quantized artifact and Hugging Face base revision are identical.

**Gate B: PASSED.**

## SONAR Inference Result

Standalone test for `raxtemur/sonar-llm-100m`:

- Checkpoint download: completed locally, approximately `401 MB`
- Local health: degraded because the runtime checkpoint loader was unavailable
- Generation: failed before model load
- Error: `SONAR_DEPENDENCY_MISSING`
- Missing runtime dependencies: `sonar`, `fairseq2`, and `fairseq2n`; the checkpoint's `sonarllm_model` package is present but cannot import without those dependencies.

The checkpoint bundle contains its `sonarllm_model` source, but that source imports the external SONAR and Fairseq2 runtimes. The current Python environment is `3.14.3`; the existing optional dependency declaration excludes `sonar-space` for Python 3.14. No large dependency installation was attempted.

**Gate C: FAILED in the current environment.** This documents an environment/runtime incompatibility, not a claim that the SONAR model is unusable on every supported platform.

## Dependency Issues

- Python: `3.14.3`
- PyTorch: `2.11.0+cpu`
- `torch.cuda.is_available()`: `false`
- `transformers`: installed
- `peft`: installed
- `huggingface_hub`: installed
- `bitsandbytes`: not installed
- `sonar-space`: not installed and excluded by the current Python-version marker
- `sonar`: not installed
- `fairseq2`: not installed
- `fairseq2n`: not installed

## Hardware Assessment

- GPU: NVIDIA GeForce RTX 3050 Laptop GPU
- VRAM: `6 GB`
- RAM: approximately `16 GB`
- NVIDIA driver: `610.62`

The physical GPU is visible to `nvidia-smi`, but the installed PyTorch build is CPU-only. QLoRA cannot be validated or run in the current Python environment. A small Qwen LoRA experiment may be feasible after installing a compatible CUDA PyTorch stack; 4-bit QLoRA requires a compatible `bitsandbytes` build and an explicit memory validation. CPU-only training is not an acceptable controlled path for this milestone.

**Gate D: FAILED.**

## Files Changed

- `ML_PIPELINE_RECOVERY_REPORT.md`
- `ml-platform/training-pipelines/build_dataset_v01.py`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/source/synthetic_seed.jsonl`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/root-cause-analysis.jsonl`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/recommendation-generation.jsonl`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/rag-grounded-responses.jsonl`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/manifest.json`
- `ml-platform/training-pipelines/dataset-v0.1-dev-synthetic/DATASET_QUALITY_REPORT.md`
- `tests/foundation/test_dataset_v01.py`

No production model, test set, prompt, fine-tuning configuration, or application service was changed.

## Tests Performed

- Synthetic dataset materialization and validation: passed, `9` accepted, `0` rejected.
- Development dataset train/validation schema validation: passed, `3/3` rows.
- Leakage validation: passed, `0`.
- `python -m pytest tests/foundation -q`: passed, `10` tests.
- `python -m py_compile ml-platform/training-pipelines/build_dataset_v01.py`: passed.
- Qwen standalone Ollama generation: passed exact `READY` output.
- Qwen Hugging Face tokenizer load: passed.
- SONAR standalone provider health/generation: failed at documented dependency gate.

## Gates

| Gate | Result |
| --- | --- |
| A: Non-empty validated development dataset | **PASSED** using synthetic development-only data |
| B: Qwen local generation | **PASSED** |
| C: SONAR local generation or documented incompatibility | **FAILED** for current environment; incompatibility documented |
| D: Hardware/training configuration validated | **FAILED**; CPU-only PyTorch and missing `bitsandbytes` |

## Exact Remaining Blockers

1. Obtain real human-approved or validated human-corrected records before creating a production training dataset.
2. Run SONAR in a supported Python/Fairseq2/SONAR environment and repeat the same minimal generation check.
3. Install a CUDA-enabled PyTorch build compatible with the host and validate `torch.cuda.is_available()`.
4. Validate a compatible `bitsandbytes` installation before using QLoRA; otherwise use a bounded non-quantized LoRA configuration.
5. Confirm the Hugging Face Qwen revision matches the Ollama Qwen artifact before any training experiment.
6. Re-run the LoRA/QLoRA smoke test on the development dataset only after the hardware stack is repaired.

No fine-tuning was started.

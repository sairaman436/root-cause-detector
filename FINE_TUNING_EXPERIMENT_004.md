# Fine-Tuning Experiment 004

Status: **BLOCKED BEFORE TRAINING**

The experiment did not load a model, start an optimizer, create an adapter, or modify either dataset. A required sequence-length preflight gate failed.

## Configuration

- Base model: `Qwen/Qwen2.5-0.5B-Instruct`
- Dataset: `dataset-v0.3`
- Dataset digest: `5a046ef1f2a76518a790a8fac4245ea2803094a6a1d8f0859a3776d7f6f3586b`
- Dataset splits inspected: TRAIN `23`, VALIDATION `2`; dataset TEST `3` was inspected for isolation only
- Held-out evaluation set: `evaluation-set-v1.0.0`
- Evaluation-set digest: `997b96caad3ab2b2b9cf72c49033715a63be9507b9d96d6165256537bd469d84`
- Held-out coverage: Root Cause `1`, Recommendation `1`, RAG `2`
- Evaluation methodology: `MODEL-EVALUATION-METHODOLOGY@1.0.0`
- Human rubric: `HUMAN-QUALITY-RUBRIC@1.0.0`
- Inference path: Outlines Transformers constrained generation, canonical `dataset-v0.3` schemas, dynamic source-ID constraints, no unconstrained fallback
- Quantization: QLoRA 4-bit NF4 with double quantization, BF16 compute
- LoRA: rank `16`, alpha `32`, dropout `0.05`, target modules `q_proj`, `k_proj`, `v_proj`, `o_proj`
- Sequence limit: `1024`
- Batch size / gradient accumulation: `1 / 8`
- Learning rate: `0.0002`
- Epochs: `1`
- Seed: `42`
- Hardware gate: CUDA available on `NVIDIA GeForce RTX 3050 6GB Laptop GPU`; BF16 supported; bitsandbytes and PEFT imported successfully

## Preflight Results

- Dataset manifest and v0.3 contract: **PASSED**
- Evaluation-set manifest and contract: **PASSED**
- Train/validation/test split counts: **PASSED** (`23 / 2 / 3`)
- Held-out count and task coverage: **PASSED** (`4`; `1 / 1 / 2`)
- TRAIN/VALIDATION to held-out scenario and example isolation: **PASSED**
- GPU/QLoRA dependency readiness: **PASSED**
- Qwen tokenizer loading: **PASSED**
- Full chat-formatted sequence limit: **FAILED**

The v0.3 record-level validator passes, but the complete formatted training example exceeds the configured limit. Five TRAIN examples exceed 1,024 tokens and one VALIDATION example exceeds it:

| Split | Example ID | Task | Formatted tokens |
|---|---|---|---:|
| TRAIN | `0e454d77-aed4-4ce9-a019-c1d92a97aab7` | recommendation-generation | 1,127 |
| TRAIN | `92191669-e7f4-41db-87fc-bc57c7f710cf` | recommendation-generation | 1,105 |
| TRAIN | `de3a0449-b8a8-43b1-b27b-5ffb5e22e3bd` | rag-grounded-responses | 1,427 |
| TRAIN | `43ea9cc6-45cf-43af-b746-e7bca8ff7128` | rag-grounded-responses | 1,428 |
| TRAIN | `6122d108-a6a8-44c7-8351-6d2ab5cef88e` | rag-grounded-responses | 1,426 |
| VALIDATION | `437b4674-d344-40f5-bc47-84488f8792da` | recommendation-generation | 1,120 |

The current training batch path would truncate these formatted examples at 1,024 tokens. Because this can remove input or target content, training was correctly stopped rather than silently truncating.

## Training and Evaluation Results

- Training loss: **not run**
- Validation loss: **not run**
- Optimizer updates: **0**
- Training duration: **not run**
- Adapter/checkpoint: **not created**
- Base-versus-fine-tuned structural comparison: **not run**
- Base-versus-fine-tuned quality comparison: **not run**
- Latency/resource comparison: **not run**
- Human quality scoring: pending; no scores were fabricated

## Test-Set Boundary

Three held-out example IDs also occur in the immutable `dataset-v0.3` TEST split. None occur in TRAIN or VALIDATION, so there is no training contamination. This cross-test overlap remains a limitation for independent final comparison and must be disclosed in any subsequent run.

## Classification

**INVALID**

This is an invalid experiment attempt because the formatted training/validation inputs do not satisfy the configured sequence limit. No training or model comparison evidence exists.

## Remaining Blocker and Next Decision

Create a governed dataset/configuration revision that preserves complete targets and fits the selected Qwen context/training limit, or explicitly raise the sequence limit after a separate RTX 3050 memory-compatibility check. Do not silently truncate the six overflowing examples. Experiment 005 is **not justified** until this preflight issue is resolved and a fresh, complete preflight passes.

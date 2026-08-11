# Fine-Tuning Experiment 001

Status: **EXPERIMENTAL**. This run uses six approved examples and is not evidence of meaningful generalization.

## Configuration

- Base model: `Qwen/Qwen2.5-0.5B-Instruct`
- Dataset: `dataset-v0.1`
- Split counts: train `4`, validation `1`, test `1`
- Quantization: `qlora_4bit` (`nf4`, double quantization `True`, compute dtype `bfloat16`)
- LoRA: rank `16`, alpha `32`, dropout `0.05`, targets `q_proj, k_proj, v_proj, o_proj`
- Learning rate: `0.0002`
- Batch size / gradient accumulation: `1 / 8`
- Sequence length: `1024`
- Epochs: `1`
- Seed: `42`
- Hardware: `NVIDIA GeForce RTX 3050 6GB Laptop GPU`
- Training duration: `5.2 seconds`
- Adapter checkpoint: `artifacts\training\qwen2.5-0.5b-instruct-dataset-v0.1\experiment-001\checkpoint-final`

## Training Result

- Training loss by epoch: `[2.5166046917438507]`
- Validation loss by epoch: `[3.8088181018829346]`
- Best validation loss: `3.8088181018829346`
- Checkpoint SHA-256: `4d3f150c745b8c57cded7ae80d445552d5fd0c35122c516289902d06c6b9385e`
- Dataset digest unchanged: `True`

## Held-Out Test Comparison

The same unchanged TEST row, prompt construction, decoding settings, and evaluation checks were used for both models. Human-rubric metrics are reported as not scored rather than inferred.

| Metric | Base Qwen | Fine-tuned Qwen |
|---|---:|---:|
| Output validity | `True` | `True` |
| Structured output success | `False` | `False` |
| Evidence grounding check | `NO_SOURCE_ID_MENTION` | `NO_SOURCE_ID_MENTION` |
| Citation correctness | `NOT_SCORED_REQUIRES_SOURCE_VERIFICATION` | `NOT_SCORED_REQUIRES_SOURCE_VERIFICATION` |
| Unsupported-claim rate | `NOT_SCORED_REQUIRES_HUMAN_OR_REFERENCE_LABELS` | `NOT_SCORED_REQUIRES_HUMAN_OR_REFERENCE_LABELS` |
| Root-cause quality | `NOT_SCORED_REQUIRES_HUMAN_RUBRIC` | `NOT_SCORED_REQUIRES_HUMAN_RUBRIC` |
| Recommendation quality | `NOT_APPLICABLE_TEST_TASK_ROOT_CAUSE_ANALYSIS` | `NOT_APPLICABLE_TEST_TASK_ROOT_CAUSE_ANALYSIS` |
| Uncertainty handling | `NOT_SCORED_REQUIRES_HUMAN_RUBRIC` | `NOT_SCORED_REQUIRES_HUMAN_RUBRIC` |
| Latency (ms) | `13301.92` | `14840.84` |

### Outputs

Base output:

```text
Smallholder farmers often rely on irrigation to grow their crops, which can be interrupted by weather events such as droughts or floods. These disruptions can lead to reduced water availability, affecting crop yields and quality. Delayed repair of pumps that handle irrigation systems further exacerbates this issue, as they may not be operational when needed. This combination of factors reduces the overall reliability of agricultural operations, making it difficult for smallholder farmers to maintain consistent crop production.
```

Fine-tuned output:

```text
Smallholder farmers often rely on irrigation to grow their crops, which can be interrupted by weather events such as droughts or floods. This disruption can lead to reduced yields and decreased crop quality, resulting in lower income and food security. Delayed pump repair due to maintenance issues also impacts crop availability, potentially leading to reduced crop yield and quality. These factors combined can significantly impact the reliability of agricultural output, making it challenging for smallholder farmers to meet their food needs effectively.
```

## Resource Usage

- Base inference: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1262.44, "peak_gpu_reserved_mb": 1568.0, "process_rss_mb": 1783.05}`
- Fine-tuned inference: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1270.78, "peak_gpu_reserved_mb": 1580.0, "process_rss_mb": 1702.27}`
- Training: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1270.78, "peak_gpu_reserved_mb": 1580.0, "process_rss_mb": 1647.86}`

## Limitations and Decision

- One TEST example cannot support a statistical quality or generalization claim.
- Root-cause quality, recommendation quality, citation correctness, and unsupported-claim rate require the existing human/reference evaluation rubric and are not fabricated here.
- The current training formatter follows the existing infrastructure and does not inject the row's citation context into the user prompt; grounding is therefore a limited smoke check, not a production evaluation.
- This adapter is not deployed and does not replace the base model.
- Pipeline readiness for a larger dataset: **TECHNICALLY READY FOR A CONTROLLED LARGER RUN; QUALITY READINESS NOT ESTABLISHED**.

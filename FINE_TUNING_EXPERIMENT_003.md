# Fine-Tuning Experiment qwen2.5-0.5b-instruct-dataset-v0.3-experiment-003

Status: **EXPERIMENTAL**. This run uses a small approved dataset and is not evidence of meaningful generalization.

## Configuration

- Base model: `Qwen/Qwen2.5-0.5B-Instruct`
- Dataset: `dataset-v0.3`
- Experiment: `qwen2.5-0.5b-instruct-dataset-v0.3-experiment-003`
- Split counts: train `23`, validation `2`, test `3`
- Quantization: `qlora_4bit` (`nf4`, double quantization `True`, compute dtype `bfloat16`)
- LoRA: rank `16`, alpha `32`, dropout `0.05`, targets `q_proj, k_proj, v_proj, o_proj`
- Learning rate: `0.0002`
- Batch size / gradient accumulation: `1 / 8`
- Sequence length: `1024`
- Epochs: `1`
- Seed: `42`
- Hardware: `NVIDIA GeForce RTX 3050 6GB Laptop GPU`
- Training duration: `41.93 seconds`
- Optimizer updates: `3`
- Adapter checkpoint: `artifacts\training\qwen2.5-0.5b-instruct-dataset-v0.3\experiment-003\checkpoint-final`

## Training Result

- Training loss by epoch: `[2.597741557204205]`
- Validation loss by epoch: `[2.066234588623047]`
- Best validation loss: `2.066234588623047`
- Dataset digest before/after: `5a046ef1f2a76518a790a8fac4245ea2803094a6a1d8f0859a3776d7f6f3586b` / `5a046ef1f2a76518a790a8fac4245ea2803094a6a1d8f0859a3776d7f6f3586b`
- Checkpoint SHA-256: `d370dcb6009ce93f387d1a05ccae8b66ff0d7a50beb09b982b7947e752ebca35`
- Dataset digest unchanged: `True`

## Held-Out Test Comparison

The same unchanged three TEST rows, prompt construction, decoding settings, and evaluation checks were used for both models. Human-rubric metrics are reported as not scored rather than inferred.

| Metric | Base Qwen | Fine-tuned Qwen |
|---|---:|---:|
| Structured output success | `0/3` | `0/3` |
| Citation/source-ID contract | `0/3` | `0/3` |
| Evidence grounding check | `0/3` | `0/3` |
| Unsupported-claim rate | `NOT SCORED` | `NOT SCORED` |
| Root-cause quality | `NOT SCORED` | `NOT SCORED` |
| Recommendation quality | `NOT APPLICABLE` | `NOT APPLICABLE` |
| RAG response quality | `NOT SCORED` | `NOT SCORED` |
| Uncertainty handling | `NOT SCORED` | `NOT SCORED` |
| Average latency (ms) | `9411.82` | `19554.86` |

### Per-example Structural Results

| Task | Scenario | Base contract | Fine-tuned contract | Base latency (ms) | Fine-tuned latency (ms) |
|---|---|---|---|---:|---:|
| `root-cause-analysis` | `pilot-v03-exp3-livelihood-market-access-root-001` | `False` | `False` | `9314.71` | `16815.0` |
| `rag-grounded-responses` | `pilot-v03-exp3-livelihood-market-rag-001` | `False` | `False` | `10761.39` | `17450.62` |
| `rag-grounded-responses` | `pilot-v03-expansion-agriculture-rag-001` | `False` | `False` | `8159.35` | `24398.96` |

## Resource Usage

- Base inference by test example: `[{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1274.91, "peak_gpu_reserved_mb": 1646.0, "process_rss_mb": 2410.46}, {"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1274.86, "peak_gpu_reserved_mb": 1646.0, "process_rss_mb": 2402.18}, {"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1274.86, "peak_gpu_reserved_mb": 1646.0, "process_rss_mb": 2402.29}]`
- Fine-tuned inference by test example: `[{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1347.64, "peak_gpu_reserved_mb": 1664.0, "process_rss_mb": 2401.92}, {"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1347.61, "peak_gpu_reserved_mb": 1664.0, "process_rss_mb": 2401.96}, {"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1347.61, "peak_gpu_reserved_mb": 1664.0, "process_rss_mb": 2402.1}]`
- Training: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1347.61, "peak_gpu_reserved_mb": 1664.0, "process_rss_mb": 2345.5}`

## Limitations and Decision

- Three TEST examples cannot support a statistical quality or generalization claim.
- Root-cause quality, recommendation quality, citation correctness, and unsupported-claim rate require the existing human/reference evaluation rubric and are not fabricated here.
- The held-out TEST contains one root-cause example and two RAG examples, with no recommendation example; grounding and quality checks remain limited and are not production evaluation.
- This adapter is not deployed and does not replace the base model.
- Pipeline readiness for a larger dataset: **TECHNICALLY READY FOR A CONTROLLED LARGER RUN; QUALITY READINESS NOT ESTABLISHED**.

## Final Classification

**REGRESSED**

This classification is based only on the structural JSON/citation contract across the three held-out examples. It is not a claim of statistical improvement because the test set is too small and human quality metrics were not scored.

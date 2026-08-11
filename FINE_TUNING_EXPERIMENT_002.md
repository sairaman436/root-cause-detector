# Fine-Tuning Experiment qwen2.5-0.5b-instruct-dataset-v0.2-experiment-002

Status: **EXPERIMENTAL**. This run uses a small approved dataset and is not evidence of meaningful generalization.

## Configuration

- Base model: `Qwen/Qwen2.5-0.5B-Instruct`
- Dataset: `dataset-v0.2`
- Experiment: `qwen2.5-0.5b-instruct-dataset-v0.2-experiment-002`
- Split counts: train `8`, validation `1`, test `1`
- Quantization: `qlora_4bit` (`nf4`, double quantization `True`, compute dtype `bfloat16`)
- LoRA: rank `16`, alpha `32`, dropout `0.05`, targets `q_proj, k_proj, v_proj, o_proj`
- Learning rate: `0.0002`
- Batch size / gradient accumulation: `1 / 8`
- Sequence length: `1024`
- Epochs: `1`
- Seed: `42`
- Hardware: `NVIDIA GeForce RTX 3050 6GB Laptop GPU`
- Training duration: `15.97 seconds`
- Optimizer updates: `1`
- Adapter checkpoint: `artifacts\training\qwen2.5-0.5b-instruct-dataset-v0.2\experiment-002\checkpoint-final`

## Training Result

- Training loss by epoch: `[2.9820496439933777]`
- Validation loss by epoch: `[2.4685072898864746]`
- Best validation loss: `2.4685072898864746`
- Checkpoint SHA-256: `796b3c7f60da6d843ed7406dbf8cfbc842533809ffc56b090d2698a1d20a7d30`
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
| Latency (ms) | `18910.45` | `41662.62` |

### Outputs

Base output:

```text
{
  "summary": "Root causes of reduced crop reliability due to irrigation interruptions and delayed pump repairs among smallholder farmers.",
  "root_causes": [
    {
      "name": "Inadequate infrastructure",
      "impact": "Reduced access to water sources",
      "causes": [
        "Irrigation systems were poorly designed and operated, leading to inadequate water supply."
      ]
    },
    {
      "name": "Poor maintenance practices",
      "impact": "Delayed repair of pumps",
      "causes": [
        "Pump maintenance was neglected, resulting in equipment failure."
      ]
    },
    {
      "name": "Unclear responsibilities",
      "impact": "Inconsistent reporting of pump availability",
      "causes": [
        "Farmers did not receive clear instructions on how to maintain their pumps properly."
      ]
    }
  ],
  "uncertainty": [
    {
      "name": "Inadequacy of data collection methods",
      "impact": "Insufficient information about pump availability and maintenance",
      "causes": [
        "Observations were made through visual inspection and anecdotal reports rather than precise measurements."
      ]
    },
    {
      "name": "Complexity of farming practices
```

Fine-tuned output:

```text
{
  "summary": "Root causes of reduced crop reliability due to irrigation interruptions and delayed pump repairs among smallholder farmers.",
  "root_causes": [
    {
      "name": "Inadequate infrastructure",
      "impact": "Reduced access to water sources",
      "causes": [
        {
          "name": "Irrigation system failure",
          "impact": "Water supply disruption",
          "causes": [
            {
              "name": "Incorrectly designed irrigation systems",
              "impact": "Inadequate water distribution",
              "causes": [
                {
                  "name": "Poorly maintained pumps",
                  "impact": "Pump malfunctions",
                  "causes": [
                    {
                      "name": "Unresponsive pump repair",
                      "impact": "No response from maintenance personnel",
                      "causes": [
                        {
                          "name": "Maintenance staff unavailability",
                          "impact": "Staff unavailable",
                          "causes": [
                            {
                              "name": "Maintenance personnel absence",
                              "impact": "Absence of trained personnel",
                              "causes": []
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
```

## Resource Usage

- Base inference: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1275.22, "peak_gpu_reserved_mb": 1376.0, "process_rss_mb": 1639.94}`
- Fine-tuned inference: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1282.39, "peak_gpu_reserved_mb": 1448.0, "process_rss_mb": 1605.17}`
- Training: `{"gpu": "NVIDIA GeForce RTX 3050 6GB Laptop GPU", "peak_gpu_allocated_mb": 1282.39, "peak_gpu_reserved_mb": 1448.0, "process_rss_mb": 1548.6}`

## Limitations and Decision

- One TEST example cannot support a statistical quality or generalization claim.
- Root-cause quality, recommendation quality, citation correctness, and unsupported-claim rate require the existing human/reference evaluation rubric and are not fabricated here.
- The held-out TEST contains one example, so grounding and quality checks remain limited and are not production evaluation.
- This adapter is not deployed and does not replace the base model.
- Pipeline readiness for a larger dataset: **TECHNICALLY READY FOR A CONTROLLED LARGER RUN; QUALITY READINESS NOT ESTABLISHED**.

# Baseline Evaluation Report

## Scope

This report records one baseline run of the current production Qwen path using
constrained v0.3 generation. It uses only the immutable `dataset-v0.3` TEST
split and does not change dataset files, evaluation criteria, or model state.

The run used the existing v0.3 prompt formatter and validators together with
the running `ai-inference-service` API. RAG requests used the constrained
`/v1/inference` route; the root-cause request used the constrained
`/v1/analysis/root-cause` route. There is no unconstrained fallback.

## Evaluation Metadata

| Field | Value |
|---|---|
| Model version | `qwen2.5:0.5b` |
| Dataset version | `dataset-v0.3` |
| Dataset split | `TEST` only; 3 examples |
| Dataset manifest | Immutable; 28 accepted examples, 23 train, 2 validation, 3 test |
| Dataset provenance prompt version | `ROOT_CAUSE_INTELLIGENCE@1.0.0+RECOMMENDATION_GENERATION@1.0.0` |
| Executed prompt path | Root cause `ROOT_CAUSE_ANALYSIS@1.0.0`; RAG used the existing v0.3 inference formatter |
| Inference provider | Ollama through the production `ai-inference-service` |
| Constrained generation | Outlines via production API |
| Temperature | `0.1` |
| Request timeout | `180 seconds` for this run |
| Fallback | None |
| Evaluation version | No explicit `evaluation_version` is defined by the existing framework; execution ID: `BASELINE_EVALUATION_2026-08-11` |

## Scenarios Evaluated

| Example | Task | Scenario | Result |
|---|---|---|---|
| `305c1dd3-5c41-453a-bd4e-7518a868e9f1` | RAG | `pilot-v03-exp3-livelihood-market-rag-001` | Passed structural contract |
| `d41ff8d7-3bbc-49c4-a67b-34f387336c67` | RAG | `pilot-v03-expansion-agriculture-rag-001` | Passed structural contract |
| `3d8e7942-43b9-4289-9901-ca706fdfb304` | Root cause | `pilot-v03-exp3-livelihood-market-access-root-001` | Passed structural contract |

Task distribution in the held-out split:

- Root-cause: 1
- Recommendation: 0
- RAG: 2

No recommendation quality result is reported because no recommendation example
exists in the immutable held-out TEST split.

## Measured Results

| Metric | Result | Interpretation |
|---|---:|---|
| Service calls completed | 3/3 | No provider or HTTP failures |
| Failure rate | 0/3 (0%) | No call failed |
| Structured JSON validity | 3/3 | All responses parsed as JSON objects |
| v0.3 schema compliance | 3/3 | Existing `validate_generated_target` returned no errors |
| Citation/source-ID correctness | 3/3 | Every returned source ID was present in the request input |
| Evidence grounding, structural | 3/3 | Evidence references were limited to supplied source IDs |
| Repair attempts | 0 | Constrained generation produced valid output directly |
| Uncertainty field presence | 3/3 | Required non-empty uncertainty fields were present |
| GPU memory telemetry | Not available | The service returned no GPU memory snapshot |

Latency measured end-to-end from the host against the production API:

- Minimum: `3,679.17 ms`
- Maximum: `8,962.12 ms`
- Average: `6,732.61 ms`

Per-call service-reported latency was `7,458 ms`, `3,676 ms`, and `8,939 ms`
respectively. The first call was not separated into a schema-cold metric by the
existing evaluator, so no cold/warm conclusion is claimed.

## Quality Metrics Not Scored

The existing repository contains structural v0.3 validation but does not
contain a reference-answer or human-scoring rubric that can validly measure
semantic quality for this run.

- Root-cause quality: **NOT SCORED**; no approved semantic reference rubric.
- Recommendation quality: **NOT APPLICABLE**; zero held-out recommendation examples.
- RAG quality: **NOT SCORED**; no approved semantic reference rubric.
- Unsupported claims: **NOT SCORED**; no claim-level adjudication labels.
- Semantic evidence grounding: **NOT SCORED**; structural source-ID linkage passed,
  but semantic entailment was not measured.
- Citation quality beyond source-ID validity: **NOT SCORED**; only the existing
  source-ID contract was evaluated.
- Semantic uncertainty handling: **NOT SCORED**; only required-field presence
  was checked.

## Failures and Citation Issues

- Provider/API failures: none.
- Structured-output failures: none.
- Schema failures: none.
- Citation/source-ID contract failures: none.
- Structural evidence-reference failures: none.
- Semantic quality failures: not determinable with the existing automated
  evaluation assets; no failure is fabricated.

## Limitations

1. The held-out test set contains only three examples and no recommendation
   example, so it cannot support balanced task-level conclusions.
2. The test scenarios are controlled pilot evaluations, not evidence of
   real-world village generalization.
3. Semantic root-cause, recommendation, and RAG quality require a validated
   human/reference rubric before those metrics can be reported.
4. GPU memory was not exposed by the running service, so resource usage is not
   reported.
5. The existing framework has no explicit evaluation-version field; the run
   identifier above is an execution identifier, not an existing framework
   release version.

## Comparison Readiness

**Structural comparison: READY.** The same v0.3 formatter, constrained
generation path, schema validator, source-ID contract, held-out examples, and
criteria can be reused for a fine-tuned-model run.

**Complete quality comparison: NOT READY.** A valid comparison of semantic
quality, unsupported claims, and recommendation quality requires a larger
held-out set containing recommendation examples and an approved semantic
evaluation rubric. No model-selection conclusion is made from this baseline.

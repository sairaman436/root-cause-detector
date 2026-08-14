# Constrained Decoding Validation

## Dependency Selected

- Dependency: `outlines==1.3.3`
- License: Apache-2.0
- Python requirement: `>=3.10,<3.14`; compatible with Python `3.13.2`
- Integration: `outlines.models.transformers.from_transformers` with `outlines.json_schema`
- Dependency declaration: `ml-platform/training-pipelines/pyproject.toml` under the existing `[training]` extra

The rejected `lm-format-enforcer` trial was removed because its Transformers adapter failed against Transformers `5.15.0`. It is not part of the project dependency set.

## Compatibility Result

Passed against the existing isolated GPU environment:

- PyTorch: `2.11.0+cu126`
- Transformers: `5.15.0`
- PEFT: `0.20.0`
- CUDA: available, CUDA `12.6`
- GPU: NVIDIA GeForce RTX 3050 6GB Laptop GPU
- Qwen: `Qwen/Qwen2.5-0.5B-Instruct`
- Outlines: imported successfully and created a Transformers model wrapper

The run produced non-fatal TorchDynamo/Triton warnings because Triton is not installed. They did not prevent constrained generation. No dependency was added to the runtime AI service.

## Integration Result

The v0.3 runner now requires the constrained generator for evaluation. It:

- Builds a task-specific strict JSON Schema from the existing v0.3 contract.
- Restricts every `source_id` and `evidence_source_ids` value to the current row's allowed source-ID enum.
- Enforces required fields, nested object shapes, arrays, enums, numeric bounds, and `additionalProperties=false`.
- Uses the same constraint for the bounded repair attempt.
- Raises a dependency error if Outlines is unavailable; it does not fall back to raw `model.generate`.

Dataset-v0.3 and the held-out test set were not modified.

## Controlled Smoke Results

The same three v0.3 TEST rows were used. Dataset digest remained:
`5a046ef1f2a76518a790a8fac4245ea2803094a6a1d8f0859a3776d7f6f3586b`.

| Metric | Unconstrained base Qwen | Outlines-constrained base Qwen |
|---|---:|---:|
| Valid structured output | `0/3` | `3/3` |
| Schema compliance | `0/3` | `3/3` |
| Citation/source-ID compliance | `0/3` | `3/3` |
| Evidence grounding | `0/3` | `3/3` |
| Repair attempts | `1 per example` | `0` |
| Average latency | `8728.22 ms` | `17944.91 ms` |
| Minimum latency | unavailable in prior summary | `4486.05 ms` |
| Maximum latency | unavailable in prior summary | `44174.63 ms` |

Per-example constrained latency:

- Root-cause: `44174.63 ms`
- RAG: `4486.05 ms`
- RAG: `5174.04 ms`

All three constrained responses passed the existing strict `validate_generated_target` validator. No citations were fabricated, and no qualitative quality scores were inferred.

## Failures

- No contract failures occurred in the constrained smoke test.
- First-call latency was high due to schema/constraint initialization and runtime compilation.
- Non-fatal Triton/TorchDynamo warnings were emitted.
- The held-out set contains one root-cause and two RAG examples; recommendation quality remains not applicable.

## Decision

**CONSTRAINED DECODING IS VIABLE FOR CONTRACT CORRECTNESS.** Outlines produced 3/3 valid v0.3 outputs where unconstrained base Qwen produced 0/3.

It is not yet production-ready on latency alone. The next engineering gate should profile and optimize schema initialization/compilation and confirm behavior across a larger approved evaluation set. No training or model comparison was started.

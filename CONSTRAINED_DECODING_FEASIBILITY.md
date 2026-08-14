# Constrained Decoding Feasibility

## Approach Tested

One controlled feasibility approach was evaluated against the existing Qwen v0.3 path:

- Hugging Face Transformers generation with a strict JSON/schema constraint.
- Dynamic enforcement of required fields and the row-specific allowed `source_id` set.
- No markdown or extra text.
- The unchanged v0.3 smoke-test inputs and contract validator would have been used for generation.

The approach stopped at the dependency/API gate. No model generation, training, fine-tuning, dataset write, or test-set modification was performed for this milestone.

## Dependency Availability

The GPU training environment contains:

- Python `3.13.2`
- PyTorch `2.11.0+cu126`
- Transformers `5.15.0`
- PEFT `0.20.0`
- bitsandbytes available

No compatible schema-constrained generation library is installed:

- `outlines`: unavailable
- `lm-format-enforcer`: unavailable
- `guidance`: unavailable
- `jsonformer`: unavailable
- `xgrammar`: unavailable
- `llama-cpp`: unavailable

The installed Transformers API exposes generic `prefix_allowed_tokens_fn` and `logits_processor` hooks, but no JSON-schema or grammar constraint processor. Its generic constraints cannot enforce the v0.3 nested schema and dynamic source-ID contract without implementing a new handwritten decoder, which is explicitly out of scope for this milestone.

The existing Ollama `format=json` option is syntax-only and cannot enforce the v0.3 schema, required fields, or allowed source IDs.

## Results Compared With Base Qwen

| Metric | Current base-Qwen result | Constrained result |
|---|---:|---:|
| Valid structured output | `0/3` | `NOT RUN: dependency unavailable` |
| Schema compliance | `0/3` | `NOT RUN: dependency unavailable` |
| Citation/source-ID compliance | `0/3` | `NOT RUN: dependency unavailable` |
| Evidence grounding | `0/3` | `NOT RUN: dependency unavailable` |
| Repair attempts | `1 per example` in current runner | `NOT RUN` |
| Latency | `8728.22 ms average` | `NOT MEASURED` |

The v0.3 dataset digest and held-out test set were not changed.

## Failures

- Required constrained-decoding dependency is missing.
- Existing Transformers hooks are insufficient for strict schema and source-ID enforcement without a custom implementation.
- No constrained-generation quality or latency metric can be claimed.

## Feasibility Decision

**NOT CURRENTLY VIABLE IN THE EXISTING ENVIRONMENT.** The constrained approach cannot be evaluated honestly until a compatible, approved schema-constrained generation library is added and validated for the current Python, CUDA, Transformers, and Qwen stack.

No workaround, citation synthesis, schema relaxation, or model change was introduced.

## Next Model Decision

A stronger compatible model should not be evaluated as a substitute for the missing constrained-decoding capability in this milestone. The next controlled step should first be a dependency-approved constrained-decoding spike, followed by the same three-row contract smoke test. Only after that gate passes should model strength be compared.

# Inference Contract Failure Analysis

## Scope

This analysis covers the base-Qwen evaluation path used by QLoRA Experiment 003. Dataset-v0.3 and its three held-out test rows were not changed.

## Confirmed Causes

1. **Prompt contract was underspecified.** The evaluation reused the training formatter and its task instruction only named broad fields. It did not show the exact nested object shape for `root_causes`, `recommendations`, risks, feasibility, or source-ID-only citations.
2. **Allowed source IDs were not explicitly presented as a closed set.** The prompt included evidence text and a lightweight citation list, but did not clearly instruct the model to copy only exact IDs from an allowed list. Base output therefore omitted citations or invented alternate citation fields.
3. **The v0.3 evaluator used the training prompt rather than an inference-specific prompt.** Training and inference have different needs: training includes the target, while inference needs an exact output contract and a generation prompt.
4. **The evaluator allowed silent input truncation.** Tokenization used `truncation=True`, so a long prompt could lose schema or citation context without being reported. This was a latent contract failure even when it was not the direct cause of the three Experiment 003 failures.

## Actual Model Failure

The base model did generate non-empty JSON for the three examples, but it did not reliably follow the requested v0.3 schema. It emitted alternate field names/shapes, omitted required citations and uncertainty strings, and failed to produce the required nested root-cause fields. This is a model adherence failure exposed by the contract, not evidence that the contract should be weakened.

## Parser and Evaluation Findings

- The existing strict `json.loads` and schema validator correctly rejected the observed outputs. No parser relaxation is appropriate.
- One output was valid JSON but structurally invalid; two outputs were also JSON-shaped but used unsupported fields or types. The primary failure was instruction/schema adherence, not JSON decoding.
- Citation and evidence-grounding scores are correctly structural gates: a result cannot pass them when the output has no valid source-ID citations. No unsupported grounding success was inferred.
- The evaluation did not have human/reference labels for qualitative root-cause, RAG, uncertainty, or unsupported-claim scoring; those metrics remain not scored.

## Legacy Contract Mismatch

The running AI inference service exposes a separate legacy `RuralAnalysisOutput` contract for its backward-compatible root-cause API. Experiment 003 evaluates the MLOps v0.3 task contract directly through the Hugging Face Qwen runner. This fix is intentionally limited to that v0.3 runner path and does not change the existing service API.

## Fixes Applied

- Added a dedicated v0.3 inference prompt with exact task-specific JSON shapes, strict no-markdown/no-extra-text rules, and a closed allowed source-ID list.
- Added explicit EOS and pad-token handling and increased the bounded evaluation generation budget to 512 new tokens so complete canonical objects are not cut off at 256.
- Removed silent evaluation-prompt truncation; prompts exceeding the configured sequence length now fail visibly before generation.
- Added one bounded validator-driven repair attempt. It re-prompts with the exact validation errors and the rejected response, then applies the same strict validator to the corrected response.
- Added a contract test covering schema and source-ID instructions in the inference prompt.

## Remaining Limitations

The controlled smoke test is still limited to one small local model and three held-out examples: one root-cause task and two RAG tasks. It cannot establish general model quality, recommendation quality, or statistical improvement. No training or dataset modification is permitted by this milestone.

## Controlled Base-Qwen Smoke Result

- Before the fixes: `0/3` structured, `0/3` citation-contract, and `0/3` evidence-grounding successes.
- After the fixes: `0/3` structured, `0/3` citation-contract, and `0/3` evidence-grounding successes.
- Post-fix average latency: `8728.22 ms` across the three rows. One bounded repair attempt was used for each row.
- The model continued to omit required citation/uncertainty fields or produce incomplete nested fields. This is now the remaining blocker; it is not safe to synthesize citations or lower the v0.3 contract.

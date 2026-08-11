# Model Selection Report

## Models Compared

No valid comparison was possible.

- Original base model: **Not selected**. The SONAR/Qwen benchmark concluded `Keep Both` because both runs had `0/1` successful generations.
- Fine-tuned model: **Does not exist**. Fine-Tuning Experiment 001 was classified `INVALID_EXPERIMENT` and produced no adapter or checkpoint.

## Evaluation Results

The existing evaluation framework defines suites for RAG groundedness, decision quality, and safety. The required held-out test set is empty, so no evaluation was run.

| Metric | Original base model | Fine-tuned model |
| --- | --- | --- |
| Root-cause quality | Not measured | Not available |
| Recommendation quality | Not measured | Not available |
| Evidence grounding | Not measured | Not available |
| Citation correctness | Not measured | Not available |
| Hallucination/unsupported claims | Not measured | Not available |
| Structured-output success | Benchmark failure: `0/1` | Not available |
| Uncertainty handling | Not measured | Not available |
| General instruction following | Not measured | Not available |
| Output reliability | Not measured | Not available |
| Latency | Failure-path timing only; excluded | Not available |
| Memory/resource usage | Not measured | Not available |

No metrics were fabricated from failed inference timings.

## Improvements

None identified. There is no fine-tuned model and no valid held-out evaluation result.

## Regressions

None measurable. Regression testing could not run without a fine-tuned artifact and held-out test examples.

## Major Failure Cases

1. `dataset-v0.1` contains zero accepted examples and zero train, validation, and test rows.
2. Experiment 001 was invalid and did not create an adapter or checkpoint.
3. The prior SONAR/Qwen benchmark produced no successful generations for either model.
4. The existing benchmark therefore did not select a model for fine-tuning.

## Resource Comparison

No valid resource comparison exists. Base-model failure-path latency is not model latency, and no fine-tuned model was loaded.

## Final Recommendation

**INVALID**. Do not select, promote, deploy, or fine-tune either model based on the current evidence.

## Another Training Experiment

Another experiment is **not justified yet**. Before rerunning:

1. Generate a governed non-empty dataset with immutable train, validation, and held-out test splits.
2. Rerun the Qwen/SONAR benchmark until at least one model produces valid structured outputs under identical prompts and context.
3. Select the model using successful quality, grounding, citation, safety, latency, and resource measurements.
4. Re-run the LoRA/QLoRA smoke test with a real train and validation split.

No production model, prompt, or test set was modified.

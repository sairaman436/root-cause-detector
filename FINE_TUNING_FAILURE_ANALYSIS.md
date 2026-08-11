# Fine-Tuning Experiment 001 Failure Analysis

Status: **ANALYSIS ONLY**. No training run was started, and no dataset or held-out test row was modified.

## Confirmed Causes

1. **The evaluation prompt did not request the structured contract that the evaluator checked.**
   `run_lora_qlora_experiment.py::_prompt` sends only a generic system instruction and the test input. It does not include the root-cause JSON schema, required keys, or an explicit JSON-only instruction. Both models therefore produced prose, while the evaluator expected parseable JSON.

2. **The TEST target itself is not valid JSON.**
   `pilot-agriculture-irrigation-001` is wrapped in a JSON code fence, but its `citation_ids` value contains the unquoted token `CONTROLLED_PROJECT_PILOT`. A correct JSON parser must reject that target. This is a dataset target-quality defect, not evidence that fine-tuning caused the failure.

3. **Citation grounding was impossible with the evaluation input as constructed.**
   `_prompt` passes `row["input"]` only. It does not pass `row["citations"]`, citation IDs, or retrieved evidence. The evaluator then searches the generated text for exact citation source IDs. A model cannot reliably emit `CONTROLLED_PROJECT_PILOT` when that identifier was never supplied in the prompt.

4. **The training formatter has the same grounding defect.**
   `run_lora_qlora_smoke_test.py::format_example` says to ground responses in supplied citations but supplies only the system message, input, and target output. Citation context is not part of the supervised input.

5. **The TRAIN corpus has no RAG-grounded training example.**
   The split contains four TRAIN rows: three root-cause-analysis rows and one recommendation-generation row. The only RAG-grounded row is in VALIDATION. The model therefore receives no RAG task supervision and is evaluated on structured/citation behavior that it was not trained to perform.

6. **The recommendation TRAIN target is truncated by the configured sequence length.**
   `pilot-water-maintenance-recommendation-001` has approximately 18,830 output tokens and approximately 18,878 formatted tokens. With `sequence_length = 1024`, most of that target is discarded before the loss is computed. This creates an incomplete supervision signal and makes the recommendation example materially unlike a normal bounded training target.

7. **The run performed only one optimizer update.**
   There are four TRAIN rows and `gradient_accumulation_steps = 8`. The experiment script accumulates all four losses and performs one optimizer step at epoch end. One epoch therefore means one parameter update, not four updates. The 5.2-second duration is consistent with a bounded smoke-scale update, not meaningful fine-tuning.

8. **The validation and test sample sizes cannot support quality conclusions.**
   VALIDATION contains one RAG row and TEST contains one root-cause row. The task distribution differs across splits, so the measured validation loss is not comparable to the held-out test behavior for the same task.

## Probable Causes

- The tiny, heterogeneous corpus likely cannot teach a stable task contract or generalize across domains from four training examples.
- The learning rate of `0.0002` with rank-16 LoRA and one effective optimizer update provides no reliable basis for judging adaptation. It may be reasonable for a future controlled run, but it was not validated here.
- The long recommendation target dominates token processing and may dilute the useful signal from the shorter examples before truncation.
- The training and inference paths do not share the production structured-generation prompt used by the application provider, so the experiment is not measuring the same contract as the platform workflow.
- The loss is computed over prompt tokens and target tokens equally because `labels` is a clone of all `input_ids`; assistant-only loss masking is not implemented. This can reduce the signal for the actual answer format.

These are training-design risks, not claims that the experiment proved a specific causal effect.

## Dataset Problems

- The TEST expected output is malformed JSON because the citation ID is unquoted.
- Output contracts are inconsistent by task: root-cause examples include structured JSON and prose-like forms, the recommendation example is a very large composite object, and the RAG example is prose.
- The citation field is stored as metadata but is not represented in the model input used for training or evaluation.
- The four TRAIN rows do not cover all three intended tasks; RAG is validation-only.
- The recommendation target is far beyond the 1,024-token context budget and is silently truncated.
- The split is leakage-safe by scenario group according to the manifest, but it is not task-balanced. A leakage-safe split is not automatically an evaluation-valid split.
- Six examples with one validation and one test example are sufficient for pipeline validation only, not model selection or generalization measurement.

## Training Problems

- `format_example` trains on the full concatenated conversation, including system and user tokens, rather than masking loss to the assistant target.
- Citation/evidence context is absent from the supervised prompt despite the system instruction referring to it.
- The configured `batch_size` is not used by the experiment runner; rows are processed one at a time.
- `warmup_ratio` is configured but not applied.
- `evaluation_steps` and `save_steps` are configured but not applied; the runner writes only a final checkpoint.
- Resume metadata is present in the configuration, but the experiment runner does not resume optimizer/model state from `resume_from_checkpoint`.
- One epoch with four rows and one effective optimizer update is insufficient for a meaningful adaptation conclusion.
- The QLoRA hardware path itself was not the failure: CUDA, bitsandbytes, PEFT, tokenizer loading, forward/backward, checkpointing, and adapter loading all succeeded.

## Evaluation Problems

- The evaluation prompt omits the required output schema and citation context.
- The structured-output check parses generated text but does not first establish that the expected target contract is valid.
- The grounding check is only an exact source-ID substring check. It is not citation precision/recall, entailment, or source verification.
- Citation correctness, unsupported-claim rate, root-cause quality, and recommendation quality were correctly reported as not scored. No human or reference rubric was executed.
- The test task is root-cause analysis, so recommendation quality is not applicable for this run. The report must not treat that as a recommendation comparison.
- Base and fine-tuned models used the same experimental prompt, but that prompt is not equivalent to the production provider's structured-generation prompt. The comparison is internally consistent but not production-contract valid.

## Recommended Changes

1. Define versioned, task-specific output schemas for root-cause, recommendation, and RAG-grounded response records. Validate every target before training; reject malformed JSON rather than training on it.
2. Correct the TEST target only through the existing governed dataset-review process. Do not edit the held-out test row ad hoc or silently replace it.
3. Build one canonical prompt formatter shared by training and evaluation. Include the task name, schema, evidence/citation context, source IDs, and explicit JSON-only output rules where the task requires structured output.
4. Normalize citation representation so the prompt contains the exact source IDs the evaluator expects and the evaluator validates citations against the supplied evidence/reference set.
5. Add assistant-only loss masking so the optimization target is the answer, not the instruction prefix.
6. Enforce a maximum target length before training. Split or redesign oversized recommendation records into bounded examples instead of silently truncating them.
7. Make configured batch size, warmup, checkpoint cadence, and resume behavior effective or remove them from the experiment configuration. Record actual optimizer-step counts.
8. Expand the governed corpus with balanced, independently reviewed examples for all three tasks and multiple rural domains. Preserve scenario-group isolation across splits.
9. Use multiple validation and test examples per task before making quality or model-selection claims. Keep evaluation prompts and criteria fixed across base and fine-tuned comparisons.
10. Add deterministic contract tests for schema validity, citation availability, prompt/rendered-token budgets, target truncation, and evaluator reference integrity before any future run.

## Is a Larger Dataset Required?

**Yes.** The current six-example corpus is adequate for exercising the pipeline, but not for learning or measuring a reliable model behavior. A larger governed corpus with task-balanced coverage, valid bounded targets, citation-bearing inputs, and non-overlapping scenario groups is required before drawing conclusions about fine-tuning quality.

## Is Experiment 002 Justified?

**Not yet.** Experiment 002 should not run against the current corpus or current evaluation path. It becomes justified only after the target schemas and citations are repaired through governance, the canonical formatter/evaluator is aligned, oversized examples are bounded, all intended tasks have training coverage, and the validation/test sets contain enough task-matched examples to support an interpretable comparison.

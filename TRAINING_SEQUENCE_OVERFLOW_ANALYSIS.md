# Training Sequence Overflow Analysis

Status: **AUDIT ONLY**

No dataset, evaluation set, review decision, model, or training configuration was modified by this analysis. No training or model loading was performed.

## Audit Method

The six records were rendered with the local Qwen tokenizer and the exact `format_training_example` path used by Experiment 004. Counts below are tokenizer token counts, not whitespace-word counts.

- `formatted`: complete Qwen chat-formatted training example, including target
- `input`: system/user portion with the generation prompt, excluding target
- `target`: raw structured JSON target
- `retrieved evidence`: the retrieved-evidence JSON already embedded in `row.input`
- `citation context`: the additional source-ID-only JSON appended by the training formatter
- `provenance`: row metadata and is not inserted into the training prompt; its token contribution is `0`, and it must remain unchanged through correction

Within the input totals, the fixed system instruction contributes `21` tokens, the task instruction contributes `22` tokens for recommendation rows and `16` tokens for RAG rows, and the scenario-only text contributes `12–18` tokens. The remaining input tokens are the retrieved context, citation block, and Qwen chat-template markers.

The configured training limit is `1024` tokens. The configured constrained-generation budget is `512` new tokens.

## Overflowing Records

| Split | Example ID | Task | Formatted | Input | Target | Retrieved evidence | Citation context | Over limit | Target above 512 |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| TRAIN | `0e454d77-aed4-4ce9-a019-c1d92a97aab7` | recommendation-generation | 1127 | 444 | 681 | 302 | 54 | 103 | 169 |
| VALIDATION | `437b4674-d344-40f5-bc47-84488f8792da` | recommendation-generation | 1120 | 439 | 679 | 303 | 54 | 96 | 167 |
| TRAIN | `43ea9cc6-45cf-43af-b746-e7bca8ff7128` | rag-grounded-responses | 1428 | 440 | 986 | 304 | 54 | 404 | 474 |
| TRAIN | `6122d108-a6a8-44c7-8351-6d2ab5cef88e` | rag-grounded-responses | 1426 | 440 | 984 | 304 | 54 | 402 | 472 |
| TRAIN | `92191669-e7f4-41db-87fc-bc57c7f710cf` | recommendation-generation | 1105 | 441 | 662 | 304 | 54 | 81 | 150 |
| TRAIN | `de3a0449-b8a8-43b1-b27b-5ffb5e22e3bd` | rag-grounded-responses | 1427 | 439 | 986 | 302 | 54 | 403 | 474 |

The six targets are below the record validator's current 1,024 whitespace-word check, but all six exceed the actual 512-token generation budget. This is a contract mismatch: `validate_record` and dataset materialization use whitespace words, while the trainer and Qwen use tokenizer tokens.

## Record Findings

### `0e454d77-aed4-4ce9-a019-c1d92a97aab7`

- Task: recommendation generation.
- Scenario: `pilot-v03-exp3-infrastructure-road-recommendation-001`.
- Target: `681` tokens, `250` whitespace words; it exceeds the 512-token generation budget.
- Overflow fields: three recommendation objects, each with title, description, evidence IDs, feasibility rationale, two risk objects, and four implementation steps; the target also repeats the root cause, uncertainties, and citations.
- Evidence/citation contribution: `302` tokens of retrieved evidence in the input plus `54` tokens of the formatter's additional source-ID context.
- Target bounding: **safe only through human correction/review**. The schema can preserve the root cause, at least two intervention options, feasibility, risks, steps, uncertainty, and citations while removing repetitive prose. The original target and provenance must remain immutable.

### `437b4674-d344-4ce7-be2e-32a2973f6d68`

- Task: recommendation generation.
- Scenario: `pilot-v03-expansion-water-recommendation-001`.
- Target: `679` tokens, `246` whitespace words; it exceeds the 512-token generation budget.
- Overflow fields: the same repeated recommendation structure as above, with verbose descriptions, generic risk explanations, implementation steps, root-cause text, and citation arrays.
- Evidence/citation contribution: `303` retrieved-evidence tokens plus `54` formatter citation-context tokens.
- Target bounding: **safe only through human correction/review**. A bounded target can retain the required options and decision fields without changing the scenario meaning, but no automatic rewrite is authorized.

### `92191669-e7f4-41db-87fc-bc57c7f710cf`

- Task: recommendation generation.
- Scenario: `pilot-v03-exp3-sanitation-drainage-recommendation-001`.
- Target: `662` tokens, `238` whitespace words; it exceeds the 512-token generation budget.
- Overflow fields: repeated descriptions, generic feasibility rationale, duplicated risk mitigation language, four repeated implementation steps per option, root cause, uncertainty, and citations.
- Evidence/citation contribution: `304` retrieved-evidence tokens plus `54` formatter citation-context tokens.
- Target bounding: **safe only through human correction/review**. The bounded output must retain multiple options, feasibility, risk, implementation steps, uncertainty, and valid source IDs.

### `43ea9cc6-45cf-43af-b746-e7bca8ff7128`

- Task: RAG-grounded response.
- Scenario: `pilot-v03-sanitation-drainage-rag-001`.
- Target: `986` tokens, `587` whitespace words; it exceeds the 512-token generation budget by `474` tokens.
- Overflow fields: the answer repeats retrieved document labels, sections, source IDs, and long evidence excerpts many times. The canonical RAG schema requires an answer, uncertainties, and citations; it does not require copying full excerpts into the answer.
- Evidence/citation contribution: `304` retrieved-evidence tokens plus `54` formatter citation-context tokens. The evidence is already available in the input and must not be removed as a shortcut.
- Target bounding: **safe only through human correction/review**. A concise evidence-grounded answer can preserve the required meaning, uncertainty, and source IDs while leaving the evidence context in the input. The original output must remain preserved.

### `6122d108-a6a8-44c7-8351-6d2ab5cef88e`

- Task: RAG-grounded response.
- Scenario: `pilot-v03-water-guidance-rag-001`.
- Target: `984` tokens, `588` whitespace words; it exceeds the 512-token generation budget by `472` tokens.
- Overflow fields: repeated full evidence passages and source descriptions in the answer, followed by the required uncertainty and citation arrays.
- Evidence/citation contribution: `304` retrieved-evidence tokens plus `54` formatter citation-context tokens.
- Target bounding: **safe only through human correction/review**. The answer can be shortened without removing the input evidence, citations, uncertainty, or provenance, but a reviewer must confirm semantic completeness.

### `de3a0449-b8a8-43b1-b27b-5ffb5e22e3bd`

- Task: RAG-grounded response.
- Scenario: `pilot-v03-exp3-infrastructure-road-rag-001`.
- Target: `986` tokens, `578` whitespace words; it exceeds the 512-token generation budget by `474` tokens.
- Overflow fields: repeated source-labelled evidence excerpts and long answer text rather than a bounded synthesis; the same evidence is already present in the input context.
- Evidence/citation contribution: `302` retrieved-evidence tokens plus `54` formatter citation-context tokens.
- Target bounding: **safe only through human correction/review**. The output can be semantically bounded to the canonical answer, uncertainty, and citations, but must not be rewritten automatically.

## Root Causes

1. **Tokenizer/validator mismatch.** Dataset v0.3 validates output length with whitespace words, while Qwen sequence capacity is measured in tokenizer tokens. The accepted records therefore passed the wrong length gate.
2. **Targets exceed the generation contract.** Recommendation targets are `662–681` Qwen tokens and RAG targets are `984–986`; all exceed the configured `max_new_tokens = 512`.
3. **RAG target duplication.** RAG answers copy long retrieved evidence excerpts into the output even though the same context is already in the input and citations carry source IDs.
4. **Formatter duplication.** `row.input` already contains a full retrieved-evidence/citation-context block. `format_training_example` appends a second source-ID-only citation context of approximately `54` tokens. Removing this block would require a versioned prompt/contract decision and would not resolve the current target overflow by itself.
5. **Verbose recommendation structure.** Recommendation outputs repeat generic feasibility, risk, mitigation, and implementation text across options. The required structure can remain while the prose is bounded, but only through governed human correction.

## Safe Remediation Options

### Option 1: Governed Human-Corrected Targets — Recommended

Create correction candidates for all six records through the existing authenticated review workflow. Preserve each original target, corrected target, source record, citations, provenance, review identity, timestamp, and correction decision. Require the corrected target to:

- validate against the canonical task schema;
- remain at or below `512` tokenizer tokens for the target;
- keep all required fields;
- keep at least two recommendation options for recommendation tasks;
- keep uncertainty fields;
- reference only source IDs present in the input;
- keep the original evidence context and provenance unchanged.

After explicit approval or validated correction, materialize a new immutable dataset version. Do not edit dataset-v0.3 in place.

### Option 2: Versioned Prompt/Formatter Compaction

Remove the duplicate source-ID-only block or compact nonessential prompt boilerplate only in a new prompt/contract version. This may recover approximately `54` tokens per example, but it does not make the current `662–986` token targets fit the 512-token generation budget and is therefore supplemental, not sufficient.

### Option 3: Explicit Exclusion

If a reviewer cannot produce a semantically complete bounded target, exclude the record in the next dataset version with a recorded reason such as `TARGET_EXCEEDS_GENERATION_BUDGET_NO_SAFE_CORRECTION`. Preserve the original record outside the training export. This is safe but reduces recommendation/RAG coverage and may leave an insufficient validation split.

### Option 4: Increase Sequence Length

Do not apply this as the first fix. It would still leave targets above the configured 512-token generation budget and would require a separate versioned training contract plus a real memory test.

## Records Requiring Human Action

All six records require explicit human correction/review before they may be used in a future training dataset. None should be auto-rewritten, silently truncated, or directly edited in dataset-v0.3.

No record is recommended for automatic exclusion at this stage. Exclusion is the fallback only when governed correction cannot preserve the required semantics within the target budget.

## Test-Set Overlap Analysis

Three of the four held-out records are exact content duplicates of records in the immutable dataset-v0.3 TEST split:

| Held-out ID | Task | Scenario | Matching dataset split | Match evidence |
|---|---|---|---|---|
| `305c1dd3-5c41-453a-bd4e-7518a868e9f1` | RAG | `pilot-v03-exp3-livelihood-market-rag-001` | TEST | Same ID, input, output, citations, and scenario |
| `d41ff8d7-3bbc-49c4-a67b-34f387336c67` | RAG | `pilot-v03-expansion-agriculture-rag-001` | TEST | Same ID, input, output, citations, and scenario |
| `3d8e7942-43b9-4289-9901-ca706fdfb304` | Root cause | `pilot-v03-exp3-livelihood-market-access-root-001` | TEST | Same ID, input, output, citations, and scenario |

The held-out copies add evaluation-set metadata and rubric/methodology metadata, but they are not independent examples. The recommendation holdout `4f1d260f-c355-4fe7-be2e-32a2973f6d68` (`pilot-v03-holdout-climate-heat-recommendation-001`) has no matching dataset-v0.3 record.

This is not TRAIN or VALIDATION contamination: none of the four held-out IDs or scenario groups occur in dataset-v0.3 TRAIN or VALIDATION. It is nevertheless a test-independence limitation because three final comparison rows duplicate the separate dataset TEST split.

### Recommended Overlap Resolution

Keep `evaluation-set-v1.0.0` immutable. Generate three distinct PILOT_EVALUATION scenarios through the existing evaluation, quality-gate, and authenticated human-review workflow, replacing the duplicated root-cause and RAG rows in a new immutable evaluation-set version such as `evaluation-set-v1.0.1`. Do not relabel, mutate, or copy new rows automatically. The replacement scenarios must be checked against all dataset-v0.3 splits and the old held-out set for scenario/input/output duplication.

## RTX 3050 6 GB Feasibility

Observed hardware at audit time:

- GPU: `NVIDIA GeForce RTX 3050 6GB Laptop GPU`
- Total VRAM: `6144 MiB`
- Free VRAM at audit time: `5407 MiB`
- Driver: `610.62`
- CUDA/BF16/bitsandbytes/PEFT: previously validated in the isolated training environment

Existing evidence proves QLoRA model loading, adapter creation, and one smoke forward/backward step at sequence length `256`. It does not prove a training step at `1024` or `1536` tokens. Longer sequences increase activation and attention memory; a 1536-token profile is especially risky on a 6 GB card and would need a separate bounded memory-readiness run with the exact batch, gradient checkpointing, quantization, and LoRA settings.

Therefore:

- Raising the limit to `1536` is **not currently certified**.
- Raising it to `1024` is already the configured limit but cannot solve targets above the 512-token generation budget.
- Target correction is the smallest and lowest-risk solution because it preserves the existing GPU profile and inference contract.

## Recommended Next Action

1. Keep dataset-v0.3 and evaluation-set-v1.0.0 immutable.
2. Add six governed correction candidates, preserving each original target and provenance.
3. Require human approval/correction and validate exact Qwen tokenizer counts, not whitespace words.
4. Create a new immutable training dataset version only after all corrected records pass schema, citation, PII, provenance, leakage, and tokenizer-length gates.
5. Replace the three duplicated holdout rows through a new governed evaluation-set version before claiming a fully independent comparison.
6. Only then rerun preflight. Do not start another experiment from the current data.

## Files Changed

- `TRAINING_SEQUENCE_OVERFLOW_ANALYSIS.md` only.

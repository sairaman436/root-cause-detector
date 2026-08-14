# Human Baseline Evaluation Report

## Examples Scored

0 of 4 held-out BASE Qwen outputs have human scores.

The four immutable evaluation records and current BASE outputs are exposed
in `ml-platform/evaluation/heldout/evaluation-set-v1.0.0`:

| Task | Example ID | Scoring status |
|---|---|---|
| `root-cause-analysis` | `3d8e7942-43b9-4289-9901-ca706fdfb304` | Missing human review |
| `recommendation-generation` | `4f1d260f-c355-4fe7-be2e-32a2973f6d68` | Missing human review |
| `rag-grounded-responses` | `305c1dd3-5c41-453a-bd4e-7518a868e9f1` | Missing human review |
| `rag-grounded-responses` | `d41ff8d7-3bbc-49c4-a67b-34f387336c67` | Missing human review |

The records preserve the evaluation-set version, rubric version, model/prompt
provenance, output, retrieved context, citations, and review provenance needed
for a compliant human review.

## Reviewer Count

0 authenticated human reviewers have persisted rubric reviews.

## Per-Task Scores

No dimension scores are available.

| Task | Applicable rubric dimensions | Scores |
|---|---|---|
| `root-cause-analysis` | `root_cause_quality`, `rag_evidence_quality`, `uncertainty_handling`, `practical_usefulness` | Missing |
| `recommendation-generation` | `root_cause_quality`, `recommendation_quality`, `rag_evidence_quality`, `uncertainty_handling`, `practical_usefulness` | Missing |
| `rag-grounded-responses` (2 examples) | `rag_evidence_quality`, `uncertainty_handling`, `practical_usefulness` | Missing |

## Disagreements

Not evaluable. No independent human scores exist.

## Average Scores

Not computable. No human scores exist, and no automated or AI-generated score
has been substituted for human judgment.

## Missing Scores

All 4 examples are missing every applicable rubric dimension score. Required
review metadata is therefore also missing: authenticated reviewer identity,
review timestamp, dimension scores, evidence references used, and reviewer
notes.

The review must use:

- `HUMAN-QUALITY-RUBRIC@1.0.0`
- `evaluation-set-v1.0.0`
- the immutable BASE Qwen outputs and supplied evidence context
- the recorded model, prompt, inference configuration, and output digest

## Validation Status

**BLOCKED: human scoring not completed.**

The evaluation-set structural validation remains valid: 4 test examples, with
1 root-cause, 1 recommendation, and 2 RAG examples. This does not constitute
human quality validation.

The existing `evaluation.pilot_human_reviews` table contains no rows. The
existing web portal exposes training-candidate approve/correct/reject actions,
but it does not expose the five-dimension human-quality rubric for held-out
BASE outputs. Those training-governance actions cannot be used as quality
scores.

## Remaining Blockers

An authorized reviewer must use a rubric-capable authenticated review surface
that records one review per held-out BASE output, including reviewer identity,
rubric/evaluation-set versions, model and inference metadata, timestamp,
dimension scores, evidence references, and notes. Until those four human
reviews are persisted, per-task scores, disagreements, averages, and baseline
validation cannot be reported.

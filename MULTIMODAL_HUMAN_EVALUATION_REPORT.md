# Multimodal Human Evaluation Report

## Status

**PENDING_AUTHENTICATED_HUMAN_REVIEW**

No human scores, failure classifications, unsupported-claim flags, or reviewer
comments were created automatically. This report records the available traces
and the exact review blocker.

The Multimodal Evaluation Lab stores local artifacts in browser `localStorage`.
The current implementation does not provide a server-side import for the
persisted JSON artifacts or a server-side reviewer identity for multimodal
scores. The portal itself is authenticated, but no authenticated human scoring
submission has been recorded for this round.

## Review Inventory

### Existing cross-domain artifacts

The six completed reviewable runs are:

1. Agriculture, original cross-domain run: `csp-agriculture.jpg`.
2. Agriculture, second case: `csp-agriculture-2.jpg`.
3. Healthcare: `csp-healthcare.jpg`.
4. Energy: `csp-energy.jpg`.
5. Education: `csp-education.jpg`.
6. Livelihoods: `csp-livelihood.jpg`.

The original Agriculture trace is documented in
`MULTIMODAL_REAL_WORLD_EVALUATION_REPORT.md`. The other five completed traces
are in `artifacts/evaluation/retrieval-alignment-controlled-reruns-20260814.json`.
All six existing artifacts are marked `UNSCORED` with empty human score data.

The prior failed Water/Sanitation authorization artifact was not treated as a
reviewable result.

### New Water/Sanitation run

- Image: `C:\Users\saira\AppData\Local\Temp\csp-water-sanitation-valid.jpg`
- SHA-256: `efb845afd509d3434a22d979f4989998037ecde0133add997a9c281bf7c388f3`
- Model: `moondream:1.8b` through Ollama.
- Vision latency: `15,550 ms`.
- Image observation: a water pump in an open field with dry grass, on a dirt
  mound, with a blue cover over the spout; no people or other objects visible.
- Model uncertainty: the image does not establish a diagnosis or cause.
- Retrieval latency: `2,681 ms`.
- RAG status: `INSUFFICIENT_EVIDENCE`.
- Retrieved governed evidence: `0` citations.
- Root cause: not generated.
- Recommendation: correctly stopped before generation because the evidence
  threshold was not met.
- Human review status: `UNSCORED`.

Actual retrieval query:

```text
Evaluation context: Water & sanitation (user-provided label; image domain not assumed)
Governed retrieval domain: WATER_SANITATION
User question: What visible water-access condition should be investigated?
Observed concepts: water pump, open field, dry grass, dirt mound, blue cover, spout
Observed summary:
- The image shows a water pump in an open field with dry grass surrounding it. The water pump is located on top of a dirt mound and has a blue cover over its spout. There are no people or other objects visible in the scene.
Model uncertainty: The image does not establish a diagnosis or cause.
```

## Trace Review Matrix

The following is an operational trace inventory, not a human quality score.

| Domain | Vision observations | Retrieval/evidence | Root cause | Recommendation | Human status |
|---|---:|---|---|---|---|
| Agriculture 1 | Returned | Governed evidence returned in the original run | Completed | Blocked by grounding | Pending |
| Agriculture 2 | Returned | 0 qualifying citations in the post-remediation artifact | Not generated | Not generated | Pending |
| Healthcare | Returned | 0 qualifying citations in the post-remediation artifact | Not generated | Not generated | Pending |
| Energy | Returned | 1 governed source | Root-cause object persisted | Blocked after HTTP 400 in rerun artifact | Pending |
| Education | Returned | 3 governed citations | Root-cause object persisted | Generated | Pending |
| Livelihoods | Returned | 1 governed source | No validated root cause | Not generated | Pending |
| Water/Sanitation | Returned | 0 citations; insufficient evidence | Not generated | Correctly stopped by gate | Pending |

Every row remains subject to human inspection of the complete image,
observation, retrieval query, evidence, root-cause, and recommendation/block
trace in the Multimodal Evaluation Lab.

## Human Scoring

No authenticated reviewer has submitted scores.

| Metric | Score | Sample size |
|---|---:|---:|
| Observation quality | Not available | 0 |
| Evidence relevance | Not available | 0 |
| Root-cause quality | Not available | 0 |
| Recommendation quality | Not available | 0 |
| Grounding | Not available | 0 |
| Overall usefulness | Not available | 0 |

Recommendation quality must remain **Not Scored** for Agriculture 1,
Healthcare, Agriculture 2, Energy, Livelihoods, and Water/Sanitation whenever
the reviewer determines that no supported recommendation was produced or the
recommendation was correctly blocked.

No averages were calculated. No score was inferred from structural pass/fail
status, latency, evidence count, or a grounding gate.

## Failure Classification

Human failure labels are pending for all seven reviewable traces. The listed
pipeline statuses are factual run outcomes only and are not reviewer
classifications:

- Agriculture 2: insufficient governed evidence.
- Healthcare: insufficient governed evidence.
- Energy: recommendation blocked after the rerun response error.
- Livelihoods: no validated root cause.
- Water/Sanitation: insufficient governed evidence after successful vision.

The reviewer must separately choose whether a blocked recommendation was a
correct safety outcome or a system failure. No unsupported-claim flags or
reviewer comments have been added.

## Domain Comparison

| Domain | Observation | Evidence | Root Cause | Recommendation | Grounding | Usefulness |
|---|---|---|---|---|---|---|
| Agriculture 1 | Pending | Pending | Pending | Not scored pending review | Pending | Pending |
| Agriculture 2 | Pending | Pending | Pending | Not scored pending review | Pending | Pending |
| Healthcare | Pending | Pending | Pending | Not scored pending review | Pending | Pending |
| Energy | Pending | Pending | Pending | Not scored pending review | Pending | Pending |
| Education | Pending | Pending | Pending | Pending | Pending | Pending |
| Livelihoods | Pending | Pending | Not scored pending review | Not scored pending review | Pending | Pending |
| Water/Sanitation | Pending | Pending | Not scored pending review | Not scored pending review | Pending | Pending |

## Dashboard State

- Total evaluated traces: `7`.
- Completed live traces: `7` trace artifacts available for review.
- Correctly blocked: not yet human-classified.
- Failed: not yet human-classified.
- Reviewer count: `0`.
- Scored examples: `0`.
- Remaining examples: `7`.
- Average metrics: not calculated because the sample size is zero.

## Strongest and Weakest Stage

Cannot be determined from human-quality evidence yet. Structural outcomes show
that the pipeline can complete Education and can fail closed for insufficient
evidence, but that is not a human usefulness assessment and is not used to
rank stages here.

## Required Human Action

An authenticated reviewer must open the local portal at
`http://localhost:3000`, select **Multimodal Evaluation Lab**, inspect each
trace, enter the six rubric scores where applicable, choose a failure class,
set unsupported-claim flags only when observed, add comments, and save each
artifact. For correctly blocked cases, recommendation quality remains
**Not Scored**.

## Data Separation

No dataset, evaluation-set version, training candidate, or production
knowledge record was created or modified. The new Water/Sanitation result is
evaluation-only and remains outside all training workflows.

## Remaining Blocker

The milestone cannot produce a human-quality result until an authenticated
human reviewer submits the seven reviews. The current localStorage-only
artifact path also does not persist reviewer identity server-side; that is a
workflow limitation, not a reason to fabricate scores.

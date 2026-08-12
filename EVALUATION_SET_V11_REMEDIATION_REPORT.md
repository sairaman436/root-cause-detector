# Evaluation Set v1.1.0 Remediation Report

Status: BLOCKED_REPLACEMENT_PIPELINE

## Scope

`evaluation-set-v1.0.0` remains immutable. Three of its four rows are exact
duplicates of `dataset-v0.3` TEST rows and cannot remain in an independent
evaluation set. The distinct climate recommendation holdout remains available
as the recommendation member of a future v1.1.0 set.

Overlapping immutable v1.0.0 rows:

| Evaluation row | Task | Scenario | Conflict |
|---|---|---|---|
| `305c1dd3-5c41-453a-bd4e-7518a868e9f1` | RAG | `pilot-v03-exp3-livelihood-market-rag-001` | exact dataset-v0.3 TEST duplicate |
| `d41ff8d7-3bbc-49c4-a67b-34f387336c67` | RAG | `pilot-v03-expansion-agriculture-rag-001` | exact dataset-v0.3 TEST duplicate |
| `3d8e7942-43b9-4289-9901-ca706fdfb304` | root cause | `pilot-v03-exp3-livelihood-market-access-root-001` | exact dataset-v0.3 TEST duplicate |

None of those rows overlap v0.3 TRAIN or VALIDATION, but that does not satisfy
independence from the full training dataset version.

## Replacement Candidates

Four distinct PILOT_EVALUATION replacement scenarios are registered in the
existing governed scenario service:

| Scenario key | Task | Intended role | Review state |
|---|---|---|---|
| `V04_REPLACEMENT_ROOT_DISASTER_WARNING_001` | root cause | replacement for duplicated root-cause row | provider/evidence gate HTTP 502; no persisted result |
| `V03_EXP_RAG_CLIMATE_001` | RAG | replacement RAG scenario | provider/evidence gate HTTP 502; no persisted result |
| `V04_REPLACEMENT_RAG_LIVELIHOOD_STORAGE_001` | RAG | replacement RAG scenario | provider/evidence gate HTTP 502; no persisted result |
| `V04_REPLACEMENT_RECOMMENDATION_MOBILE_CLINIC_001` | recommendation | replacement recommendation scenario | persisted PILOT_EVALUATION scenario, pending execution/review |

They are constructed, clearly labelled PILOT_EVALUATION scenarios, distinct
from the v0.3 scenario groups, and use the existing evidence, source-ID, PII
and authenticated human-review gates. No replacement result or candidate has
been auto-created or approved.

## Current Counts

| Set | Total | Root cause | Recommendation | RAG | Train | Validation | Test |
|---|---:|---:|---:|---:|---:|---:|---:|
| `evaluation-set-v1.0.0` immutable source | 4 | 1 | 1 | 2 | 0 | 0 | 4 |
| `evaluation-set-v1.1.0` | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

Replacement review counts: approved 0, corrected 0, rejected 0, pending 0.
No replacement candidate was persisted. The root and both RAG executions
reached the rebuilt service but failed at `RAG_EVIDENCE_REQUIRED`, surfaced as
HTTP 502. The recommendation scenario was registered with PILOT_EVALUATION and
remains pending execution/review.

## Validation and Overlap Status

The v1.0.0 duplicate finding is confirmed. A v1.1.0 validation result cannot
be issued until replacement evaluations are persisted and reviewed. The final
set must contain one root-cause, one recommendation and two RAG examples, with
zero overlap against v0.4 TRAIN, VALIDATION or TEST and zero synthetic records.
Current replacement result count is `0`; current replacement candidate count is
`0`.

## Required Next Gate

Complete the backend image rebuild, run the three registered scenarios through
the existing pilot endpoint, generate candidates only for eligible results,
and leave them pending authenticated human review. After explicit decisions,
materialize `evaluation-set-v1.1.0` and run the complete contract and leakage
validator. Do not mutate v1.0.0 or begin training before that gate passes.

## Verification

- PostgreSQL and the existing backend container are healthy.
- The locally built artifact passed the Maven offline reactor build.
- Flyway validated 33 migrations and startup passed after aligning the V32
  `CHAR(64)` hash column mapping.
- Replacement results/candidates persisted: `0`.
- No evaluation-set or v0.3 row was modified.

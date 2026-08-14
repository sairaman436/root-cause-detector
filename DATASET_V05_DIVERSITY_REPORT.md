# Dataset v0.5 Diversity Report

## Status

Dataset v0.5 was **not materialized**. Dataset v0.4, evaluation-set-v1.0.0, and evaluation-set-v1.1.0 were not modified. No training or fine-tuning was run.

## v0.4 Coverage

The immutable v0.4 audit found 25 records across six domains:

| Domain | Root cause | Recommendation | RAG | Total |
|---|---:|---:|---:|---:|
| Water & sanitation | 3 | 3 | 2 | 8 |
| Agriculture & food production | 2 | 1 | 2 | 5 |
| Healthcare access | 2 | 1 | 2 | 5 |
| Housing/basic infrastructure | 1 | 1 | 1 | 3 |
| Education | 1 | 0 | 1 | 2 |
| Livelihoods/markets | 1 | 0 | 1 | 2 |
| Energy/electricity | 0 | 0 | 0 | 0 |
| Climate/disaster resilience | 0 | 0 | 0 | 0 |

The v0.4 audit identified 10 near-duplicate pairs at the configured 0.45 Jaccard threshold, a 3.33% pair rate across 300 possible pairs. Eleven records reuse the same four-source evidence bundle, including `development-evaluation-fixture`; this is retained as an audit finding and was not changed.

See [DATASET_V04_DIVERSITY_AUDIT.md](DATASET_V04_DIVERSITY_AUDIT.md).

## v0.5 Target And Candidate Set

The target is eight domains multiplied by three tasks: 24 distinct scenarios.

| Target domain | Root cause | Recommendation | RAG | Completed candidates | Blocked |
|---|---:|---:|---:|---:|---:|
| Water & sanitation | 1 | 1 | 1 | 3 | 0 |
| Agriculture & food production | 1 | 1 | 1 | 3 | 0 |
| Healthcare access | 1 | 1 | 1 | 3 | 0 |
| Energy/electricity | 1 | 0 | 1 | 2 | 1 |
| Education | 1 | 1 | 1 | 3 | 0 |
| Livelihoods/markets | 1 | 1 | 1 | 3 | 0 |
| Climate/disaster resilience | 1 | 0 | 1 | 2 | 1 |
| Housing/basic infrastructure | 1 | 1 | 1 | 3 | 0 |
| **Total** | **8** | **8** | **8** | **22** | **2** |

The 24 registered scenario definitions use materially different problem categories, unique scenario keys, unique evidence IDs, unique evidence source IDs, explicit uncertainty, and domain metadata. The runtime diversity gate blocks exact evidence-source reuse and scenario-text Jaccard overlap at or above 0.45 against existing persisted scenarios.

## New Scenarios

Completed and persisted through `PILOT_EVALUATION`:

- Water and sanitation: latrine overflow, waste-collection coordination, toilet-access retrieval.
- Agriculture and food production: crop pest/disease, seed storage, post-harvest food safety.
- Healthcare access: health-center staffing, routine appointment access, facility-hours retrieval.
- Energy/electricity: transformer reliability, grid-outage retrieval.
- Education: teacher attendance, student transport, dropout evidence retrieval.
- Livelihoods/markets: seasonal employment, supply-chain disruption, artisan-market information.
- Climate/disaster resilience: drought preparedness, cyclone-warning retrieval.
- Housing/basic infrastructure: community-facility roof leakage, market-shed maintenance, facility-condition retrieval.

## Blocked Scenarios

The following two recommendation scenarios were attempted through the governed endpoint and were not persisted:

| Scenario | Failure | Reason |
|---|---|---|
| `pilot-v05-energy-solar-maintenance-recommendation-generation-001` | HTTP 400 | `VALIDATED_ROOT_CAUSE_REQUIRED` |
| `pilot-v05-climate-flood-resilience-recommendation-generation-001` | HTTP 400 | `VALIDATED_ROOT_CAUSE_REQUIRED` |

No root cause was fabricated or auto-validated. These scenarios remain blocked until a legitimate validated root-cause dependency exists.

## Evidence And Governance

- Completed evaluations: `22`.
- Completed evaluation results: `22`.
- RAG citation retrieval: present for every completed result.
- Evidence source IDs: unique `PILOT_V05_*` IDs per scenario.
- Classification: `PILOT_EVALUATION`.
- PII gate: passed for candidates accepted by the existing candidate generator.
- Provenance gate: passed for candidates accepted by the existing candidate generator.
- Human review: `22` candidates pending; `0` approved, `0` corrected, `0` rejected.
- No candidate was auto-approved or promoted to a dataset.

## Candidate Distribution

- Total completed candidates: `22`.
- Root-cause candidates: `8`.
- Recommendation candidates: `6`.
- RAG candidates: `8`.
- Semantic diversity blocks: `0`.
- Exact evidence-source reuse blocks: `0`.
- Candidate generation blocks after evaluation: `0`.
- Evaluation blocks: `2`, both `VALIDATED_ROOT_CAUSE_REQUIRED`.

## Splits

No train, validation, or test split exists for v0.5 because materialization is intentionally deferred until human decisions exist. The final split distribution is therefore **not applicable** at this stage.

## Remaining Blockers

1. Authenticated reviewers must review the 22 pending candidates.
2. The two blocked recommendation scenarios require a legitimate validated-root-cause dependency; thresholds and governance gates must not be bypassed.
3. After review, a separate v0.5 materialization step must validate schema, citations, provenance, PII, sequence lengths, domain/task coverage, semantic duplication, and split leakage.

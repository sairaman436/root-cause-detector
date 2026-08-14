# Dataset v0.5 Quality Remediation Report

## Scope

This report records the v0.5 evidence-isolation and candidate-quality remediation. No candidate was approved, no dataset-v0.5 was materialized, and no training run was started.

## Root Cause Of Evidence Contamination

The previous PILOT_EVALUATION path indexed all controlled evidence into one shared corpus, filtered primarily by domain, and allowed the backend vector-search fallback to supply evidence when the governed RAG request had no matching result. A recently indexed or broadly labelled `CONTROLLED_PROJECT_PILOT` document could therefore be returned for an unrelated scenario.

The previous path also treated `CONTROLLED_PROJECT_PILOT` as the source label for multiple constructed scenarios. That label is a provenance/source-type classification, not a sufficient retrieval boundary.

## CONTROLLED_PROJECT_PILOT Analysis

`CONTROLLED_PROJECT_PILOT` remains a legitimate constructed PILOT_EVALUATION provenance label for controlled scenario records, but it is not real field evidence and is not a generic retrieval source. The remediation path now assigns each v0.5 correction/replacement scenario its own provenance-bearing evidence source ID and allows retrieval only from that ID. Development fixtures remain blocked by the existing development/synthetic/fixture checks.

The historical v0.5 candidates were not mutated. They remain pending and require separate human review under the existing governance workflow.

## Isolation Fix

Implemented the smallest boundary-preserving fix:

- RAG supports `allowed_source_ids` and `governed_only` filters.
- Governed retrieval rejects development, synthetic, and fixture sources.
- The backend does not use its generic vector-search fallback for governed evaluation requests.
- Root-cause analysis consumes the exact governed RAG evidence supplied by the scenario pipeline.
- Recommendation generation receives the same source allowlist and fails closed without RAG evidence.
- New remediation documents include a provenance-only scenario identity marker so checksum deduplication cannot collapse distinct governed records.
- Same-document retry after a partial request is idempotent; a different document with the same checksum remains rejected.

## Root-Cause Quality Fix

Root-cause factors are now emitted only when non-problem evidence supports the factor. When evidence is insufficient, the service emits an explicit unresolved-evidence result with zero confidence and no causal hypothesis. The evaluation pass gate also requires every emitted validated root cause to have supporting evidence.

This preserves the distinction between observed evidence and an unvalidated hypothesis and does not manufacture a cause from an empty or unrelated evidence set.

## Recommendation Grounding Fix

Recommendation generation remains behind `VALIDATED_ROOT_CAUSE_REQUIRED`. For v0.5 remediation scenarios it also receives a scenario-specific evidence allowlist. Intervention options use the scenario domain/problem category and retain feasibility, risk, implementation, uncertainty, and citation fields. A recommendation cannot satisfy the evidence gate using generic or cross-scenario evidence.

## Regeneration Results

All 22 requested immutable remediation attempts were made individually:

- 20 correction versions were attempted.
- 2 replacement versions were attempted.
- 19 evaluation results completed and persisted.
- 3 recommendation attempts were blocked before result persistence by `VALIDATED_ROOT_CAUSE_REQUIRED`:
  - `pilot-v05r-agri-seed-storage-recommendation-generation-001`
  - `pilot-v05r-health-appointment-access-recommendation-generation-001`
  - `pilot-v05r-livelihood-supply-chain-recommendation-generation-001`
- Of the 19 completed results, 11 passed the existing candidate-generation eligibility gates and produced new candidates.
- 8 completed results remain blocked from candidate generation by the existing structural/quality gate, including invalid model contract output or scores below the candidate threshold:
  - `pilot-v05r-agri-food-safety-rag-grounded-responses-001`
  - `pilot-v05r-climate-drought-preparedness-root-cause-analysis-001`
  - `pilot-v05r-climate-cyclone-warning-rag-grounded-responses-001`
  - `pilot-v05r-health-facility-hours-rag-grounded-responses-001`
  - `pilot-v05r-housing-community-facility-rag-grounded-responses-001`
  - `pilot-v05r-livelihood-artisan-markets-rag-grounded-responses-001`
  - `pilot-v05r-water-toilet-access-rag-grounded-responses-001`
  - `pilot-v05r-water-household-greywater-recommendation-generation-001`

## Candidate State

The 11 generated remediation candidates are all:

- `PILOT_EVALUATION`
- non-synthetic candidate records
- linked to their persisted evaluation result and scenario provenance
- `PENDING_APPROVAL`
- not included in any dataset

The 22 historical v0.5 candidates remain unchanged: all 22 are still `PENDING_APPROVAL` and none was auto-approved or rejected.

## Tests And Verification

- RAG regression suite: **8 passed**.
- RAG Python compilation: **passed**.
- Root-cause focused backend suite: **4 passed**.
- Full core-backend suite: **138 passed, 0 failures, 0 errors**.
- Core-backend Docker build: **passed**.
- Flyway startup validation against local PostgreSQL: **34 migrations validated; schema up to date**.
- Backend health: **UP**.
- RAG health: **ok**.
- Evidence isolation regression proves Scenario A cannot retrieve Scenario B evidence.
- Governed-only regression proves development/synthetic evidence produces insufficient evidence.
- Candidate governance regression proves development-synthetic citations are blocked.
- Database verification proves no historical v0.5 review status changed.

## Remaining Human-Review Requirements

Human reviewers must review the 11 new pending candidates through the existing authenticated Training Review workflow. The 8 structural/quality-blocked results require correction and re-evaluation through a new governed version before they can enter review. The 3 recommendation scenarios require a validated root-cause result before recommendation generation can proceed. No dataset-v0.5 materialization is permitted until those governance and quality gates are satisfied.

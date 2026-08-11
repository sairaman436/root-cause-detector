# Training Governance Report

## Files Changed

- `services/core-backend/src/main/java/com/airural/platform/core/learning/application/ContinuousLearningService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/ApprovalWorkflowEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/CorrectionEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/HumanReviewEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/LearningRecordEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/TrainingCandidateEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/infrastructure/TrainingCandidateRepository.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/web/LearningController.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/web/dto/LearningDtos.java`
- `services/core-backend/src/main/java/com/airural/platform/core/identity/security/SecurityConfiguration.java`
- `services/core-backend/src/main/resources/db/migration/V29__governed_training_review_pipeline.sql`
- `services/core-backend/src/test/java/com/airural/platform/core/learning/ContinuousLearningServiceTests.java`
- `services/core-backend/src/test/java/com/airural/platform/core/learning/TrainingReviewIntegrationTests.java`
- `services/core-backend/src/test/resources/h2-schemas.sql`
- `apps/web-portal/src/app/page.tsx`
- `ml-platform/training-pipelines/promote_approved_candidates.py`
- `ml-platform/training-pipelines/build_dataset_v01.py`
- `tests/foundation/test_approved_candidate_promotion.py`

## Review Flow Implemented

Real learning records can now be queued as governed training candidates and reviewed through the authenticated API and dashboard UI:

`learning record -> pending candidate -> JWT-authenticated APPROVE/CORRECT/REJECT -> audit and persistence -> approved-real export -> existing dataset-v0.1 validator`

- Reviewer identity is taken from the authenticated JWT principal; the legacy request reviewer value is ignored on the authenticated route.
- `CORRECT` persists the original output, corrected output, correction validation state, reviewer user ID, review ID, and review timestamp.
- `REJECT` requires a non-empty reason and remains outside dataset export.
- Synthetic candidates can only be rejected and cannot enter the production dataset.
- Pending candidates are not exportable.
- Dataset export requires an approved real candidate, a training-eligible learning record, supported task, non-empty scenario group, valid citations, provenance, and no detected PII.
- The existing JSONL synthetic review queue remains unchanged and is not connected to production dataset promotion.

## Tests Passed/Failed

- Backend: 11 tests passed, 0 failed (`ContinuousLearningServiceTests`: 8; `TrainingReviewIntegrationTests`: 3).
- Python dataset/promotion tests: 15 passed, 0 failed.
- Python compilation: passed for the dataset builder, governed promotion adapter, and promotion tests.
- Frontend: `npm run build` passed, including TypeScript validation and static generation.
- `git diff --check`: passed.

## Security/Authorization Checks

- Unauthenticated access to the candidate queue is rejected with HTTP 401.
- Candidate review requires an authenticated principal with the existing learning governance permissions.
- Review persistence stores the JWT user ID and actual human review ID.
- Synthetic approval is rejected before any review record is written.
- Correction output and rejection reason are validated before state transition.
- The UI does not expose a reviewer text input.

## Dataset Promotion Checks

- Only `APPROVE` or `CORRECT` decisions from approved real candidates are accepted by the promotion adapter.
- Synthetic, pending, rejected, malformed, duplicate, PII-bearing, citation-invalid, and incomplete export records are blocked.
- The existing dataset builder remains responsible for schema validation, provenance, duplicate detection, PII checks, citation checks, deterministic leakage-safe splits, and split leakage validation.
- A controlled approved-real integration record materialized successfully through the export contract in tests.
- No production training run was started.

## Remaining Blockers

- The real PostgreSQL governance store has no human-approved real candidates yet, so `dataset-v0.1` correctly remains empty. A reviewer must explicitly approve or correct real governed interactions before materialization can produce production examples.
- The 9 synthetic development queue entries remain pending and are intentionally excluded from `dataset-v0.1`.
- The full V29 migration should be verified against a PostgreSQL integration environment before release. The workflow integration test uses isolated H2 schema generation because an older pre-existing migration (`V25`) is not H2-compatible.
- The legacy `ReviewRequest` retains its compatibility `reviewer` field, but the authenticated controller/service path ignores it and records the JWT identity instead.

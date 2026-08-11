# Training Candidate Generation Report

## Candidates Generated

- Production candidates generated during validation: `0`.
- In-memory eligible-path test candidate: `1`.
- No candidate was approved, corrected, promoted, or trained.

## Candidates Blocked

- Failed evaluation result: `1` (`RESULT_NOT_PASSED`).
- Synthetic fixture: `1` (`SYNTHETIC_FIXTURE`).
- Quality-gate failure: `1` (`QUALITY_GATE_FAILED`).
- Duplicate evaluation result skipped: `1` (`DUPLICATE_EVALUATION_RESULT`).

## Files Changed

- `services/core-backend/src/main/java/com/airural/platform/core/evaluation/domain/PilotScenarioEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/identity/security/SecurityConfiguration.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/application/ContinuousLearningService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/application/EvaluationTrainingCandidateService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/application/TrainingCandidateQueue.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/domain/LearningRecordEntity.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/infrastructure/LearningRecordRepository.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/web/LearningController.java`
- `services/core-backend/src/main/java/com/airural/platform/core/learning/web/dto/LearningDtos.java`
- `services/core-backend/src/main/resources/db/migration/V30__evaluation_training_candidate_bridge.sql`
- `services/core-backend/src/test/java/com/airural/platform/core/learning/EvaluationTrainingCandidateServiceTests.java`
- `services/core-backend/src/test/java/com/airural/platform/core/learning/TrainingReviewIntegrationTests.java`

## Tests Passed/Failed

- Maven: `17 passed, 0 failed` across candidate-generation, continuous-learning, and authenticated review integration tests.
- Python: `13 passed, 0 failed` across dataset promotion, dataset validation, and review-queue tests.
- `git diff --check`: passed.

## Remaining Blockers

- The current PostgreSQL evaluation store contains no eligible real governed results, so production `dataset-v0.1` remains empty.
- Existing synthetic pilot fixtures and the JSONL development queue remain explicitly blocked from production candidate generation.
- Migration `V30__evaluation_training_candidate_bridge.sql` must be applied to the target PostgreSQL environment before using the endpoint there.
- The generic model-level evaluation run cannot produce examples by itself; candidates require completed per-scenario evaluation results with preserved input, output, evidence, and citations.

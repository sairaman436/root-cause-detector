-- Purpose: Preserves per-scenario evaluation provenance on governed learning records.
-- Why it exists: Eligible evaluation results must become reviewable candidates exactly once without auto-approval or synthetic-data promotion.
-- Architecture fit: Adds the evaluation-to-learning boundary while reusing the existing candidate and human-review workflow.

ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS evaluation_result_id UUID REFERENCES evaluation.pilot_scenario_results(id);
ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS evaluation_score NUMERIC(5, 4);
ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS evaluation_metadata_json TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uk_learning_records_evaluation_result
    ON learning.learning_records(evaluation_result_id)
    WHERE evaluation_result_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_learning_records_evaluation_result
    ON learning.learning_records(evaluation_result_id, approval_status, training_eligible);

-- Purpose: Distinguishes development fixtures from controlled pilot evaluations.
-- Why it exists: The original pilot schema used one SYNTHETIC flag for both categories,
-- preventing a governed pilot scenario from entering the existing human-review queue.
-- Architecture fit: Extends the evaluation bounded context and preserves the existing
-- learning/review boundary; it never approves or promotes training data.

ALTER TABLE evaluation.pilot_runs
    ADD COLUMN IF NOT EXISTS evaluation_classification VARCHAR(40) NOT NULL DEFAULT 'DEVELOPMENT_SYNTHETIC',
    ADD COLUMN IF NOT EXISTS review_status VARCHAR(40) NOT NULL DEFAULT 'PENDING';

ALTER TABLE evaluation.pilot_scenarios
    ADD COLUMN IF NOT EXISTS evaluation_classification VARCHAR(40) NOT NULL DEFAULT 'DEVELOPMENT_SYNTHETIC',
    ADD COLUMN IF NOT EXISTS scenario_provenance_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS review_status VARCHAR(40) NOT NULL DEFAULT 'PENDING';

ALTER TABLE evaluation.pilot_scenario_results
    ADD COLUMN IF NOT EXISTS evaluation_classification VARCHAR(40) NOT NULL DEFAULT 'DEVELOPMENT_SYNTHETIC',
    ADD COLUMN IF NOT EXISTS review_status VARCHAR(40) NOT NULL DEFAULT 'PENDING';

ALTER TABLE evaluation.pilot_runs
    ADD CONSTRAINT ck_pilot_run_classification
        CHECK (evaluation_classification IN ('DEVELOPMENT_SYNTHETIC', 'PILOT_EVALUATION'));

ALTER TABLE evaluation.pilot_scenarios
    ADD CONSTRAINT ck_pilot_scenario_classification
        CHECK (evaluation_classification IN ('DEVELOPMENT_SYNTHETIC', 'PILOT_EVALUATION'));

ALTER TABLE evaluation.pilot_scenario_results
    ADD CONSTRAINT ck_pilot_result_classification
        CHECK (evaluation_classification IN ('DEVELOPMENT_SYNTHETIC', 'PILOT_EVALUATION'));

ALTER TABLE evaluation.pilot_runs
    ADD CONSTRAINT ck_pilot_run_review_status
        CHECK (review_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'CORRECTED', 'REJECTED'));

ALTER TABLE evaluation.pilot_scenarios
    ADD CONSTRAINT ck_pilot_scenario_review_status
        CHECK (review_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'CORRECTED', 'REJECTED'));

ALTER TABLE evaluation.pilot_scenario_results
    ADD CONSTRAINT ck_pilot_result_review_status
        CHECK (review_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'CORRECTED', 'REJECTED'));

CREATE INDEX IF NOT EXISTS idx_pilot_runs_classification_status
    ON evaluation.pilot_runs(evaluation_classification, review_status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_pilot_scenarios_classification_status
    ON evaluation.pilot_scenarios(evaluation_classification, review_status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_pilot_results_classification_status
    ON evaluation.pilot_scenario_results(evaluation_classification, review_status, evaluated_at DESC);

-- ============================================================
-- V28 — Pilot Evaluation Framework (Rural Intelligence Platform)
-- ============================================================
-- Purpose: Adds the reproducible pilot evaluation framework schema.
-- Why it exists: Before using pipeline outputs for any downstream purpose,
--   reliability must be measured across survey → evidence → RAG → root cause →
--   recommendation → human review for all 12 synthetic scenario types.
-- Architecture fit: Extends the evaluation schema without modifying the model,
--   production prompts, or existing tables.
-- CRITICAL: All evaluation scenarios contain SYNTHETIC data only.
--   Do not use real village statistics.
-- ============================================================

-- ----------------------------------------------------------------
-- SCENARIO DATASET
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_datasets (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dataset_key      VARCHAR(120) NOT NULL UNIQUE,
    name             VARCHAR(240) NOT NULL,
    version          VARCHAR(40)  NOT NULL DEFAULT 'v1',
    description      TEXT,
    domain_coverage  JSONB        NOT NULL DEFAULT '[]'::jsonb,
    scenario_count   INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    frozen_at        TIMESTAMPTZ,
    CONSTRAINT ck_pilot_dataset_version CHECK (char_length(version) > 0)
);

CREATE TABLE IF NOT EXISTS evaluation.pilot_scenarios (
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dataset_id                    UUID NOT NULL REFERENCES evaluation.pilot_datasets(id) ON DELETE CASCADE,
    scenario_id                   VARCHAR(120) NOT NULL,
    synthetic_label               VARCHAR(80)  NOT NULL DEFAULT 'SYNTHETIC',
    village_context               TEXT         NOT NULL,
    domain                        VARCHAR(80)  NOT NULL,
    problem_statement             TEXT         NOT NULL,
    survey_data_json              JSONB        NOT NULL DEFAULT '{}'::jsonb,
    evidence_json                 JSONB        NOT NULL DEFAULT '[]'::jsonb,
    knowledge_documents_json      JSONB        NOT NULL DEFAULT '[]'::jsonb,
    expected_relevant_evidence    JSONB        NOT NULL DEFAULT '[]'::jsonb,
    expected_root_cause_candidates JSONB       NOT NULL DEFAULT '[]'::jsonb,
    expected_uncertainties        JSONB        NOT NULL DEFAULT '[]'::jsonb,
    expected_recommendation_categories JSONB  NOT NULL DEFAULT '[]'::jsonb,
    evaluation_notes              TEXT,
    adversarial                   BOOLEAN      NOT NULL DEFAULT FALSE,
    adversarial_type              VARCHAR(120),
    created_at                    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_pilot_scenario_dataset UNIQUE (dataset_id, scenario_id),
    CONSTRAINT ck_pilot_scenario_synthetic CHECK (synthetic_label = 'SYNTHETIC'),
    CONSTRAINT ck_pilot_scenario_domain CHECK (domain IN (
        'WATER', 'AGRICULTURE', 'EDUCATION', 'EMPLOYMENT',
        'HEALTHCARE', 'SANITATION', 'INFRASTRUCTURE', 'LIVELIHOOD',
        'MULTI_DOMAIN', 'ADVERSARIAL'
    ))
);

-- ----------------------------------------------------------------
-- EVALUATION RUNS (pilot-specific; links to scenarios)
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_runs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dataset_id          UUID NOT NULL REFERENCES evaluation.pilot_datasets(id),
    run_label           VARCHAR(180) NOT NULL,
    pipeline_mode       VARCHAR(80)  NOT NULL,  -- BASE_QWEN, QWEN_RAG, FULL_PIPELINE
    model               VARCHAR(120) NOT NULL,
    model_version       VARCHAR(120) NOT NULL,
    prompt_version      VARCHAR(120) NOT NULL,
    knowledge_snapshot  VARCHAR(160) NOT NULL,
    status              VARCHAR(40)  NOT NULL DEFAULT 'PENDING',
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    total_scenarios     INTEGER NOT NULL DEFAULT 0,
    passed_scenarios    INTEGER NOT NULL DEFAULT 0,
    failed_scenarios    INTEGER NOT NULL DEFAULT 0,
    run_metadata_json   JSONB   NOT NULL DEFAULT '{}'::jsonb,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_pilot_run_pipeline_mode CHECK (pipeline_mode IN ('BASE_QWEN', 'QWEN_RAG', 'FULL_PIPELINE')),
    CONSTRAINT ck_pilot_run_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'PARTIAL'))
);

-- ----------------------------------------------------------------
-- PER-SCENARIO RESULTS
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_scenario_results (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_run_id                UUID NOT NULL REFERENCES evaluation.pilot_runs(id) ON DELETE CASCADE,
    scenario_id                 UUID NOT NULL REFERENCES evaluation.pilot_scenarios(id),

    -- Root cause evaluation
    problem_understanding_score     NUMERIC(5,4),
    fact_extraction_score           NUMERIC(5,4),
    evidence_groundedness_score     NUMERIC(5,4),
    root_cause_relevance_score      NUMERIC(5,4),
    alt_hypothesis_quality_score    NUMERIC(5,4),
    contradiction_detection_score   NUMERIC(5,4),
    missing_evidence_detection_score NUMERIC(5,4),
    uncertainty_handling_score      NUMERIC(5,4),
    citation_accuracy_score         NUMERIC(5,4),

    -- Recommendation evaluation
    root_cause_alignment_score      NUMERIC(5,4),
    rec_evidence_groundedness_score NUMERIC(5,4),
    recommendation_relevance_score  NUMERIC(5,4),
    option_diversity_score          NUMERIC(5,4),
    feasibility_reasoning_score     NUMERIC(5,4),
    risk_identification_score       NUMERIC(5,4),
    scheme_matching_score           NUMERIC(5,4),
    implementation_planning_score   NUMERIC(5,4),

    -- Hallucination evaluation
    unsupported_claims_count        INTEGER NOT NULL DEFAULT 0,
    false_citations_count           INTEGER NOT NULL DEFAULT 0,
    invented_statistics_count       INTEGER NOT NULL DEFAULT 0,
    invented_schemes_count          INTEGER NOT NULL DEFAULT 0,
    false_eligibility_count         INTEGER NOT NULL DEFAULT 0,
    overconfident_conclusions_count INTEGER NOT NULL DEFAULT 0,

    -- Overall
    overall_score               NUMERIC(5,4),
    pass                        BOOLEAN,
    latency_ms                  BIGINT,
    pipeline_output_json        JSONB  NOT NULL DEFAULT '{}'::jsonb,
    evaluated_at                TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_pilot_result_run_scenario UNIQUE (pilot_run_id, scenario_id)
);

-- ----------------------------------------------------------------
-- CITATION VALIDATION RECORDS (per claim per result)
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_citation_checks (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scenario_result_id      UUID NOT NULL REFERENCES evaluation.pilot_scenario_results(id) ON DELETE CASCADE,
    claim_text              TEXT NOT NULL,
    cited_source            TEXT,
    citation_exists         BOOLEAN,
    citation_resolves       BOOLEAN,
    citation_supports_claim BOOLEAN,
    citation_is_relevant    BOOLEAN,
    citation_correct_version BOOLEAN,
    failure_reason          TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------
-- HUMAN REVIEW RECORDS (for pilot evaluation workflow)
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_human_reviews (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scenario_result_id      UUID NOT NULL REFERENCES evaluation.pilot_scenario_results(id),
    reviewer_id             UUID,
    root_cause_quality      INTEGER CHECK (root_cause_quality BETWEEN 1 AND 5),
    evidence_quality        INTEGER CHECK (evidence_quality BETWEEN 1 AND 5),
    recommendation_quality  INTEGER CHECK (recommendation_quality BETWEEN 1 AND 5),
    clarity                 INTEGER CHECK (clarity BETWEEN 1 AND 5),
    practical_usefulness    INTEGER CHECK (practical_usefulness BETWEEN 1 AND 5),
    confidence_calibration  INTEGER CHECK (confidence_calibration BETWEEN 1 AND 5),
    overall_usefulness      INTEGER CHECK (overall_usefulness BETWEEN 1 AND 5),
    comments                TEXT,
    accepts_root_cause      BOOLEAN,
    accepts_recommendation  BOOLEAN,
    reviewed_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_pilot_human_review_result_reviewer UNIQUE (scenario_result_id, reviewer_id)
);

-- ----------------------------------------------------------------
-- ERROR RECORDS (taxonomy-labelled failures)
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_error_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scenario_result_id  UUID NOT NULL REFERENCES evaluation.pilot_scenario_results(id) ON DELETE CASCADE,
    error_type          VARCHAR(80) NOT NULL,
    description         TEXT NOT NULL,
    severity            VARCHAR(40) NOT NULL DEFAULT 'MEDIUM',
    field               VARCHAR(120),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_pilot_error_type CHECK (error_type IN (
        'FACTUAL_ERROR', 'UNSUPPORTED_INFERENCE', 'WRONG_ROOT_CAUSE',
        'MISSED_ROOT_CAUSE', 'FALSE_CITATION', 'MISSING_CITATION',
        'CONTRADICTION_MISSED', 'INSUFFICIENT_EVIDENCE_IGNORED',
        'UNSUPPORTED_RECOMMENDATION', 'SCHEME_MISMATCH',
        'OVERCONFIDENCE', 'UNDERCONFIDENCE', 'OTHER'
    ))
);

-- ----------------------------------------------------------------
-- AGGREGATE METRICS (per pilot run)
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_run_metrics (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_run_id                    UUID NOT NULL UNIQUE REFERENCES evaluation.pilot_runs(id) ON DELETE CASCADE,

    -- Root cause metrics
    root_cause_accuracy             NUMERIC(5,4),
    evidence_groundedness           NUMERIC(5,4),
    citation_accuracy               NUMERIC(5,4),
    contradiction_detection_rate    NUMERIC(5,4),
    missing_evidence_detection_rate NUMERIC(5,4),

    -- Hallucination metrics
    unsupported_claim_rate          NUMERIC(5,4),
    hallucination_rate              NUMERIC(5,4),

    -- Recommendation metrics
    recommendation_relevance        NUMERIC(5,4),
    recommendation_acceptance_rate  NUMERIC(5,4),

    -- Human agreement
    human_agreement_rate            NUMERIC(5,4),

    -- Operational metrics
    average_latency_ms              NUMERIC(12,2),
    failure_rate                    NUMERIC(5,4),

    -- Unmeasurable fields (set to NULL with a documented reason in notes_json)
    notes_json                      JSONB NOT NULL DEFAULT '{}'::jsonb,

    computed_at                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ----------------------------------------------------------------
-- BENCHMARK VERSION REGISTRY
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS evaluation.pilot_benchmark_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_key     VARCHAR(120) NOT NULL UNIQUE,
    description     TEXT NOT NULL,
    dataset_id      UUID REFERENCES evaluation.pilot_datasets(id),
    released_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    release_notes   TEXT
);

-- ----------------------------------------------------------------
-- INDEXES
-- ----------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_pilot_scenarios_dataset_domain    ON evaluation.pilot_scenarios(dataset_id, domain);
CREATE INDEX IF NOT EXISTS idx_pilot_scenarios_adversarial       ON evaluation.pilot_scenarios(adversarial);
CREATE INDEX IF NOT EXISTS idx_pilot_runs_dataset_mode           ON evaluation.pilot_runs(dataset_id, pipeline_mode, status);
CREATE INDEX IF NOT EXISTS idx_pilot_scenario_results_run        ON evaluation.pilot_scenario_results(pilot_run_id, pass);
CREATE INDEX IF NOT EXISTS idx_pilot_scenario_results_scenario   ON evaluation.pilot_scenario_results(scenario_id);
CREATE INDEX IF NOT EXISTS idx_pilot_citation_checks_result      ON evaluation.pilot_citation_checks(scenario_result_id);
CREATE INDEX IF NOT EXISTS idx_pilot_human_reviews_result        ON evaluation.pilot_human_reviews(scenario_result_id);
CREATE INDEX IF NOT EXISTS idx_pilot_error_records_result        ON evaluation.pilot_error_records(scenario_result_id, error_type);
CREATE INDEX IF NOT EXISTS idx_pilot_error_records_type          ON evaluation.pilot_error_records(error_type, severity);

-- ----------------------------------------------------------------
-- PERMISSIONS
-- ----------------------------------------------------------------

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000002801', 'PILOT_EVAL_READ',   'PILOT_EVALUATION', 'READ',   'View pilot evaluation scenarios, runs, results, and metrics', NOW(), NOW()),
('00000000-0000-0000-0000-000000002802', 'PILOT_EVAL_RUN',    'PILOT_EVALUATION', 'RUN',    'Execute pilot evaluation runs against the synthetic dataset', NOW(), NOW()),
('00000000-0000-0000-0000-000000002803', 'PILOT_EVAL_REVIEW', 'PILOT_EVALUATION', 'REVIEW', 'Submit human review scores for pilot scenarios', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('PILOT_EVAL_READ', 'PILOT_EVAL_RUN', 'PILOT_EVAL_REVIEW')
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------
-- SEED: default pilot dataset (scenarios loaded at runtime by
--       PilotEvaluationService.seedDataset())
-- ----------------------------------------------------------------

INSERT INTO evaluation.pilot_datasets (id, dataset_key, name, version, description, domain_coverage)
VALUES (
    '00000000-0000-0000-0000-000000002850',
    'rural-intelligence-pilot-v1',
    'Rural Intelligence Pilot Evaluation Dataset v1',
    'v1',
    'Versioned synthetic evaluation dataset covering 12 rural scenario types. All data is SYNTHETIC. No real village statistics are used.',
    '["WATER","AGRICULTURE","EDUCATION","EMPLOYMENT","HEALTHCARE","SANITATION","INFRASTRUCTURE","LIVELIHOOD","MULTI_DOMAIN","ADVERSARIAL"]'::jsonb
)
ON CONFLICT (dataset_key) DO NOTHING;

INSERT INTO evaluation.pilot_benchmark_versions (id, version_key, description, dataset_id)
VALUES (
    '00000000-0000-0000-0000-000000002860',
    'pilot-v1.0.0',
    'Initial versioned pilot evaluation benchmark for the Rural Intelligence Platform. Regression baseline.',
    '00000000-0000-0000-0000-000000002850'
)
ON CONFLICT (version_key) DO NOTHING;

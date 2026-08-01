-- Purpose: Adds the enterprise AI evaluation platform schema for AI-5.
-- Why it exists: Every model must be evaluated automatically, repeatedly, immutably, and with full audit history before production deployment decisions.
-- Architecture fit: Implements benchmark, safety, red-team, hallucination, citation, comparison, and approval records without retraining, deploying, or merging adapters.

CREATE SCHEMA IF NOT EXISTS evaluation;

CREATE TABLE evaluation.benchmark_suites (
    id UUID PRIMARY KEY,
    suite_key VARCHAR(160) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(120) NOT NULL,
    dataset_type VARCHAR(120) NOT NULL,
    version VARCHAR(40) NOT NULL,
    status VARCHAR(60) NOT NULL,
    criteria_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE evaluation.evaluation_runs (
    id UUID PRIMARY KEY,
    model_run_id UUID NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    model_family VARCHAR(80) NOT NULL,
    evaluation_type VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    recommendation VARCHAR(60) NOT NULL,
    overall_score NUMERIC(5, 4) NOT NULL,
    immutable_hash VARCHAR(128) NOT NULL,
    audit_json TEXT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL
);

CREATE TABLE evaluation.benchmark_runs (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    benchmark_suite_id UUID NOT NULL,
    score NUMERIC(5, 4) NOT NULL,
    status VARCHAR(60) NOT NULL,
    result_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_benchmark_runs_evaluation FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id),
    CONSTRAINT fk_benchmark_runs_suite FOREIGN KEY (benchmark_suite_id) REFERENCES evaluation.benchmark_suites(id)
);

CREATE TABLE evaluation.evaluation_metrics (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    accuracy NUMERIC(5, 4) NOT NULL,
    precision_score NUMERIC(5, 4) NOT NULL,
    recall_score NUMERIC(5, 4) NOT NULL,
    f1_score NUMERIC(5, 4) NOT NULL,
    hallucination_rate NUMERIC(5, 4) NOT NULL,
    citation_accuracy NUMERIC(5, 4) NOT NULL,
    latency_ms NUMERIC(12, 2) NOT NULL,
    vram_gb NUMERIC(8, 2) NOT NULL,
    gpu_time_seconds NUMERIC(12, 2) NOT NULL,
    token_usage INTEGER NOT NULL,
    reasoning_quality NUMERIC(5, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_evaluation_metrics_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.safety_tests (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    test_type VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    risk_score NUMERIC(5, 4) NOT NULL,
    findings_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_safety_tests_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.red_team_runs (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    attack_type VARCHAR(120) NOT NULL,
    outcome VARCHAR(80) NOT NULL,
    severity_score NUMERIC(5, 4) NOT NULL,
    evidence_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_red_team_runs_evaluation FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.hallucination_reports (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    hallucination_rate NUMERIC(5, 4) NOT NULL,
    unsupported_claim_count INTEGER NOT NULL,
    findings_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_hallucination_reports_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.citation_reports (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    citation_accuracy NUMERIC(5, 4) NOT NULL,
    broken_citation_count INTEGER NOT NULL,
    unsupported_citation_count INTEGER NOT NULL,
    findings_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_citation_reports_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.model_comparisons (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    baseline_type VARCHAR(120) NOT NULL,
    baseline_model VARCHAR(255) NOT NULL,
    candidate_score NUMERIC(5, 4) NOT NULL,
    baseline_score NUMERIC(5, 4) NOT NULL,
    recommendation VARCHAR(60) NOT NULL,
    comparison_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_model_comparisons_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE TABLE evaluation.evaluation_approvals (
    id UUID PRIMARY KEY,
    evaluation_run_id UUID NOT NULL,
    review_board VARCHAR(160) NOT NULL,
    status VARCHAR(80) NOT NULL,
    reviewer VARCHAR(160) NOT NULL,
    decision_notes TEXT NOT NULL,
    decided_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_evaluation_approvals_run FOREIGN KEY (evaluation_run_id) REFERENCES evaluation.evaluation_runs(id)
);

CREATE INDEX idx_evaluation_runs_model_status ON evaluation.evaluation_runs(model_run_id, status, recommendation);
CREATE INDEX idx_benchmark_runs_eval_suite ON evaluation.benchmark_runs(evaluation_run_id, benchmark_suite_id);
CREATE INDEX idx_evaluation_metrics_run ON evaluation.evaluation_metrics(evaluation_run_id);
CREATE INDEX idx_safety_tests_run_type ON evaluation.safety_tests(evaluation_run_id, test_type);
CREATE INDEX idx_red_team_runs_run_attack ON evaluation.red_team_runs(evaluation_run_id, attack_type);
CREATE INDEX idx_hallucination_reports_run ON evaluation.hallucination_reports(evaluation_run_id);
CREATE INDEX idx_citation_reports_run ON evaluation.citation_reports(evaluation_run_id);
CREATE INDEX idx_model_comparisons_run ON evaluation.model_comparisons(evaluation_run_id, recommendation);
CREATE INDEX idx_evaluation_approvals_run_board ON evaluation.evaluation_approvals(evaluation_run_id, review_board);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001601', 'EVALUATION_READ', 'EVALUATION', 'READ', 'Read evaluation results, benchmarks, safety reports, comparisons, and approvals', NOW(), NOW()),
('00000000-0000-0000-0000-000000001602', 'EVALUATION_RUN', 'EVALUATION', 'RUN', 'Run immutable model evaluation suites without retraining or deployment', NOW(), NOW()),
('00000000-0000-0000-0000-000000001603', 'EVALUATION_PROMOTE', 'EVALUATION', 'PROMOTE', 'Record promotion recommendations without deployment', NOW(), NOW()),
('00000000-0000-0000-0000-000000001604', 'EVALUATION_REJECT', 'EVALUATION', 'REJECT', 'Record rejection decisions without retraining', NOW(), NOW()),
('00000000-0000-0000-0000-000000001605', 'AI_SAFETY_REVIEW', 'EVALUATION_SAFETY', 'REVIEW', 'Review AI safety evaluation findings', NOW(), NOW()),
('00000000-0000-0000-0000-000000001606', 'RED_TEAM_REVIEW', 'EVALUATION_RED_TEAM', 'REVIEW', 'Review red-team attack results', NOW(), NOW()),
('00000000-0000-0000-0000-000000001607', 'GOVERNMENT_POLICY_REVIEW', 'EVALUATION_POLICY', 'REVIEW', 'Review government policy and rural domain evaluation results', NOW(), NOW()),
('00000000-0000-0000-0000-000000001608', 'RELEASE_REVIEW', 'EVALUATION_RELEASE', 'REVIEW', 'Record final release review decisions from evaluation boards', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('EVALUATION_READ', 'EVALUATION_RUN', 'EVALUATION_PROMOTE', 'EVALUATION_REJECT', 'AI_SAFETY_REVIEW', 'RED_TEAM_REVIEW', 'GOVERNMENT_POLICY_REVIEW', 'RELEASE_REVIEW');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('EVALUATION_READ', 'AI_SAFETY_REVIEW', 'GOVERNMENT_POLICY_REVIEW');

INSERT INTO evaluation.benchmark_suites (id, suite_key, name, category, dataset_type, version, status, criteria_json, created_at)
VALUES
('00000000-0000-0000-0000-000000001651', 'survey-understanding', 'Survey Understanding', 'MODEL_QUALITY', 'VALIDATION', 'v1', 'ACTIVE', '{"reproducible":true}', NOW()),
('00000000-0000-0000-0000-000000001652', 'government-policies', 'Government Policies', 'POLICY_REASONING', 'POLICY_QUESTIONS', 'v1', 'ACTIVE', '{"reproducible":true}', NOW()),
('00000000-0000-0000-0000-000000001653', 'root-cause-reasoning', 'Root Cause Reasoning', 'REASONING', 'BLIND_TESTING', 'v1', 'ACTIVE', '{"reproducible":true}', NOW()),
('00000000-0000-0000-0000-000000001654', 'citation-accuracy', 'Citation Accuracy', 'CITATION', 'REGRESSION_TESTING', 'v1', 'ACTIVE', '{"reproducible":true}', NOW()),
('00000000-0000-0000-0000-000000001655', 'red-team-safety', 'Red Team Safety', 'SAFETY', 'ADVERSARIAL_TESTING', 'v1', 'ACTIVE', '{"reproducible":true}', NOW());

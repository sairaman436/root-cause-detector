-- Purpose: Creates the Enterprise Root Cause Discovery and Decision Intelligence schema for Milestone 10.
-- Why it exists: Decisions, root causes, hypotheses, recommendations, confidence, rules, traces, feedback, and audits must be durable and explainable.
-- Architecture fit: Adds decision intelligence infrastructure without predictive ML training, dashboards, external government APIs, satellite data, or IoT integrations.

CREATE SCHEMA IF NOT EXISTS decision;

CREATE TABLE decision.decisions (
    id UUID PRIMARY KEY,
    survey_id UUID,
    organization_id UUID,
    requested_by UUID,
    decision_type VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    input_json TEXT NOT NULL,
    final_decision TEXT NOT NULL,
    overall_confidence DOUBLE PRECISION NOT NULL,
    human_approval_required BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE decision.decision_versions (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    decision_json TEXT NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_decision_version UNIQUE (decision_id, version_number)
);

CREATE TABLE decision.root_causes (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    rank INTEGER NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    evidence_json TEXT NOT NULL
);

CREATE TABLE decision.hypotheses (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    rationale TEXT NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    rank INTEGER NOT NULL,
    alternative BOOLEAN NOT NULL
);

CREATE TABLE decision.recommendations (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    priority INTEGER NOT NULL,
    impact_score DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    cost_notes TEXT NOT NULL,
    risk_notes TEXT NOT NULL,
    expected_outcome TEXT NOT NULL,
    human_approval_required BOOLEAN NOT NULL
);

CREATE TABLE decision.recommendation_evidence (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL,
    evidence_type VARCHAR(80) NOT NULL,
    evidence_ref VARCHAR(180) NOT NULL,
    summary TEXT NOT NULL,
    weight DOUBLE PRECISION NOT NULL
);

CREATE TABLE decision.decision_traces (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    step_number INTEGER NOT NULL,
    step_name VARCHAR(120) NOT NULL,
    details_json TEXT NOT NULL,
    confidence_after_step DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE decision.confidence_scores (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL UNIQUE,
    overall_confidence DOUBLE PRECISION NOT NULL,
    evidence_completeness DOUBLE PRECISION NOT NULL,
    knowledge_coverage DOUBLE PRECISION NOT NULL,
    ml_confidence DOUBLE PRECISION NOT NULL,
    rule_consistency DOUBLE PRECISION NOT NULL,
    historical_similarity DOUBLE PRECISION NOT NULL,
    agent_agreement DOUBLE PRECISION NOT NULL,
    contradictory_evidence DOUBLE PRECISION NOT NULL,
    reason_codes_json TEXT NOT NULL,
    missing_evidence_json TEXT NOT NULL,
    required_followups_json TEXT NOT NULL
);

CREATE TABLE decision.decision_rules (
    id UUID PRIMARY KEY,
    rule_key VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    rule_type VARCHAR(80) NOT NULL,
    dsl_expression TEXT NOT NULL,
    priority INTEGER NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE decision.decision_feedback (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    user_id UUID,
    decision_outcome VARCHAR(40) NOT NULL,
    feedback_text VARCHAR(1000),
    override_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE decision.decision_audit (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id UUID,
    outcome VARCHAR(40) NOT NULL,
    details_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_decision_status_time ON decision.decisions (status, created_at);
CREATE INDEX idx_decision_survey ON decision.decisions (survey_id, created_at);
CREATE INDEX idx_root_cause_decision_rank ON decision.root_causes (decision_id, rank);
CREATE INDEX idx_hypothesis_decision_rank ON decision.hypotheses (decision_id, rank);
CREATE INDEX idx_recommendation_decision_priority ON decision.recommendations (decision_id, priority);
CREATE INDEX idx_trace_decision_step ON decision.decision_traces (decision_id, step_number);
CREATE INDEX idx_rule_status_priority ON decision.decision_rules (status, priority);
CREATE INDEX idx_feedback_decision ON decision.decision_feedback (decision_id, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001001', 'DECISION_ANALYZE', 'DECISION', 'ANALYZE', 'Run decision intelligence analysis', NOW(), NOW()),
('00000000-0000-0000-0000-000000001002', 'DECISION_READ', 'DECISION', 'READ', 'Read decisions, explanations, confidence, and history', NOW(), NOW()),
('00000000-0000-0000-0000-000000001003', 'DECISION_REVIEW', 'DECISION', 'REVIEW', 'Review decision outputs and recommendations', NOW(), NOW()),
('00000000-0000-0000-0000-000000001004', 'DECISION_ADMIN', 'DECISION', 'ADMIN', 'Administer decision intelligence rules and governance', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('DECISION_ANALYZE', 'DECISION_READ', 'DECISION_REVIEW', 'DECISION_ADMIN');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000001001'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000001002'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000001001'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000001002'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000001002'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000001003');

INSERT INTO decision.decision_rules (id, rule_key, name, rule_type, dsl_expression, priority, status, created_at)
VALUES
('00000000-0000-0000-0000-000000001011', 'mandatory-evidence', 'Mandatory evidence context', 'MANDATORY_CONDITION', 'context.evidence.size > 0 OR context.surveyEvidence.present', 1, 'ACTIVE', NOW()),
('00000000-0000-0000-0000-000000001012', 'policy-citation-required', 'Policy citation required', 'POLICY_RULE', 'recommendation.policyCitation.required == true', 2, 'ACTIVE', NOW()),
('00000000-0000-0000-0000-000000001013', 'human-approval-consequential', 'Human approval for consequential recommendations', 'GOVERNMENT_CONSTRAINT', 'recommendation.consequential -> humanApproval.required', 3, 'ACTIVE', NOW()),
('00000000-0000-0000-0000-000000001014', 'conflict-detection', 'Contradictory evidence conflict detection', 'CONFLICT_DETECTION', 'context.contradictions.none', 4, 'ACTIVE', NOW());

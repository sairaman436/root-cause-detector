-- Purpose: Creates the recommendation intelligence engine persistence model.
-- Why it exists: Intervention options, evidence, risks, resources, implementation plans, scheme matches, reviews, and versions must be durable and auditable.
-- Architecture fit: Extends the decision schema with decision-support recommendation records without autonomous execution.

CREATE SCHEMA IF NOT EXISTS decision;

CREATE TABLE IF NOT EXISTS decision.recommendation_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    root_cause_analysis_id UUID REFERENCES decision.root_cause_analyses(id),
    organization_id UUID,
    requested_by UUID,
    status VARCHAR(40) NOT NULL DEFAULT 'AI_GENERATED',
    request_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    response_json JSONB NOT NULL,
    model VARCHAR(120) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(120) NOT NULL,
    knowledge_snapshot VARCHAR(160) NOT NULL,
    evidence_snapshot VARCHAR(160) NOT NULL,
    human_approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_recommendation_set_status CHECK (status IN ('AI_GENERATED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'MORE_EVIDENCE_REQUESTED', 'SUPERSEDED'))
);

CREATE TABLE IF NOT EXISTS decision.recommendation_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    title VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    target_root_cause TEXT NOT NULL,
    target_population INTEGER,
    domain VARCHAR(80) NOT NULL,
    intervention_type VARCHAR(120) NOT NULL,
    priority INTEGER NOT NULL,
    expected_outcomes_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    required_resources_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    estimated_effort VARCHAR(80) NOT NULL,
    estimated_timeframe VARCHAR(120) NOT NULL,
    feasibility_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    dependencies_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    assumptions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    limitations_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    status VARCHAR(40) NOT NULL DEFAULT 'AI_GENERATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_recommendation_option_id UNIQUE (recommendation_set_id, recommendation_id)
);

CREATE TABLE IF NOT EXISTS decision.recommendation_evidence_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    evidence_ref TEXT NOT NULL,
    evidence_type VARCHAR(80) NOT NULL,
    grounding_type VARCHAR(80) NOT NULL,
    confidence NUMERIC(8,5) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.recommendation_risks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    risk_type VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(40) NOT NULL,
    likelihood VARCHAR(40) NOT NULL,
    mitigation TEXT NOT NULL,
    evidence_or_assumption TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.recommendation_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    resource_name TEXT NOT NULL,
    resource_status VARCHAR(80) NOT NULL,
    evidence_or_gap TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.recommendation_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    metric_name VARCHAR(180) NOT NULL,
    baseline TEXT NOT NULL,
    target TEXT NOT NULL,
    measurement_method TEXT NOT NULL,
    measurement_frequency VARCHAR(120) NOT NULL,
    data_gap TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.recommendation_scheme_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120),
    scheme_name VARCHAR(240) NOT NULL,
    source TEXT NOT NULL,
    eligibility_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    applicable_population TEXT NOT NULL,
    relevant_benefit TEXT NOT NULL,
    limitations_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    source_date_version VARCHAR(120) NOT NULL,
    status VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.implementation_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    recommendation_id VARCHAR(120) NOT NULL,
    phase VARCHAR(80) NOT NULL,
    actions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    responsible_role VARCHAR(180) NOT NULL,
    required_inputs_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    dependencies_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    success_indicators_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.recommendation_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    reviewer_user_id UUID,
    action VARCHAR(40) NOT NULL,
    reviewer_notes TEXT,
    modified_recommendation_json JSONB,
    correction TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_recommendation_review_action CHECK (action IN ('ACCEPT', 'EDIT', 'REJECT', 'REQUEST_MORE_EVIDENCE', 'APPROVE'))
);

CREATE TABLE IF NOT EXISTS decision.recommendation_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_set_id UUID NOT NULL REFERENCES decision.recommendation_sets(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    root_cause_analysis_version VARCHAR(80) NOT NULL,
    response_json JSONB NOT NULL,
    model VARCHAR(120) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(120) NOT NULL,
    knowledge_snapshot VARCHAR(160) NOT NULL,
    evidence_snapshot VARCHAR(160) NOT NULL,
    reviewer_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_recommendation_version UNIQUE (recommendation_set_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_recommendation_sets_root_cause ON decision.recommendation_sets(root_cause_analysis_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_recommendation_sets_status ON decision.recommendation_sets(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_recommendation_options_set ON decision.recommendation_options(recommendation_set_id, priority);
CREATE INDEX IF NOT EXISTS idx_recommendation_risks_set ON decision.recommendation_risks(recommendation_set_id, recommendation_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_metrics_set ON decision.recommendation_metrics(recommendation_set_id, recommendation_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_scheme_set ON decision.recommendation_scheme_matches(recommendation_set_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_reviews_set ON decision.recommendation_reviews(recommendation_set_id, created_at DESC);

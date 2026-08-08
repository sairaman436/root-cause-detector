-- Purpose: Creates the transparent root-cause intelligence engine persistence model.
-- Why it exists: Root-cause analyses must preserve problems, facts, hypotheses, evidence scoring, causal graph relationships, versions, and human reviews.
-- Architecture fit: Extends the decision schema without replacing the existing decision intelligence module.

CREATE SCHEMA IF NOT EXISTS decision;

CREATE TABLE IF NOT EXISTS decision.root_cause_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_id UUID,
    organization_id UUID,
    requested_by UUID,
    problem_json JSONB NOT NULL,
    analysis_json JSONB NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'GENERATED',
    model VARCHAR(120) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(120) NOT NULL,
    knowledge_snapshot VARCHAR(160) NOT NULL,
    survey_version VARCHAR(80) NOT NULL,
    confidence_score NUMERIC(8,5) NOT NULL,
    human_review_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_root_cause_analysis_status CHECK (status IN ('GENERATED', 'REVIEWED', 'REJECTED', 'SUPERSEDED'))
);

CREATE TABLE IF NOT EXISTS decision.root_cause_problems (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    problem_id VARCHAR(120) NOT NULL,
    village VARCHAR(180),
    domain VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    affected_population INTEGER,
    severity VARCHAR(40),
    source VARCHAR(180),
    problem_timestamp TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.observed_facts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    fact_id VARCHAR(120) NOT NULL,
    statement TEXT NOT NULL,
    source VARCHAR(240) NOT NULL,
    source_type VARCHAR(80) NOT NULL,
    category VARCHAR(80) NOT NULL,
    confidence NUMERIC(8,5) NOT NULL,
    fact_timestamp TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_observed_fact_analysis_fact UNIQUE (analysis_id, fact_id),
    CONSTRAINT ck_observed_fact_category CHECK (category IN ('OBSERVED_FACT', 'RETRIEVED_EVIDENCE', 'MODEL_INFERENCE', 'HYPOTHESIS', 'RECOMMENDATION', 'CONTRADICTORY_EVIDENCE'))
);

CREATE TABLE IF NOT EXISTS decision.contributing_factors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    factor TEXT NOT NULL,
    supporting_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    contradicting_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence NUMERIC(8,5) NOT NULL,
    source VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.root_cause_candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    root_cause_id VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    supporting_facts_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    supporting_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    contradicting_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence NUMERIC(8,5) NOT NULL,
    affected_domain VARCHAR(80) NOT NULL,
    assumptions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    uncertainty TEXT NOT NULL,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    reasoning_summary TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_root_cause_candidate_analysis_cause UNIQUE (analysis_id, root_cause_id)
);

CREATE TABLE IF NOT EXISTS decision.alternative_hypotheses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    hypothesis_id VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    supporting_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    missing_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence NUMERIC(8,5) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_alternative_hypothesis_analysis UNIQUE (analysis_id, hypothesis_id)
);

CREATE TABLE IF NOT EXISTS decision.causal_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    from_node TEXT NOT NULL,
    to_node TEXT NOT NULL,
    relationship_type VARCHAR(80) NOT NULL,
    confidence NUMERIC(8,5) NOT NULL,
    evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    source VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.evidence_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    evidence_id VARCHAR(120) NOT NULL,
    statement TEXT NOT NULL,
    source VARCHAR(240) NOT NULL,
    source_type VARCHAR(80) NOT NULL,
    reliability NUMERIC(8,5) NOT NULL,
    relevance NUMERIC(8,5) NOT NULL,
    freshness NUMERIC(8,5) NOT NULL,
    consistency NUMERIC(8,5) NOT NULL,
    confidence NUMERIC(8,5) NOT NULL,
    category VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.root_cause_uncertainties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    uncertainty_id VARCHAR(120) NOT NULL,
    statement TEXT NOT NULL,
    missing_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    follow_up_questions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    severity VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS decision.root_cause_human_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    reviewer_user_id UUID,
    action VARCHAR(40) NOT NULL,
    reviewer_notes TEXT,
    modified_analysis_json JSONB,
    additional_evidence_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    correction TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_root_cause_review_action CHECK (action IN ('ACCEPT', 'REJECT', 'MODIFY', 'ADD_EVIDENCE', 'FLAG_INCORRECT_REASONING'))
);

CREATE TABLE IF NOT EXISTS decision.root_cause_analysis_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES decision.root_cause_analyses(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    analysis_json JSONB NOT NULL,
    model VARCHAR(120) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(120) NOT NULL,
    knowledge_snapshot VARCHAR(160) NOT NULL,
    survey_version VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_root_cause_analysis_version UNIQUE (analysis_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_root_cause_analyses_status_time ON decision.root_cause_analyses(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_root_cause_analyses_survey ON decision.root_cause_analyses(survey_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_root_cause_problem_domain ON decision.root_cause_problems(domain);
CREATE INDEX IF NOT EXISTS idx_observed_facts_analysis ON decision.observed_facts(analysis_id, category);
CREATE INDEX IF NOT EXISTS idx_contributing_factors_analysis ON decision.contributing_factors(analysis_id);
CREATE INDEX IF NOT EXISTS idx_root_cause_candidates_analysis ON decision.root_cause_candidates(analysis_id, validated, confidence DESC);
CREATE INDEX IF NOT EXISTS idx_alternative_hypotheses_analysis ON decision.alternative_hypotheses(analysis_id);
CREATE INDEX IF NOT EXISTS idx_causal_relationships_analysis ON decision.causal_relationships(analysis_id);
CREATE INDEX IF NOT EXISTS idx_evidence_assessments_analysis ON decision.evidence_assessments(analysis_id);
CREATE INDEX IF NOT EXISTS idx_root_cause_uncertainties_analysis ON decision.root_cause_uncertainties(analysis_id);
CREATE INDEX IF NOT EXISTS idx_root_cause_human_reviews_analysis ON decision.root_cause_human_reviews(analysis_id, created_at DESC);

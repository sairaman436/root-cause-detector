-- Purpose: Creates the AI-7 enterprise continuous learning schema.
-- Why it exists: Operational intelligence must be converted into governed future-training candidates without automatic retraining or deployment.
-- Architecture fit: Adds learning records, feedback, corrections, human review, knowledge deltas, candidates, approvals, metrics, and immutable audits.

CREATE SCHEMA IF NOT EXISTS learning;

CREATE TABLE learning.learning_records (
    id UUID PRIMARY KEY,
    occurred_at TIMESTAMP NOT NULL,
    source_type VARCHAR(120) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(120) NOT NULL,
    retrieved_context TEXT,
    input_text TEXT NOT NULL,
    ai_output_text TEXT NOT NULL,
    human_edited_output_text TEXT,
    accepted_output_text TEXT,
    confidence NUMERIC(5, 4) NOT NULL,
    evidence_used_json TEXT NOT NULL,
    agent_used VARCHAR(160),
    reviewer VARCHAR(180),
    training_eligible BOOLEAN NOT NULL,
    privacy_classification VARCHAR(80) NOT NULL,
    approval_status VARCHAR(80) NOT NULL
);

CREATE TABLE learning.feedback_events (
    id UUID PRIMARY KEY,
    learning_record_id UUID NOT NULL,
    feedback_source VARCHAR(120) NOT NULL,
    feedback_type VARCHAR(120) NOT NULL,
    feedback_text TEXT,
    sentiment VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_feedback_events_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id)
);

CREATE TABLE learning.corrections (
    id UUID PRIMARY KEY,
    learning_record_id UUID NOT NULL,
    correction_type VARCHAR(120) NOT NULL,
    original_text TEXT,
    corrected_text TEXT NOT NULL,
    corrected_by VARCHAR(180) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_corrections_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id)
);

CREATE TABLE learning.human_reviews (
    id UUID PRIMARY KEY,
    learning_record_id UUID NOT NULL,
    reviewer VARCHAR(180) NOT NULL,
    decision VARCHAR(80) NOT NULL,
    escalation_level VARCHAR(120) NOT NULL,
    comments TEXT NOT NULL,
    reviewed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_human_reviews_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id)
);

CREATE TABLE learning.knowledge_deltas (
    id UUID PRIMARY KEY,
    learning_record_id UUID NOT NULL,
    delta_type VARCHAR(120) NOT NULL,
    source_reference VARCHAR(240) NOT NULL,
    delta_summary TEXT NOT NULL,
    refresh_job_status VARCHAR(80) NOT NULL,
    reindex_request_status VARCHAR(80) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_deltas_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id)
);

CREATE TABLE learning.training_candidates (
    id UUID PRIMARY KEY,
    learning_record_id UUID NOT NULL,
    candidate_dataset VARCHAR(180) NOT NULL,
    source VARCHAR(120) NOT NULL,
    quality_score NUMERIC(5, 4) NOT NULL,
    reviewer VARCHAR(180) NOT NULL,
    dataset_lineage VARCHAR(300) NOT NULL,
    training_readiness VARCHAR(100) NOT NULL,
    approval_status VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_candidates_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id)
);

CREATE TABLE learning.approval_workflows (
    id UUID PRIMARY KEY,
    training_candidate_id UUID NOT NULL,
    review_board VARCHAR(160) NOT NULL,
    decision VARCHAR(100) NOT NULL,
    reviewer VARCHAR(180) NOT NULL,
    rationale TEXT NOT NULL,
    decided_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_approval_workflows_candidate FOREIGN KEY (training_candidate_id) REFERENCES learning.training_candidates(id)
);

CREATE TABLE learning.learning_metrics (
    id UUID PRIMARY KEY,
    metric_window VARCHAR(80) NOT NULL,
    feedback_volume INTEGER NOT NULL,
    acceptance_rate NUMERIC(5, 4) NOT NULL,
    correction_rate NUMERIC(5, 4) NOT NULL,
    model_error_categories_json TEXT NOT NULL,
    learning_dataset_growth INTEGER NOT NULL,
    knowledge_update_count INTEGER NOT NULL,
    training_candidate_count INTEGER NOT NULL,
    measured_at TIMESTAMP NOT NULL
);

CREATE TABLE learning.learning_audits (
    id UUID PRIMARY KEY,
    learning_record_id UUID,
    training_candidate_id UUID,
    event_type VARCHAR(120) NOT NULL,
    actor VARCHAR(180) NOT NULL,
    event_json TEXT NOT NULL,
    immutable_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_learning_audits_record FOREIGN KEY (learning_record_id) REFERENCES learning.learning_records(id),
    CONSTRAINT fk_learning_audits_candidate FOREIGN KEY (training_candidate_id) REFERENCES learning.training_candidates(id)
);

CREATE INDEX idx_learning_records_source_status ON learning.learning_records(source_type, approval_status, privacy_classification);
CREATE INDEX idx_feedback_events_record_type ON learning.feedback_events(learning_record_id, feedback_type);
CREATE INDEX idx_human_reviews_record_decision ON learning.human_reviews(learning_record_id, decision);
CREATE INDEX idx_knowledge_deltas_type_status ON learning.knowledge_deltas(delta_type, refresh_job_status, reindex_request_status);
CREATE INDEX idx_training_candidates_status_readiness ON learning.training_candidates(approval_status, training_readiness, quality_score);
CREATE INDEX idx_approval_workflows_candidate_board ON learning.approval_workflows(training_candidate_id, review_board, decision);
CREATE INDEX idx_learning_metrics_window ON learning.learning_metrics(metric_window, measured_at);
CREATE INDEX idx_learning_audits_record_event ON learning.learning_audits(learning_record_id, event_type, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001801', 'LEARNING_READ', 'LEARNING', 'READ', 'Read continuous learning records, candidates, history, and metrics', NOW(), NOW()),
('00000000-0000-0000-0000-000000001802', 'LEARNING_CAPTURE', 'LEARNING', 'CAPTURE', 'Capture human feedback, expert corrections, AI mistakes, and knowledge updates', NOW(), NOW()),
('00000000-0000-0000-0000-000000001803', 'LEARNING_REVIEW', 'LEARNING', 'REVIEW', 'Review learning records and create governed training candidates', NOW(), NOW()),
('00000000-0000-0000-0000-000000001804', 'LEARNING_PROMOTE', 'LEARNING', 'PROMOTE', 'Promote candidates for future dataset preparation without retraining', NOW(), NOW()),
('00000000-0000-0000-0000-000000001805', 'LEARNING_REJECT', 'LEARNING', 'REJECT', 'Reject candidates from future training datasets', NOW(), NOW()),
('00000000-0000-0000-0000-000000001806', 'DATASET_APPROVAL', 'LEARNING_DATASET', 'APPROVE', 'Approve continuous learning candidates for future datasets', NOW(), NOW()),
('00000000-0000-0000-0000-000000001807', 'AI_GOVERNANCE_REVIEW', 'LEARNING_GOVERNANCE', 'REVIEW', 'Review continuous learning safety, privacy, and governance controls', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('LEARNING_READ', 'LEARNING_CAPTURE', 'LEARNING_REVIEW', 'LEARNING_PROMOTE', 'LEARNING_REJECT', 'DATASET_APPROVAL', 'AI_GOVERNANCE_REVIEW');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('LEARNING_READ', 'LEARNING_REVIEW', 'AI_GOVERNANCE_REVIEW');

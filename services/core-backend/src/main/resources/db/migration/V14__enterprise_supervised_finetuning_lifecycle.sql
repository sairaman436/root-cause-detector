-- Purpose: Adds the supervised fine-tuning lifecycle schema for AI-4.
-- Why it exists: The first Rural Intelligence Foundation Model adapter requires durable runs, adapter versions, metrics, evaluation gates, model cards, reports, approvals, and rollback metadata.
-- Architecture fit: Implements AI-4 lifecycle recording without deploying, merging, or replacing production models.

CREATE SCHEMA IF NOT EXISTS finetuning;

CREATE TABLE finetuning.fine_tuning_runs (
    id UUID PRIMARY KEY,
    run_name VARCHAR(255) NOT NULL,
    selected_base_model VARCHAR(255) NOT NULL,
    selected_model_family VARCHAR(80) NOT NULL,
    training_strategy VARCHAR(160) NOT NULL,
    status VARCHAR(80) NOT NULL,
    dataset_source_type VARCHAR(80) NOT NULL,
    dataset_id UUID NOT NULL,
    lineage_json TEXT NOT NULL,
    benchmark_report_json TEXT NOT NULL,
    review_status VARCHAR(120) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE finetuning.adapter_versions (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    adapter_type VARCHAR(80) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    version_number INTEGER NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    status VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_adapter_versions_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id),
    CONSTRAINT uq_adapter_version UNIQUE (run_id, adapter_type, version_number)
);

CREATE TABLE finetuning.training_metrics (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    loss_value NUMERIC(12, 6) NOT NULL,
    validation_loss NUMERIC(12, 6) NOT NULL,
    learning_rate NUMERIC(12, 8) NOT NULL,
    gpu_utilization NUMERIC(5, 4) NOT NULL,
    vram_usage_gb NUMERIC(8, 2) NOT NULL,
    checkpoint_progress NUMERIC(5, 4) NOT NULL,
    training_time_seconds BIGINT NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_finetuning_training_metrics_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id)
);

CREATE TABLE finetuning.evaluation_metrics (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    reasoning_score NUMERIC(5, 4) NOT NULL,
    hallucination_score NUMERIC(5, 4) NOT NULL,
    safety_score NUMERIC(5, 4) NOT NULL,
    citation_accuracy NUMERIC(5, 4) NOT NULL,
    policy_compliance NUMERIC(5, 4) NOT NULL,
    output_formatting NUMERIC(5, 4) NOT NULL,
    latency_ms NUMERIC(12, 2) NOT NULL,
    memory_usage_gb NUMERIC(8, 2) NOT NULL,
    overall_score NUMERIC(5, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_evaluation_metrics_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id)
);

CREATE TABLE finetuning.model_cards (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    base_model VARCHAR(255) NOT NULL,
    intended_use TEXT NOT NULL,
    limitations TEXT NOT NULL,
    license VARCHAR(160) NOT NULL,
    safety_notes TEXT NOT NULL,
    card_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_model_cards_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id)
);

CREATE TABLE finetuning.training_reports (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    report_type VARCHAR(120) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    summary_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_reports_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id)
);

CREATE TABLE finetuning.training_approvals (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    review_board VARCHAR(120) NOT NULL,
    status VARCHAR(80) NOT NULL,
    reviewer VARCHAR(160) NOT NULL,
    decision_notes TEXT NOT NULL,
    decided_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_approvals_run FOREIGN KEY (run_id) REFERENCES finetuning.fine_tuning_runs(id)
);

CREATE INDEX idx_finetuning_runs_status_model ON finetuning.fine_tuning_runs(status, selected_model_family);
CREATE INDEX idx_finetuning_runs_dataset ON finetuning.fine_tuning_runs(dataset_source_type, dataset_id);
CREATE INDEX idx_adapter_versions_run_type ON finetuning.adapter_versions(run_id, adapter_type);
CREATE INDEX idx_finetuning_training_metrics_run_time ON finetuning.training_metrics(run_id, recorded_at);
CREATE INDEX idx_evaluation_metrics_run_overall ON finetuning.evaluation_metrics(run_id, overall_score);
CREATE INDEX idx_model_cards_run_model ON finetuning.model_cards(run_id, model_name);
CREATE INDEX idx_training_reports_run_type ON finetuning.training_reports(run_id, report_type);
CREATE INDEX idx_training_approvals_run_board ON finetuning.training_approvals(run_id, review_board);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001501', 'FINETUNING_READ', 'FINETUNING', 'READ', 'Read fine-tuning runs, models, reports, metrics, and approvals', NOW(), NOW()),
('00000000-0000-0000-0000-000000001502', 'FINETUNING_ENGINEER', 'FINETUNING', 'EXECUTE', 'Execute governed supervised fine-tuning lifecycle and produce adapter artifacts without deployment', NOW(), NOW()),
('00000000-0000-0000-0000-000000001503', 'FINETUNING_ROLLBACK', 'FINETUNING', 'ROLLBACK', 'Rollback fine-tuned adapter release candidates without changing production models', NOW(), NOW()),
('00000000-0000-0000-0000-000000001504', 'AI_RESEARCH_REVIEW', 'FINETUNING_REVIEW', 'APPROVE', 'Review AI research gates for fine-tuning outputs and model cards', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('FINETUNING_READ', 'FINETUNING_ENGINEER', 'FINETUNING_ROLLBACK', 'AI_RESEARCH_REVIEW');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('FINETUNING_READ', 'AI_RESEARCH_REVIEW');

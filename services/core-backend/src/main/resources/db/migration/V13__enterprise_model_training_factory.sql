-- Purpose: Adds the enterprise model training factory schema for AI-3.
-- Why it exists: Future Rural Intelligence Foundation Models require governed job management, experiments, scheduling, GPU resources, checkpoints, artifacts, model registry, adapters, metrics, and audit logs before any training execution.
-- Architecture fit: Implements AI-3 infrastructure only; it does not train, merge, deploy, or serve models.

CREATE SCHEMA IF NOT EXISTS training;

CREATE TABLE training.training_experiments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_team VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    metadata_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE training.hyperparameter_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parameters_json TEXT NOT NULL,
    precision_mode VARCHAR(80) NOT NULL,
    gradient_checkpointing BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE training.training_jobs (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL,
    hyperparameter_set_id UUID NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    base_model VARCHAR(255) NOT NULL,
    model_family VARCHAR(80) NOT NULL,
    training_method VARCHAR(80) NOT NULL,
    dataset_source_type VARCHAR(80) NOT NULL,
    dataset_id UUID NOT NULL,
    status VARCHAR(60) NOT NULL,
    priority INTEGER NOT NULL,
    requested_gpu_count INTEGER NOT NULL,
    requested_vram_gb INTEGER NOT NULL,
    mixed_precision_ready BOOLEAN NOT NULL,
    distributed_ready BOOLEAN NOT NULL,
    resume_enabled BOOLEAN NOT NULL,
    lineage_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_jobs_experiment FOREIGN KEY (experiment_id) REFERENCES training.training_experiments(id),
    CONSTRAINT fk_training_jobs_hyperparameters FOREIGN KEY (hyperparameter_set_id) REFERENCES training.hyperparameter_sets(id)
);

CREATE TABLE training.gpu_resources (
    id UUID PRIMARY KEY,
    resource_name VARCHAR(160) NOT NULL UNIQUE,
    resource_type VARCHAR(80) NOT NULL,
    gpu_count INTEGER NOT NULL,
    total_vram_gb INTEGER NOT NULL,
    allocated_vram_gb INTEGER NOT NULL,
    status VARCHAR(60) NOT NULL,
    last_heartbeat_at TIMESTAMP NOT NULL
);

CREATE TABLE training.training_runs (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    gpu_resource_id UUID,
    status VARCHAR(60) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_seconds BIGINT,
    scheduler_decision VARCHAR(500) NOT NULL,
    CONSTRAINT fk_training_runs_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id),
    CONSTRAINT fk_training_runs_gpu FOREIGN KEY (gpu_resource_id) REFERENCES training.gpu_resources(id)
);

CREATE TABLE training.training_queue (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    priority INTEGER NOT NULL,
    queue_status VARCHAR(60) NOT NULL,
    attempt_count INTEGER NOT NULL,
    available_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_queue_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id)
);

CREATE TABLE training.training_artifacts (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    run_id UUID,
    artifact_type VARCHAR(120) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    integrity_status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_artifacts_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id),
    CONSTRAINT fk_training_artifacts_run FOREIGN KEY (run_id) REFERENCES training.training_runs(id)
);

CREATE TABLE training.training_checkpoints (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    run_id UUID,
    checkpoint_step INTEGER NOT NULL,
    checkpoint_type VARCHAR(80) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    validation_status VARCHAR(60) NOT NULL,
    restorable BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_checkpoints_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id),
    CONSTRAINT fk_training_checkpoints_run FOREIGN KEY (run_id) REFERENCES training.training_runs(id)
);

CREATE TABLE training.training_metrics (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    run_id UUID,
    gpu_utilization NUMERIC(5, 4) NOT NULL,
    vram_usage_gb NUMERIC(8, 2) NOT NULL,
    training_throughput NUMERIC(12, 4) NOT NULL,
    loss_value NUMERIC(12, 6) NOT NULL,
    checkpoint_count INTEGER NOT NULL,
    estimated_cost NUMERIC(12, 4) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_metrics_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id),
    CONSTRAINT fk_training_metrics_run FOREIGN KEY (run_id) REFERENCES training.training_runs(id)
);

CREATE TABLE training.training_logs (
    id UUID PRIMARY KEY,
    job_id UUID,
    log_level VARCHAR(40) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    context_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_logs_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id)
);

CREATE TABLE training.model_registry (
    id UUID PRIMARY KEY,
    job_id UUID,
    model_name VARCHAR(255) NOT NULL,
    model_family VARCHAR(80) NOT NULL,
    base_model VARCHAR(255) NOT NULL,
    parent_model VARCHAR(255),
    model_type VARCHAR(80) NOT NULL,
    license VARCHAR(160) NOT NULL,
    gguf_metadata TEXT NOT NULL,
    ollama_manifest TEXT NOT NULL,
    vllm_metadata TEXT NOT NULL,
    status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_model_registry_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id)
);

CREATE TABLE training.adapter_registry (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    adapter_type VARCHAR(80) NOT NULL,
    base_model VARCHAR(255) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_adapter_registry_job FOREIGN KEY (job_id) REFERENCES training.training_jobs(id)
);

CREATE INDEX idx_training_jobs_status_priority ON training.training_jobs(status, priority);
CREATE INDEX idx_training_jobs_dataset ON training.training_jobs(dataset_source_type, dataset_id);
CREATE INDEX idx_training_runs_job_status ON training.training_runs(job_id, status);
CREATE INDEX idx_training_queue_status_priority ON training.training_queue(queue_status, priority, available_at);
CREATE INDEX idx_training_checkpoints_job_step ON training.training_checkpoints(job_id, checkpoint_step);
CREATE INDEX idx_training_metrics_job_time ON training.training_metrics(job_id, recorded_at);
CREATE INDEX idx_training_logs_job_event ON training.training_logs(job_id, event_type, created_at);
CREATE INDEX idx_model_registry_family_status ON training.model_registry(model_family, status);
CREATE INDEX idx_adapter_registry_job_type ON training.adapter_registry(job_id, adapter_type);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001401', 'TRAINING_READ', 'TRAINING', 'READ', 'Read training jobs, experiments, models, checkpoints, and dashboard metrics', NOW(), NOW()),
('00000000-0000-0000-0000-000000001402', 'TRAINING_ENGINEER', 'TRAINING', 'ENGINEER', 'Create, schedule, cancel, and inspect training factory jobs without executing model training', NOW(), NOW()),
('00000000-0000-0000-0000-000000001403', 'TRAINING_ADMIN', 'TRAINING', 'ADMIN', 'Administer training queue, GPU resources, artifact store, and training audit controls', NOW(), NOW()),
('00000000-0000-0000-0000-000000001404', 'MODEL_REGISTRY_READ', 'MODEL_REGISTRY', 'READ', 'Read governed model registry and version metadata', NOW(), NOW()),
('00000000-0000-0000-0000-000000001405', 'CHECKPOINT_RESTORE', 'TRAINING_CHECKPOINT', 'RESTORE', 'Request checkpoint restore after validation', NOW(), NOW()),
('00000000-0000-0000-0000-000000001406', 'MLOPS_ADMIN', 'MLOPS', 'ADMIN', 'Administer MLOps workflows, training approvals, and release evidence', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('TRAINING_READ', 'TRAINING_ENGINEER', 'TRAINING_ADMIN', 'MODEL_REGISTRY_READ', 'CHECKPOINT_RESTORE', 'MLOPS_ADMIN');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('TRAINING_READ', 'MODEL_REGISTRY_READ');

INSERT INTO training.gpu_resources (id, resource_name, resource_type, gpu_count, total_vram_gb, allocated_vram_gb, status, last_heartbeat_at)
VALUES
('00000000-0000-0000-0000-000000001451', 'local-training-capacity-placeholder', 'SINGLE_GPU_READY', 1, 24, 0, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000001452', 'future-cluster-capacity-placeholder', 'CLUSTER_READY', 8, 640, 0, 'RESERVED_FOR_FUTURE', NOW());

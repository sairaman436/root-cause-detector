-- Purpose: Adds governed AI dataset engineering registry, sample, quality, annotation, versioning, lineage, and approval tables.
-- Why it exists: RAG, evaluation, fine-tuning preparation, synthetic data review, and agent memory require approved datasets with quality evidence.
-- Architecture fit: Implements AI-1 Dataset Engineering Platform persistence without model training or fine-tuning.

CREATE SCHEMA IF NOT EXISTS datasets;

CREATE TABLE datasets.datasets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dataset_type VARCHAR(80) NOT NULL,
    status VARCHAR(60) NOT NULL,
    owner_id UUID,
    description TEXT,
    tags VARCHAR(1000),
    metadata_json TEXT,
    quality_score NUMERIC(5, 4) NOT NULL,
    synthetic_ratio NUMERIC(5, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE datasets.dataset_versions (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    status VARCHAR(60) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_versions_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id),
    CONSTRAINT uq_dataset_version_number UNIQUE (dataset_id, version_number)
);

CREATE TABLE datasets.dataset_samples (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    version_id UUID,
    sample_type VARCHAR(80) NOT NULL,
    input_text TEXT NOT NULL,
    output_text TEXT,
    language VARCHAR(32) NOT NULL,
    fingerprint VARCHAR(128) NOT NULL,
    synthetic BOOLEAN NOT NULL,
    validation_status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_samples_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id),
    CONSTRAINT fk_dataset_samples_version FOREIGN KEY (version_id) REFERENCES datasets.dataset_versions(id),
    CONSTRAINT uq_dataset_sample_fingerprint UNIQUE (fingerprint)
);

CREATE TABLE datasets.dataset_annotations (
    id UUID PRIMARY KEY,
    sample_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    annotation_type VARCHAR(80) NOT NULL,
    label VARCHAR(160) NOT NULL,
    status VARCHAR(60) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_annotations_sample FOREIGN KEY (sample_id) REFERENCES datasets.dataset_samples(id)
);

CREATE TABLE datasets.dataset_reviewers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reviewer_type VARCHAR(80) NOT NULL,
    expertise VARCHAR(255),
    assigned_count INTEGER NOT NULL,
    completed_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE datasets.dataset_quality (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    quality_score NUMERIC(5, 4) NOT NULL,
    duplicate_rate NUMERIC(5, 4) NOT NULL,
    pii_rate NUMERIC(5, 4) NOT NULL,
    validation_error_rate NUMERIC(5, 4) NOT NULL,
    findings_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_quality_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id)
);

CREATE TABLE datasets.synthetic_datasets (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    generation_method VARCHAR(120) NOT NULL,
    safety_status VARCHAR(80) NOT NULL,
    sample_count INTEGER NOT NULL,
    provenance_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_synthetic_datasets_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id)
);

CREATE TABLE datasets.evaluation_datasets (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    benchmark_name VARCHAR(160) NOT NULL,
    evaluation_purpose VARCHAR(160) NOT NULL,
    sample_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_evaluation_datasets_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id)
);

CREATE TABLE datasets.dataset_lineage (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    source_type VARCHAR(80) NOT NULL,
    source_id UUID NOT NULL,
    transformation VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_lineage_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id)
);

CREATE TABLE datasets.dataset_approvals (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    approver_id UUID NOT NULL,
    approval_status VARCHAR(60) NOT NULL,
    rationale TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dataset_approvals_dataset FOREIGN KEY (dataset_id) REFERENCES datasets.datasets(id)
);

CREATE INDEX idx_datasets_type_status ON datasets.datasets(dataset_type, status);
CREATE INDEX idx_datasets_owner_created ON datasets.datasets(owner_id, created_at);
CREATE INDEX idx_dataset_samples_dataset_status ON datasets.dataset_samples(dataset_id, validation_status);
CREATE INDEX idx_dataset_quality_dataset_created ON datasets.dataset_quality(dataset_id, created_at);
CREATE INDEX idx_dataset_annotations_sample_status ON datasets.dataset_annotations(sample_id, status);
CREATE INDEX idx_dataset_lineage_dataset_source ON datasets.dataset_lineage(dataset_id, source_type);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001201', 'DATASET_READ', 'DATASET', 'READ', 'Read governed AI datasets, versions, and quality reports', NOW(), NOW()),
('00000000-0000-0000-0000-000000001202', 'DATASET_ENGINEER', 'DATASET', 'ENGINEER', 'Create, clean, validate, and export governed AI datasets', NOW(), NOW()),
('00000000-0000-0000-0000-000000001203', 'DATASET_REVIEW', 'DATASET', 'REVIEW', 'Review, annotate, and approve dataset quality evidence', NOW(), NOW()),
('00000000-0000-0000-0000-000000001204', 'DATASET_ANNOTATE', 'DATASET', 'ANNOTATE', 'Annotate dataset samples and resolve label conflicts', NOW(), NOW()),
('00000000-0000-0000-0000-000000001205', 'DATASET_ADMIN', 'DATASET', 'ADMIN', 'Administer dataset lifecycle, reviewers, approvals, and rollback', NOW(), NOW()),
('00000000-0000-0000-0000-000000001206', 'AI_SCIENTIST', 'AI_PLATFORM', 'RESEARCH', 'Operate governed AI research data preparation workflows', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('DATASET_READ', 'DATASET_ENGINEER', 'DATASET_REVIEW', 'DATASET_ANNOTATE', 'DATASET_ADMIN', 'AI_SCIENTIST');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('DATASET_READ', 'DATASET_REVIEW');

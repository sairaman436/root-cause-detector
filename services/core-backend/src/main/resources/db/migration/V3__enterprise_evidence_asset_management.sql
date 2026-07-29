-- Purpose: Creates the Enterprise Evidence and Asset Management schema for Milestone 4.
-- Why it exists: Evidence assets are the durable support records for future AI, RAG, and analytics workflows.
-- Architecture fit: Adds governed evidence storage metadata without introducing OCR, embeddings, Kafka, ML, RAG, or reporting.

CREATE SCHEMA IF NOT EXISTS evidence;

CREATE TABLE evidence.evidence (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES identity.organizations(id),
    survey_id UUID REFERENCES survey.surveys(id),
    question_id UUID REFERENCES survey.survey_questions(id),
    uploaded_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    evidence_type VARCHAR(40) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(180) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256_checksum VARCHAR(64) NOT NULL,
    storage_provider VARCHAR(40) NOT NULL,
    storage_key VARCHAR(600) NOT NULL,
    current_version INTEGER NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_evidence_storage_key UNIQUE (storage_provider, storage_key)
);

CREATE TABLE evidence.evidence_metadata (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL UNIQUE REFERENCES evidence.evidence(id) ON DELETE CASCADE,
    title VARCHAR(220),
    description VARCHAR(1000),
    custom_metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE evidence.evidence_versions (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL REFERENCES evidence.evidence(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(180) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256_checksum VARCHAR(64) NOT NULL,
    storage_provider VARCHAR(40) NOT NULL,
    storage_key VARCHAR(600) NOT NULL,
    metadata_snapshot_json TEXT,
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_evidence_versions_number UNIQUE (evidence_id, version_number)
);

CREATE TABLE evidence.evidence_tags (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL REFERENCES evidence.evidence(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_evidence_tags_name UNIQUE (evidence_id, name)
);

CREATE TABLE evidence.evidence_audit (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL REFERENCES evidence.evidence(id) ON DELETE CASCADE,
    actor_user_id UUID NOT NULL REFERENCES identity.users(id),
    action VARCHAR(60) NOT NULL,
    outcome VARCHAR(40) NOT NULL,
    details VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_evidence_organization ON evidence.evidence (organization_id);
CREATE INDEX idx_evidence_survey ON evidence.evidence (survey_id);
CREATE INDEX idx_evidence_question ON evidence.evidence (question_id);
CREATE INDEX idx_evidence_uploaded_by ON evidence.evidence (uploaded_by_user_id);
CREATE INDEX idx_evidence_created_at ON evidence.evidence (created_at);
CREATE INDEX idx_evidence_type ON evidence.evidence (evidence_type);
CREATE INDEX idx_evidence_checksum ON evidence.evidence (sha256_checksum);
CREATE INDEX idx_evidence_tags_name ON evidence.evidence_tags (name);
CREATE INDEX idx_evidence_versions_evidence ON evidence.evidence_versions (evidence_id, version_number);
CREATE INDEX idx_evidence_audit_evidence ON evidence.evidence_audit (evidence_id, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000401', 'EVIDENCE_READ', 'EVIDENCE', 'READ', 'Search and view evidence metadata', NOW(), NOW()),
('00000000-0000-0000-0000-000000000402', 'EVIDENCE_MANAGE', 'EVIDENCE', 'MANAGE', 'Upload, update, delete, and restore evidence', NOW(), NOW()),
('00000000-0000-0000-0000-000000000403', 'EVIDENCE_DOWNLOAD', 'EVIDENCE', 'DOWNLOAD', 'Download evidence binaries and request future signed URLs', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('EVIDENCE_READ', 'EVIDENCE_MANAGE', 'EVIDENCE_DOWNLOAD');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000401'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000402'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000403'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000401'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000403');

-- Purpose: Adds source registry, connector, metadata, trust, fingerprint, coverage, and version tables for enterprise knowledge acquisition.
-- Why it exists: RAG, fine-tuning preparation, evaluations, synthetic data, policy retrieval, and agent memory need continuously governed corpora.
-- Architecture fit: Implements AI-2 Knowledge Acquisition Platform persistence without training, LoRA, QLoRA, embeddings, or RAG execution.

CREATE SCHEMA IF NOT EXISTS knowledge;

CREATE TABLE knowledge.knowledge_sources (
    id UUID PRIMARY KEY,
    source_key VARCHAR(160) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    source_type VARCHAR(120) NOT NULL,
    base_url VARCHAR(512),
    trust_tier VARCHAR(80) NOT NULL,
    status VARCHAR(60) NOT NULL,
    owner_team VARCHAR(120) NOT NULL,
    schedule_cron VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE knowledge.knowledge_datasets (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    dataset_type VARCHAR(120) NOT NULL,
    status VARCHAR(60) NOT NULL,
    owner_team VARCHAR(120) NOT NULL,
    retention_policy VARCHAR(120) NOT NULL,
    version_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_datasets_source FOREIGN KEY (source_id) REFERENCES knowledge.knowledge_sources(id)
);

CREATE TABLE knowledge.knowledge_acquisition_jobs (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    job_type VARCHAR(80) NOT NULL,
    status VARCHAR(60) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    documents_discovered INTEGER NOT NULL,
    documents_accepted INTEGER NOT NULL,
    quality_score NUMERIC(5, 4) NOT NULL,
    error_message TEXT,
    CONSTRAINT fk_knowledge_jobs_source FOREIGN KEY (source_id) REFERENCES knowledge.knowledge_sources(id)
);

CREATE TABLE knowledge.knowledge_crawlers (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    connector_type VARCHAR(120) NOT NULL,
    schedule_cron VARCHAR(120),
    incremental_cursor VARCHAR(512),
    status VARCHAR(60) NOT NULL,
    last_run_at TIMESTAMP,
    CONSTRAINT fk_knowledge_crawlers_source FOREIGN KEY (source_id) REFERENCES knowledge.knowledge_sources(id)
);

CREATE TABLE knowledge.knowledge_metadata (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    document_title VARCHAR(500) NOT NULL,
    document_type VARCHAR(80) NOT NULL,
    language VARCHAR(32) NOT NULL,
    department VARCHAR(160),
    scheme VARCHAR(160),
    administrative_region VARCHAR(160),
    metadata_json TEXT NOT NULL,
    extracted_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_metadata_dataset FOREIGN KEY (dataset_id) REFERENCES knowledge.knowledge_datasets(id)
);

CREATE TABLE knowledge.knowledge_entities (
    id UUID PRIMARY KEY,
    metadata_id UUID NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_value VARCHAR(255) NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL,
    CONSTRAINT fk_knowledge_entities_metadata FOREIGN KEY (metadata_id) REFERENCES knowledge.knowledge_metadata(id)
);

CREATE TABLE knowledge.knowledge_classifications (
    id UUID PRIMARY KEY,
    metadata_id UUID NOT NULL,
    label VARCHAR(120) NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL,
    classifier_version VARCHAR(80) NOT NULL,
    CONSTRAINT fk_knowledge_classifications_metadata FOREIGN KEY (metadata_id) REFERENCES knowledge.knowledge_metadata(id)
);

CREATE TABLE knowledge.knowledge_trust (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    trust_score NUMERIC(5, 4) NOT NULL,
    freshness_score NUMERIC(5, 4) NOT NULL,
    coverage_score NUMERIC(5, 4) NOT NULL,
    quality_score NUMERIC(5, 4) NOT NULL,
    rationale VARCHAR(500) NOT NULL,
    evaluated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_trust_source FOREIGN KEY (source_id) REFERENCES knowledge.knowledge_sources(id)
);

CREATE TABLE knowledge.knowledge_versions (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    storage_uri VARCHAR(512) NOT NULL,
    status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_versions_dataset FOREIGN KEY (dataset_id) REFERENCES knowledge.knowledge_datasets(id),
    CONSTRAINT uq_knowledge_version_number UNIQUE (dataset_id, version_number)
);

CREATE TABLE knowledge.knowledge_fingerprints (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    fingerprint VARCHAR(128) NOT NULL,
    algorithm VARCHAR(40) NOT NULL,
    duplicate BOOLEAN NOT NULL,
    first_seen_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_fingerprints_source FOREIGN KEY (source_id) REFERENCES knowledge.knowledge_sources(id),
    CONSTRAINT uq_knowledge_fingerprint UNIQUE (fingerprint)
);

CREATE TABLE knowledge.knowledge_coverage (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    coverage_area VARCHAR(160) NOT NULL,
    coverage_score NUMERIC(5, 4) NOT NULL,
    missing_topics TEXT NOT NULL,
    evaluated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_coverage_dataset FOREIGN KEY (dataset_id) REFERENCES knowledge.knowledge_datasets(id)
);

CREATE INDEX idx_knowledge_sources_type_status ON knowledge.knowledge_sources(source_type, status);
CREATE INDEX idx_knowledge_datasets_source_status ON knowledge.knowledge_datasets(source_id, status);
CREATE INDEX idx_knowledge_jobs_source_status ON knowledge.knowledge_acquisition_jobs(source_id, status);
CREATE INDEX idx_knowledge_metadata_dataset_type ON knowledge.knowledge_metadata(dataset_id, document_type);
CREATE INDEX idx_knowledge_entities_type_value ON knowledge.knowledge_entities(entity_type, entity_value);
CREATE INDEX idx_knowledge_classifications_label ON knowledge.knowledge_classifications(label);
CREATE INDEX idx_knowledge_coverage_dataset_area ON knowledge.knowledge_coverage(dataset_id, coverage_area);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001301', 'KNOWLEDGE_READ', 'KNOWLEDGE', 'READ', 'Read knowledge sources, datasets, jobs, and coverage reports', NOW(), NOW()),
('00000000-0000-0000-0000-000000001302', 'KNOWLEDGE_ENGINEER', 'KNOWLEDGE', 'ENGINEER', 'Acquire, crawl, normalize, classify, and reindex trusted knowledge corpora', NOW(), NOW()),
('00000000-0000-0000-0000-000000001303', 'KNOWLEDGE_ADMIN', 'KNOWLEDGE', 'ADMIN', 'Administer source trust, connector governance, and knowledge lifecycle controls', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('KNOWLEDGE_READ', 'KNOWLEDGE_ENGINEER', 'KNOWLEDGE_ADMIN');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name = 'KNOWLEDGE_READ';

INSERT INTO knowledge.knowledge_sources (id, source_key, name, source_type, base_url, trust_tier, status, owner_team, schedule_cron, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001351', 'gov-schemes', 'Central Government Schemes', 'GOVERNMENT_SCHEMES', 'https://www.india.gov.in/', 'PRIMARY_GOVERNMENT', 'ACTIVE', 'knowledge-engineering', '0 0 2 * * *', NOW(), NOW()),
('00000000-0000-0000-0000-000000001352', 'census-india', 'Census of India', 'CENSUS', 'https://censusindia.gov.in/', 'PRIMARY_GOVERNMENT', 'ACTIVE', 'knowledge-engineering', '0 0 3 * * 0', NOW(), NOW()),
('00000000-0000-0000-0000-000000001353', 'who-publications', 'WHO Publications', 'WHO', 'https://www.who.int/publications', 'MULTILATERAL', 'ACTIVE', 'knowledge-engineering', '0 0 4 * * 0', NOW(), NOW()),
('00000000-0000-0000-0000-000000001354', 'fao-publications', 'FAO Publications', 'FAO', 'https://www.fao.org/publications', 'MULTILATERAL', 'ACTIVE', 'knowledge-engineering', '0 0 5 * * 0', NOW(), NOW());

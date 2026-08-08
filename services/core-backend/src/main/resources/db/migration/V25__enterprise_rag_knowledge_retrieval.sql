-- Purpose: Creates governance and lineage tables for the enterprise RAG knowledge retrieval layer.
-- Why it exists: Trusted document ingestion, chunking, embedding, retrieval, citation validation, and audits require durable metadata beyond the vector store.
-- Architecture fit: Extends the AI and knowledge schemas without embedding retrieval execution logic inside PostgreSQL.

CREATE SCHEMA IF NOT EXISTS knowledge;

CREATE TABLE IF NOT EXISTS knowledge.knowledge_collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_name VARCHAR(160) NOT NULL,
    collection_version INTEGER NOT NULL DEFAULT 1,
    embedding_model VARCHAR(160) NOT NULL,
    embedding_version VARCHAR(80) NOT NULL,
    embedding_dimension INTEGER NOT NULL,
    vector_store VARCHAR(80) NOT NULL DEFAULT 'QDRANT',
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_knowledge_collection_version UNIQUE (collection_name, collection_version),
    CONSTRAINT ck_knowledge_collection_dimension CHECK (embedding_dimension > 0)
);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_document_id VARCHAR(120) NOT NULL,
    collection_id UUID REFERENCES knowledge.knowledge_collections(id),
    title VARCHAR(500) NOT NULL,
    source VARCHAR(240) NOT NULL,
    source_url TEXT,
    publisher VARCHAR(240) NOT NULL,
    publication_date DATE,
    document_version VARCHAR(80) NOT NULL DEFAULT '1.0.0',
    language VARCHAR(32) NOT NULL DEFAULT 'en',
    domain VARCHAR(120) NOT NULL DEFAULT 'general',
    document_type VARCHAR(80) NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    trust_tier VARCHAR(80) NOT NULL DEFAULT 'PROJECT_APPROVED',
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    ingested_by UUID,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    retired_at TIMESTAMPTZ,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT uk_knowledge_document_external_version UNIQUE (external_document_id, document_version),
    CONSTRAINT uk_knowledge_document_checksum UNIQUE (checksum_sha256),
    CONSTRAINT ck_knowledge_document_status CHECK (status IN ('ACTIVE', 'REPLACED', 'RETIRED', 'FAILED'))
);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_document_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES knowledge.knowledge_documents(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    version_label VARCHAR(80) NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    change_reason TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_knowledge_document_version_number UNIQUE (document_id, version_number)
);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_chunk_id VARCHAR(120) NOT NULL UNIQUE,
    document_id UUID NOT NULL REFERENCES knowledge.knowledge_documents(id) ON DELETE CASCADE,
    collection_id UUID REFERENCES knowledge.knowledge_collections(id),
    ordinal INTEGER NOT NULL,
    page_number INTEGER,
    section_title VARCHAR(500) NOT NULL,
    text_checksum_sha256 CHAR(64) NOT NULL,
    token_estimate INTEGER NOT NULL DEFAULT 0,
    language VARCHAR(32) NOT NULL DEFAULT 'en',
    domain VARCHAR(120) NOT NULL DEFAULT 'general',
    security_flags JSONB NOT NULL DEFAULT '[]'::jsonb,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_knowledge_chunk_document_ordinal UNIQUE (document_id, ordinal)
);

CREATE TABLE IF NOT EXISTS knowledge.embedding_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chunk_id UUID NOT NULL REFERENCES knowledge.knowledge_chunks(id) ON DELETE CASCADE,
    embedding_model VARCHAR(160) NOT NULL,
    embedding_version VARCHAR(80) NOT NULL,
    embedding_dimension INTEGER NOT NULL,
    vector_store VARCHAR(80) NOT NULL DEFAULT 'QDRANT',
    vector_point_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_embedding_record_point UNIQUE (vector_store, vector_point_id),
    CONSTRAINT ck_embedding_record_dimension CHECK (embedding_dimension > 0)
);

CREATE TABLE IF NOT EXISTS knowledge.retrieval_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_hash CHAR(64) NOT NULL,
    user_id UUID,
    organization_id UUID,
    collection_name VARCHAR(160) NOT NULL,
    filters_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    retrieval_mode VARCHAR(120) NOT NULL,
    top_k INTEGER NOT NULL,
    support_status VARCHAR(80),
    latency_ms INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS knowledge.retrieval_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    retrieval_query_id UUID NOT NULL REFERENCES knowledge.retrieval_queries(id) ON DELETE CASCADE,
    chunk_id UUID REFERENCES knowledge.knowledge_chunks(id),
    rank INTEGER NOT NULL,
    vector_score NUMERIC(8,5),
    keyword_score NUMERIC(8,5),
    rerank_score NUMERIC(8,5),
    final_score NUMERIC(8,5) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_retrieval_result_rank UNIQUE (retrieval_query_id, rank)
);

CREATE TABLE IF NOT EXISTS knowledge.citation_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    retrieval_query_id UUID REFERENCES knowledge.retrieval_queries(id) ON DELETE SET NULL,
    document_id UUID REFERENCES knowledge.knowledge_documents(id),
    chunk_id UUID REFERENCES knowledge.knowledge_chunks(id),
    citation_label VARCHAR(80) NOT NULL,
    validation_status VARCHAR(80) NOT NULL,
    validation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_citation_validation_status CHECK (validation_status IN ('VALIDATED', 'FAILED', 'NOT_APPLICABLE'))
);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(120) NOT NULL,
    subject_type VARCHAR(80) NOT NULL,
    subject_id VARCHAR(120) NOT NULL,
    actor_user_id UUID,
    organization_id UUID,
    details_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_knowledge_documents_domain ON knowledge.knowledge_documents(domain);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_source ON knowledge.knowledge_documents(source);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_status ON knowledge.knowledge_documents(status);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_publisher ON knowledge.knowledge_documents(publisher);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_metadata ON knowledge.knowledge_documents USING GIN (metadata_json);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_document ON knowledge.knowledge_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_domain_language ON knowledge.knowledge_chunks(domain, language);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_metadata ON knowledge.knowledge_chunks USING GIN (metadata_json);
CREATE INDEX IF NOT EXISTS idx_embedding_records_chunk ON knowledge.embedding_records(chunk_id);
CREATE INDEX IF NOT EXISTS idx_retrieval_queries_created_at ON knowledge.retrieval_queries(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_retrieval_queries_hash ON knowledge.retrieval_queries(query_hash);
CREATE INDEX IF NOT EXISTS idx_retrieval_results_query ON knowledge.retrieval_results(retrieval_query_id);
CREATE INDEX IF NOT EXISTS idx_citation_records_document ON knowledge.citation_records(document_id);
CREATE INDEX IF NOT EXISTS idx_citation_records_status ON knowledge.citation_records(validation_status);
CREATE INDEX IF NOT EXISTS idx_knowledge_audit_subject ON knowledge.knowledge_audit(subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_audit_created_at ON knowledge.knowledge_audit(created_at DESC);

-- Purpose: Creates the Enterprise AI Foundation schema for Milestone 8.
-- Why it exists: The platform needs governed model registry, prompt management, embedding, vector metadata, RAG, usage, and inference telemetry foundations.
-- Architecture fit: Adds AI infrastructure without implementing root cause reasoning, recommendations, autonomous agents, or predictive models.

CREATE SCHEMA IF NOT EXISTS ai;

CREATE TABLE ai.ai_models (
    id UUID PRIMARY KEY,
    model_id VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    family VARCHAR(80) NOT NULL,
    provider VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.model_versions (
    id UUID PRIMARY KEY,
    model_id UUID NOT NULL REFERENCES ai.ai_models(id) ON DELETE CASCADE,
    version_name VARCHAR(80) NOT NULL,
    parameter_count VARCHAR(80) NOT NULL,
    quantization VARCHAR(80),
    license_name VARCHAR(180),
    capabilities TEXT NOT NULL,
    supported_languages TEXT NOT NULL,
    memory_requirement VARCHAR(80),
    gpu_requirement VARCHAR(80),
    context_length INTEGER NOT NULL,
    embedding_support BOOLEAN NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_model_version UNIQUE (model_id, version_name)
);

CREATE TABLE ai.prompt_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.prompt_templates (
    id UUID PRIMARY KEY,
    category_id UUID REFERENCES ai.prompt_categories(id),
    name VARCHAR(160) NOT NULL UNIQUE,
    status VARCHAR(40) NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.prompt_versions (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES ai.prompt_templates(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    template_text TEXT NOT NULL,
    variables_json TEXT NOT NULL,
    approval_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_prompt_version UNIQUE (template_id, version_number)
);

CREATE TABLE ai.embedding_jobs (
    id UUID PRIMARY KEY,
    source_type VARCHAR(80) NOT NULL,
    source_id UUID NOT NULL,
    embedding_model VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    chunk_count INTEGER NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE ai.embedding_records (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES ai.embedding_jobs(id) ON DELETE CASCADE,
    collection_name VARCHAR(120) NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    vector_json TEXT NOT NULL,
    metadata_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.vector_collections (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    vector_size INTEGER NOT NULL,
    distance_metric VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.inference_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    model_id VARCHAR(120) NOT NULL,
    request_type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    prompt_tokens INTEGER NOT NULL,
    completion_tokens INTEGER NOT NULL,
    latency_ms BIGINT NOT NULL,
    safety_blocked BOOLEAN NOT NULL,
    prompt_hash TEXT,
    response_preview TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.token_usage (
    id UUID PRIMARY KEY,
    user_id UUID,
    model_id VARCHAR(120) NOT NULL,
    prompt_tokens INTEGER NOT NULL,
    completion_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    estimated_cost DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.context_sessions (
    id UUID PRIMARY KEY,
    user_id UUID,
    village_id UUID,
    survey_id UUID,
    session_type VARCHAR(80) NOT NULL,
    context_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.rag_requests (
    id UUID PRIMARY KEY,
    user_id UUID,
    query_text TEXT NOT NULL,
    collection_name VARCHAR(120) NOT NULL,
    model_id VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    response_text TEXT NOT NULL,
    retrieval_latency_ms BIGINT NOT NULL,
    inference_latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai.rag_citations (
    id UUID PRIMARY KEY,
    rag_request_id UUID NOT NULL REFERENCES ai.rag_requests(id) ON DELETE CASCADE,
    source_type VARCHAR(120) NOT NULL,
    source_id VARCHAR(180) NOT NULL,
    excerpt TEXT NOT NULL,
    score DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_model_family_status ON ai.ai_models (family, status);
CREATE INDEX idx_prompt_status ON ai.prompt_templates (status, created_at);
CREATE INDEX idx_embedding_collection ON ai.embedding_records (collection_name, chunk_index);
CREATE INDEX idx_inference_model_time ON ai.inference_logs (model_id, created_at);
CREATE INDEX idx_usage_model_time ON ai.token_usage (model_id, created_at);
CREATE INDEX idx_context_user_time ON ai.context_sessions (user_id, updated_at);
CREATE INDEX idx_rag_collection_time ON ai.rag_requests (collection_name, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000801', 'AI_ADMIN', 'AI', 'ADMIN', 'Administer AI models, prompts, safety, and gateway controls', NOW(), NOW()),
('00000000-0000-0000-0000-000000000802', 'AI_OPERATOR', 'AI', 'OPERATE', 'Run AI gateway, embedding, and RAG operations', NOW(), NOW()),
('00000000-0000-0000-0000-000000000803', 'PROMPT_ENGINEER', 'AI_PROMPT', 'MANAGE', 'Create and manage prompt templates and versions', NOW(), NOW()),
('00000000-0000-0000-0000-000000000804', 'AI_AUDITOR', 'AI_AUDIT', 'READ', 'View AI usage, inference, and governance telemetry', NOW(), NOW()),
('00000000-0000-0000-0000-000000000805', 'AI_READ', 'AI', 'READ', 'Read AI model and prompt catalogs', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('AI_ADMIN', 'AI_OPERATOR', 'PROMPT_ENGINEER', 'AI_AUDITOR', 'AI_READ');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000802'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000805'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000804');

INSERT INTO ai.ai_models (id, model_id, name, family, provider, status, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000811', 'qwen2.5-local', 'Qwen 2.5 Local', 'Qwen', 'Ollama', 'APPROVED', NOW(), NOW()),
('00000000-0000-0000-0000-000000000812', 'llama3.1-local', 'Llama 3.1 Local', 'Llama', 'Ollama', 'AVAILABLE', NOW(), NOW()),
('00000000-0000-0000-0000-000000000813', 'gemma2-local', 'Gemma 2 Local', 'Gemma', 'Ollama', 'AVAILABLE', NOW(), NOW()),
('00000000-0000-0000-0000-000000000814', 'mistral-local', 'Mistral Local', 'Mistral', 'Ollama', 'AVAILABLE', NOW(), NOW()),
('00000000-0000-0000-0000-000000000815', 'deepseek-local', 'DeepSeek Local', 'DeepSeek', 'vLLM', 'AVAILABLE', NOW(), NOW()),
('00000000-0000-0000-0000-000000000816', 'phi3-local', 'Phi 3 Local', 'Phi', 'llama.cpp', 'AVAILABLE', NOW(), NOW());

INSERT INTO ai.model_versions (id, model_id, version_name, parameter_count, quantization, license_name, capabilities, supported_languages, memory_requirement, gpu_requirement, context_length, embedding_support, status, created_at)
VALUES
('00000000-0000-0000-0000-000000000821', '00000000-0000-0000-0000-000000000811', '2.5', '7B', 'Q4_K_M', 'Model-specific license', '["chat","rag","summarization"]', '["en","hi","te"]', '8GB RAM', 'Optional GPU', 32768, false, 'APPROVED', NOW()),
('00000000-0000-0000-0000-000000000822', '00000000-0000-0000-0000-000000000812', '3.1', '8B', 'Q4_K_M', 'Model-specific license', '["chat","rag"]', '["en"]', '8GB RAM', 'Optional GPU', 8192, false, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000823', '00000000-0000-0000-0000-000000000813', '2', '9B', 'Q4_K_M', 'Model-specific license', '["chat"]', '["en"]', '10GB RAM', 'Optional GPU', 8192, false, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000824', '00000000-0000-0000-0000-000000000814', 'latest', '7B', 'Q4_K_M', 'Apache-2.0 compatible where applicable', '["chat","rag"]', '["en"]', '8GB RAM', 'Optional GPU', 32768, false, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000825', '00000000-0000-0000-0000-000000000815', 'latest', '7B', 'FP16', 'Model-specific license', '["chat","reasoning"]', '["en"]', '16GB RAM', 'GPU recommended', 32768, false, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000826', '00000000-0000-0000-0000-000000000816', '3', '3.8B', 'Q4_K_M', 'MIT where applicable', '["chat"]', '["en"]', '4GB RAM', 'Optional GPU', 4096, false, 'AVAILABLE', NOW());

INSERT INTO ai.prompt_categories (id, name, description, created_at)
VALUES
('00000000-0000-0000-0000-000000000831', 'General', 'General AI prompts', NOW()),
('00000000-0000-0000-0000-000000000832', 'RAG', 'Retrieval augmented generation prompts', NOW()),
('00000000-0000-0000-0000-000000000833', 'Safety', 'AI safety and validation prompts', NOW());

INSERT INTO ai.vector_collections (id, name, vector_size, distance_metric, status, description, created_at)
VALUES
('00000000-0000-0000-0000-000000000841', 'knowledge', 16, 'COSINE', 'ACTIVE', 'Knowledge base chunks', NOW()),
('00000000-0000-0000-0000-000000000842', 'surveys', 16, 'COSINE', 'ACTIVE', 'Survey text and metadata chunks', NOW()),
('00000000-0000-0000-0000-000000000843', 'evidence', 16, 'COSINE', 'ACTIVE', 'Evidence metadata and extracted text chunks', NOW()),
('00000000-0000-0000-0000-000000000844', 'policies', 16, 'COSINE', 'ACTIVE', 'Policy documents', NOW()),
('00000000-0000-0000-0000-000000000845', 'research_papers', 16, 'COSINE', 'ACTIVE', 'Research paper chunks', NOW()),
('00000000-0000-0000-0000-000000000846', 'village_reports', 16, 'COSINE', 'ACTIVE', 'Village report chunks', NOW()),
('00000000-0000-0000-0000-000000000847', 'conversation_memory', 16, 'COSINE', 'FUTURE', 'Future conversation memory collection', NOW());

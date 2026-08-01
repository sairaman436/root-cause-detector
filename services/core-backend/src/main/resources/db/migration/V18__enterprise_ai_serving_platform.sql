-- Purpose: Creates the AI-8 enterprise model serving schema.
-- Why it exists: All AI inference must pass through a governed gateway with sessions, requests, responses, routing, serving nodes, deployments, metrics, and audits.
-- Architecture fit: Adds serving infrastructure without retraining, evaluation, or dataset collection.

CREATE SCHEMA IF NOT EXISTS serving;

CREATE TABLE serving.inference_sessions (
    id UUID PRIMARY KEY,
    user_id UUID,
    session_type VARCHAR(120) NOT NULL,
    conversation_context TEXT NOT NULL,
    user_context TEXT NOT NULL,
    village_context TEXT NOT NULL,
    survey_context TEXT NOT NULL,
    knowledge_context TEXT NOT NULL,
    memory_limit_tokens INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE serving.inference_requests (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    user_id UUID,
    task_type VARCHAR(120) NOT NULL,
    assistant_type VARCHAR(120) NOT NULL,
    language VARCHAR(40) NOT NULL,
    user_role VARCHAR(120) NOT NULL,
    prompt_text TEXT NOT NULL,
    context_json TEXT NOT NULL,
    streaming_requested BOOLEAN NOT NULL,
    batch_requested BOOLEAN NOT NULL,
    async_requested BOOLEAN NOT NULL,
    policy_status VARCHAR(80) NOT NULL,
    prompt_security_status VARCHAR(80) NOT NULL,
    status VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_inference_requests_session FOREIGN KEY (session_id) REFERENCES serving.inference_sessions(id)
);

CREATE TABLE serving.inference_responses (
    id UUID PRIMARY KEY,
    inference_request_id UUID NOT NULL,
    selected_model VARCHAR(160) NOT NULL,
    provider VARCHAR(120) NOT NULL,
    output_text TEXT NOT NULL,
    output_validation_status VARCHAR(80) NOT NULL,
    citation_validation_status VARCHAR(80) NOT NULL,
    fallback_used BOOLEAN NOT NULL,
    cache_hit BOOLEAN NOT NULL,
    prompt_tokens INTEGER NOT NULL,
    completion_tokens INTEGER NOT NULL,
    latency_ms BIGINT NOT NULL,
    status VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_inference_responses_request FOREIGN KEY (inference_request_id) REFERENCES serving.inference_requests(id)
);

CREATE TABLE serving.serving_nodes (
    id UUID PRIMARY KEY,
    node_name VARCHAR(160) NOT NULL,
    provider_type VARCHAR(120) NOT NULL,
    endpoint_url VARCHAR(500) NOT NULL,
    hardware_class VARCHAR(120) NOT NULL,
    queue_depth INTEGER NOT NULL,
    max_concurrency INTEGER NOT NULL,
    gpu_count INTEGER NOT NULL,
    vram_gb INTEGER NOT NULL,
    cpu_cores INTEGER NOT NULL,
    memory_gb INTEGER NOT NULL,
    circuit_status VARCHAR(80) NOT NULL,
    health_status VARCHAR(80) NOT NULL,
    last_heartbeat_at TIMESTAMP NOT NULL
);

CREATE TABLE serving.model_deployments (
    id UUID PRIMARY KEY,
    optimization_run_id UUID,
    model_key VARCHAR(160) NOT NULL,
    model_version VARCHAR(120) NOT NULL,
    assistant_type VARCHAR(120) NOT NULL,
    provider_type VARCHAR(120) NOT NULL,
    deployment_target VARCHAR(120) NOT NULL,
    quality_gate_status VARCHAR(80) NOT NULL,
    traffic_status VARCHAR(80) NOT NULL,
    warmup_complete BOOLEAN NOT NULL,
    rollback_version VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_model_deployments_optimization FOREIGN KEY (optimization_run_id) REFERENCES optimization.optimization_runs(id)
);

CREATE TABLE serving.routing_decisions (
    id UUID PRIMARY KEY,
    inference_request_id UUID NOT NULL,
    model_deployment_id UUID NOT NULL,
    serving_node_id UUID NOT NULL,
    selected_model VARCHAR(160) NOT NULL,
    routing_policy VARCHAR(500) NOT NULL,
    fallback_model VARCHAR(160) NOT NULL,
    fallback_enabled BOOLEAN NOT NULL,
    decision_factors_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_routing_decisions_request FOREIGN KEY (inference_request_id) REFERENCES serving.inference_requests(id),
    CONSTRAINT fk_routing_decisions_deployment FOREIGN KEY (model_deployment_id) REFERENCES serving.model_deployments(id),
    CONSTRAINT fk_routing_decisions_node FOREIGN KEY (serving_node_id) REFERENCES serving.serving_nodes(id)
);

CREATE TABLE serving.inference_metrics (
    id UUID PRIMARY KEY,
    metric_window VARCHAR(80) NOT NULL,
    requests_per_second NUMERIC(12, 4) NOT NULL,
    p95_latency_ms NUMERIC(12, 2) NOT NULL,
    tokens_per_second NUMERIC(12, 2) NOT NULL,
    gpu_utilization_percent NUMERIC(5, 2) NOT NULL,
    vram_gb NUMERIC(8, 2) NOT NULL,
    cpu_utilization_percent NUMERIC(5, 2) NOT NULL,
    memory_gb NUMERIC(8, 2) NOT NULL,
    queue_depth INTEGER NOT NULL,
    error_rate NUMERIC(5, 4) NOT NULL,
    timeout_rate NUMERIC(5, 4) NOT NULL,
    measured_at TIMESTAMP NOT NULL
);

CREATE TABLE serving.serving_audits (
    id UUID PRIMARY KEY,
    inference_request_id UUID,
    user_id UUID,
    event_type VARCHAR(120) NOT NULL,
    tenant_id VARCHAR(120) NOT NULL,
    request_signature VARCHAR(256) NOT NULL,
    event_json TEXT NOT NULL,
    immutable_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_serving_audits_request FOREIGN KEY (inference_request_id) REFERENCES serving.inference_requests(id)
);

CREATE INDEX idx_inference_sessions_user_status ON serving.inference_sessions(user_id, status, expires_at);
CREATE INDEX idx_inference_requests_session_status ON serving.inference_requests(session_id, status, created_at);
CREATE INDEX idx_inference_responses_request_status ON serving.inference_responses(inference_request_id, status, selected_model);
CREATE INDEX idx_serving_nodes_provider_health ON serving.serving_nodes(provider_type, health_status, circuit_status);
CREATE INDEX idx_model_deployments_assistant_traffic ON serving.model_deployments(assistant_type, traffic_status, created_at);
CREATE INDEX idx_routing_decisions_request_model ON serving.routing_decisions(inference_request_id, selected_model);
CREATE INDEX idx_inference_metrics_window ON serving.inference_metrics(metric_window, measured_at);
CREATE INDEX idx_serving_audits_request_event ON serving.serving_audits(inference_request_id, event_type, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001901', 'SERVING_READ', 'SERVING', 'READ', 'Read serving models, health, metrics, and sessions', NOW(), NOW()),
('00000000-0000-0000-0000-000000001902', 'SERVING_INFER', 'SERVING', 'INFER', 'Run model inference through the enterprise gateway', NOW(), NOW()),
('00000000-0000-0000-0000-000000001903', 'SERVING_ADMIN', 'SERVING', 'ADMIN', 'Manage serving gateway operations and routing controls', NOW(), NOW()),
('00000000-0000-0000-0000-000000001904', 'SERVING_MONITOR', 'SERVING', 'MONITOR', 'Monitor serving performance, nodes, queues, and errors', NOW(), NOW()),
('00000000-0000-0000-0000-000000001905', 'SERVING_SECURITY_REVIEW', 'SERVING_SECURITY', 'REVIEW', 'Review prompt security, request signing, tenant isolation, and audits', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('SERVING_READ', 'SERVING_INFER', 'SERVING_ADMIN', 'SERVING_MONITOR', 'SERVING_SECURITY_REVIEW');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000204', id
FROM identity.permissions
WHERE name IN ('SERVING_READ', 'SERVING_INFER', 'SERVING_MONITOR');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name IN ('SERVING_READ', 'SERVING_MONITOR', 'SERVING_SECURITY_REVIEW');

INSERT INTO serving.model_deployments (id, optimization_run_id, model_key, model_version, assistant_type, provider_type, deployment_target, quality_gate_status, traffic_status, warmup_complete, rollback_version, created_at)
VALUES
('00000000-0000-0000-0000-000000001951', NULL, 'rural-general-assistant', 'serving-v1', 'GENERAL_ASSISTANT', 'OPENAI_COMPATIBLE', 'HYBRID_INFERENCE', 'PASSED', 'ACTIVE', TRUE, 'serving-v0', NOW()),
('00000000-0000-0000-0000-000000001952', NULL, 'rural-policy-assistant', 'serving-v1', 'POLICY_ASSISTANT', 'OLLAMA', 'LOCAL_GPU', 'PASSED', 'ACTIVE', TRUE, 'serving-v0', NOW()),
('00000000-0000-0000-0000-000000001953', NULL, 'rural-root-cause-assistant', 'serving-v1', 'ROOT_CAUSE_ASSISTANT', 'VLLM', 'CLOUD_GPU', 'PASSED', 'ACTIVE', TRUE, 'serving-v0', NOW());

INSERT INTO serving.serving_nodes (id, node_name, provider_type, endpoint_url, hardware_class, queue_depth, max_concurrency, gpu_count, vram_gb, cpu_cores, memory_gb, circuit_status, health_status, last_heartbeat_at)
VALUES
('00000000-0000-0000-0000-000000001961', 'ollama-primary', 'OLLAMA', 'provider://ollama', 'LOCAL_GPU', 0, 16, 1, 12, 8, 32, 'CLOSED', 'HEALTHY', NOW()),
('00000000-0000-0000-0000-000000001962', 'vllm-primary', 'VLLM', 'provider://vllm', 'CLOUD_GPU', 0, 32, 1, 24, 16, 64, 'CLOSED', 'HEALTHY', NOW()),
('00000000-0000-0000-0000-000000001963', 'llama-cpp-edge', 'LLAMA_CPP', 'provider://llama-cpp', 'EDGE_DEVICE', 0, 8, 0, 0, 8, 16, 'CLOSED', 'HEALTHY', NOW());

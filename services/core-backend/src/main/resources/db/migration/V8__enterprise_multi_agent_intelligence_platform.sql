-- Purpose: Creates the Enterprise Multi-Agent Intelligence schema for Milestone 9.
-- Why it exists: The platform needs governed agent registry, orchestration, memory, MCP-style tools, reasoning traces, feedback, and audit records.
-- Architecture fit: Adds assistive multi-agent infrastructure without autonomous government decisions, external integrations, or self-learning agents.

CREATE SCHEMA IF NOT EXISTS agents;

CREATE TABLE agents.agents (
    id UUID PRIMARY KEY,
    agent_key VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    agent_type VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    capabilities_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.agent_conversations (
    id UUID PRIMARY KEY,
    user_id UUID,
    title VARCHAR(180) NOT NULL,
    status VARCHAR(40) NOT NULL,
    context_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.task_plans (
    id UUID PRIMARY KEY,
    conversation_id UUID,
    objective TEXT NOT NULL,
    plan_json TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.task_executions (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    retry_count INTEGER NOT NULL,
    failure_reason VARCHAR(1000),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE agents.agent_executions (
    id UUID PRIMARY KEY,
    conversation_id UUID,
    plan_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    input_json TEXT NOT NULL,
    output_json TEXT NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    latency_ms BIGINT NOT NULL,
    requires_approval BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE agents.agent_tasks (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    parent_task_id UUID,
    agent_key VARCHAR(120) NOT NULL,
    task_type VARCHAR(80) NOT NULL,
    instructions TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    priority INTEGER NOT NULL,
    result_json TEXT NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE agents.agent_memory (
    id UUID PRIMARY KEY,
    memory_type VARCHAR(80) NOT NULL,
    scope_type VARCHAR(80) NOT NULL,
    scope_id UUID,
    conversation_id UUID,
    task_id UUID,
    content_json TEXT NOT NULL,
    references_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE agents.tool_definitions (
    id UUID PRIMARY KEY,
    tool_key VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    permissions_json TEXT NOT NULL,
    metadata_json TEXT NOT NULL,
    health_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.tool_invocations (
    id UUID PRIMARY KEY,
    tool_key VARCHAR(120) NOT NULL,
    task_id UUID,
    execution_id UUID,
    input_json TEXT NOT NULL,
    output_json TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    latency_ms BIGINT NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.reasoning_traces (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    task_id UUID,
    step_number INTEGER NOT NULL,
    agent_key VARCHAR(120) NOT NULL,
    reasoning_type VARCHAR(80) NOT NULL,
    content TEXT NOT NULL,
    citations_json TEXT NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.agent_audit (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id UUID,
    outcome VARCHAR(40) NOT NULL,
    details_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents.agent_feedback (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    user_id UUID,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000),
    approval_decision VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_agents_type_status ON agents.agents (agent_type, status);
CREATE INDEX idx_agent_execution_status_time ON agents.agent_executions (status, created_at);
CREATE INDEX idx_agent_task_execution ON agents.agent_tasks (execution_id, priority);
CREATE INDEX idx_agent_memory_conversation ON agents.agent_memory (conversation_id, created_at);
CREATE INDEX idx_tool_invocation_tool_time ON agents.tool_invocations (tool_key, created_at);
CREATE INDEX idx_reasoning_execution ON agents.reasoning_traces (execution_id, step_number);
CREATE INDEX idx_agent_feedback_execution ON agents.agent_feedback (execution_id, created_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000901', 'AGENT_ADMIN', 'AGENT', 'ADMIN', 'Administer agent registry, tools, and orchestration settings', NOW(), NOW()),
('00000000-0000-0000-0000-000000000902', 'AGENT_EXECUTE', 'AGENT', 'EXECUTE', 'Execute assistive agent workflows', NOW(), NOW()),
('00000000-0000-0000-0000-000000000903', 'AGENT_READ', 'AGENT', 'READ', 'Read agent registry, tools, history, and memory', NOW(), NOW()),
('00000000-0000-0000-0000-000000000904', 'POLICY_REVIEWER', 'AGENT_POLICY', 'REVIEW', 'Review policy-sensitive agent outputs and approvals', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('AGENT_ADMIN', 'AGENT_EXECUTE', 'AGENT_READ', 'POLICY_REVIEWER');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000902'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000903'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000903'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000904');

INSERT INTO agents.agents (id, agent_key, name, agent_type, status, description, capabilities_json, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000911', 'survey-intelligence-agent', 'Survey Intelligence Agent', 'SURVEY', 'ACTIVE', 'Analyzes surveys, missing information, inconsistencies, follow-up questions, and summaries', '["survey_analysis","missing_information","inconsistency_detection","follow_up_questions","survey_summary"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000912', 'root-cause-analysis-agent', 'Root Cause Analysis Agent', 'ROOT_CAUSE', 'ACTIVE', 'Generates ranked root-cause hypotheses with explainable evidence and approval requirements', '["evidence_retrieval","knowledge_retrieval","factor_analysis","hypothesis_ranking","reasoning_explanation"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000913', 'recommendation-agent', 'Recommendation Agent', 'RECOMMENDATION', 'ACTIVE', 'Generates assistive intervention recommendations requiring human approval', '["scheme_search","intervention_retrieval","recommendation_generation","prioritization","confidence_estimation"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000914', 'policy-knowledge-agent', 'Policy Knowledge Agent', 'POLICY', 'ACTIVE', 'Answers policy questions with schemes, circulars, and citations', '["policy_search","scheme_retrieval","circular_retrieval","citation_answering"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000915', 'analytics-agent', 'Analytics Agent', 'ANALYTICS', 'ACTIVE', 'Performs trend, comparison, anomaly, and pattern analysis', '["trend_analysis","village_comparison","district_comparison","anomaly_detection","pattern_discovery"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000916', 'report-generation-agent', 'Report Generation Agent', 'REPORT', 'ACTIVE', 'Creates executive reports, village reports, district reports, and policy briefs', '["executive_reports","village_reports","district_reports","policy_briefs","pdf_ready_output"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000917', 'research-agent', 'Research Agent', 'RESEARCH', 'ACTIVE', 'Searches knowledge, previous surveys, similar cases, and evidence summaries', '["knowledge_search","survey_comparison","similar_cases","evidence_summary"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000918', 'data-quality-agent', 'Data Quality Agent', 'DATA_QUALITY', 'ACTIVE', 'Detects duplicate surveys, completeness gaps, anomalies, and inconsistent evidence', '["duplicate_detection","completeness_validation","anomaly_detection","evidence_consistency"]', NOW(), NOW());

INSERT INTO agents.tool_definitions (id, tool_key, name, category, description, permissions_json, metadata_json, health_status, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000921', 'survey.service', 'Survey Service Tool', 'Survey Service', 'Adapter for survey metadata, summaries, and validation context', '["SURVEY_READ"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000922', 'evidence.service', 'Evidence Service Tool', 'Evidence Service', 'Adapter for evidence metadata and audit-safe summaries', '["EVIDENCE_READ"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000923', 'knowledge.service', 'Knowledge Service Tool', 'Knowledge Service', 'Adapter for governed knowledge and policy retrieval', '["AI_READ"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000924', 'geography.service', 'Geography Service Tool', 'Geography Service', 'Adapter for administrative hierarchy and village context', '["GEO_READ"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000925', 'ai.foundation', 'AI Foundation Tool', 'AI Foundation', 'Adapter for AI gateway and safety-controlled generation', '["AI_OPERATOR"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000926', 'rag.service', 'RAG Service Tool', 'RAG Service', 'Adapter for citation-preserving retrieval augmented generation', '["AI_OPERATOR"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000927', 'analytics.service', 'Analytics Tool', 'Analytics', 'Adapter for future analytics context and anomaly summaries', '["AI_OPERATOR"]', '{"mcp":"tool-discovery","directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000928', 'future.weather.api', 'Future Weather API Tool', 'Future Weather API', 'Placeholder adapter for future weather integration', '["AGENT_ADMIN"]', '{"mcp":"future-tool","directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW()),
('00000000-0000-0000-0000-000000000929', 'future.census.api', 'Future Census API Tool', 'Future Census API', 'Placeholder adapter for future census integration', '["AGENT_ADMIN"]', '{"mcp":"future-tool","directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW()),
('00000000-0000-0000-0000-000000000930', 'future.satellite.api', 'Future Satellite API Tool', 'Future Satellite API', 'Placeholder adapter for future satellite integration', '["AGENT_ADMIN"]', '{"mcp":"future-tool","directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW());

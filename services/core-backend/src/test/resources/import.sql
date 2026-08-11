-- Purpose: Seeds the minimum identity data required by Spring integration tests.
-- Why it exists: The H2 test profile intentionally bypasses PostgreSQL Flyway migrations, so tests must not depend on production seed migrations.
-- Architecture fit: Keeps test authentication and authorization deterministic while production PostgreSQL remains migration-managed.

INSERT INTO identity.organizations (id, name, code, status, created_at, updated_at, version, is_active)
VALUES ('00000000-0000-0000-0000-000000000001', 'Platform Administration', 'PLATFORM', 'ACTIVE', NOW(), NOW(), 0, TRUE);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at, version, is_active)
VALUES
('00000000-0000-0000-0000-000000000101', 'USER_MANAGE', 'TEST_USER_MANAGE', 'ALLOW', 'Test user administration', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000102', 'ROLE_MANAGE', 'TEST_ROLE_MANAGE', 'ALLOW', 'Test role administration', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000103', 'PERMISSION_READ', 'TEST_PERMISSION_READ', 'ALLOW', 'Test permission access', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000104', 'ORGANIZATION_MANAGE', 'TEST_ORGANIZATION_MANAGE', 'ALLOW', 'Test organization administration', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000105', 'SURVEY_READ', 'TEST_SURVEY_READ', 'ALLOW', 'Test survey reads', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000106', 'SURVEY_MANAGE', 'TEST_SURVEY_MANAGE', 'ALLOW', 'Test survey writes', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000107', 'SURVEY_PUBLISH', 'TEST_SURVEY_PUBLISH', 'ALLOW', 'Test survey workflow', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000108', 'EVIDENCE_MANAGE', 'TEST_EVIDENCE_MANAGE', 'ALLOW', 'Test evidence writes', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000109', 'AI_ADMIN', 'TEST_AI_ADMIN', 'ALLOW', 'Test AI administration', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000110', 'DECISION_ANALYZE', 'TEST_DECISION_ANALYZE', 'ALLOW', 'Test decision analysis', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000111', 'DECISION_READ', 'TEST_DECISION_READ', 'ALLOW', 'Test decision reads', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000112', 'REPORT_GENERATE', 'TEST_REPORT_GENERATE', 'ALLOW', 'Test report generation', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000113', 'REPORT_READ', 'TEST_REPORT_READ', 'ALLOW', 'Test report reads', NOW(), NOW(), 0, TRUE);

INSERT INTO identity.roles (id, name, description, created_at, updated_at, version, is_active)
VALUES ('00000000-0000-0000-0000-000000000201', 'ADMINISTRATOR', 'Test administrator', NOW(), NOW(), 0, TRUE);

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id FROM identity.permissions;

-- Purpose: Mirrors the cross-module permission catalog required by secured integration tests.
-- Why it exists: Flyway normally adds these permissions across V2-V8; H2 tests use test DDL and therefore need the same authorization vocabulary.
-- Architecture fit: Keeps authorization behavior under test aligned with the production RBAC contract.
INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at, version, is_active)
VALUES
('00000000-0000-0000-0000-000000000114', 'USER_READ', 'USER', 'READ', 'View users and profiles', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000115', 'AUDIT_READ', 'AUDIT', 'READ', 'View audit events', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000304', 'SURVEY_ASSIGN', 'SURVEY_ASSIGNMENT', 'MANAGE', 'Assign surveys', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000404', 'EVIDENCE_READ', 'EVIDENCE', 'READ', 'View evidence metadata', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000405', 'EVIDENCE_DOWNLOAD', 'EVIDENCE', 'DOWNLOAD', 'Download evidence', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000501', 'GEO_READ', 'GEOSPATIAL', 'READ', 'View geospatial records', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000502', 'GEO_MANAGE', 'GEOSPATIAL', 'MANAGE', 'Manage geospatial records', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000701', 'EVENT_ADMIN', 'EVENTS', 'ADMIN', 'Administer event operations', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000702', 'EVENT_VIEWER', 'EVENTS', 'VIEW', 'View event operations', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000703', 'AUDIT_VIEWER', 'EVENT_AUDIT', 'VIEW', 'View event audit records', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000802', 'AI_OPERATOR', 'AI', 'OPERATE', 'Operate AI workflows', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000803', 'PROMPT_ENGINEER', 'AI_PROMPT', 'MANAGE', 'Manage prompts', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000804', 'AI_AUDITOR', 'AI_AUDIT', 'READ', 'Read AI telemetry', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000805', 'AI_READ', 'AI', 'READ', 'Read AI catalogs', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000901', 'AGENT_ADMIN', 'AGENT', 'ADMIN', 'Administer agents and tools', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000902', 'AGENT_EXECUTE', 'AGENT', 'EXECUTE', 'Execute agent workflows', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000903', 'AGENT_READ', 'AGENT', 'READ', 'Read agent records', NOW(), NOW(), 0, TRUE),
('00000000-0000-0000-0000-000000000904', 'POLICY_REVIEWER', 'AGENT_POLICY', 'REVIEW', 'Review policy-sensitive outputs', NOW(), NOW(), 0, TRUE);

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('USER_READ', 'AUDIT_READ', 'SURVEY_ASSIGN', 'EVIDENCE_READ', 'EVIDENCE_DOWNLOAD', 'GEO_READ', 'GEO_MANAGE', 'EVENT_ADMIN', 'EVENT_VIEWER', 'AUDIT_VIEWER', 'AI_OPERATOR', 'PROMPT_ENGINEER', 'AI_AUDITOR', 'AI_READ', 'AGENT_ADMIN', 'AGENT_EXECUTE', 'AGENT_READ', 'POLICY_REVIEWER');

INSERT INTO identity.roles (id, name, description, created_at, updated_at, version, is_active)
VALUES ('00000000-0000-0000-0000-000000000203', 'ANALYST', 'Read-only analytics test user', NOW(), NOW(), 0, TRUE);

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000203', id
FROM identity.permissions
WHERE name IN ('USER_READ', 'SURVEY_READ', 'EVIDENCE_READ', 'EVIDENCE_DOWNLOAD', 'GEO_READ');

-- Purpose: Seeds the production agent registry shape required by the agent integration contract.
-- Why it exists: V8 normally registers eight assistive agents through Flyway, which H2 intentionally does not execute.
-- Architecture fit: Provides test-only registry data without enabling autonomous behavior or changing runtime configuration.
INSERT INTO agents.agents (id, agent_key, name, agent_type, status, description, capabilities_json, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000911', 'survey-intelligence-agent', 'Survey Intelligence Agent', 'SURVEY', 'ACTIVE', 'Survey analysis', '["survey_analysis"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000912', 'root-cause-analysis-agent', 'Root Cause Analysis Agent', 'ROOT_CAUSE', 'ACTIVE', 'Root cause analysis', '["evidence_retrieval"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000913', 'recommendation-agent', 'Recommendation Agent', 'RECOMMENDATION', 'ACTIVE', 'Recommendations', '["recommendation_generation"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000914', 'policy-knowledge-agent', 'Policy Knowledge Agent', 'POLICY', 'ACTIVE', 'Policy retrieval', '["policy_search"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000915', 'analytics-agent', 'Analytics Agent', 'ANALYTICS', 'ACTIVE', 'Analytics', '["trend_analysis"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000916', 'report-generation-agent', 'Report Generation Agent', 'REPORT', 'ACTIVE', 'Report generation', '["executive_reports"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000917', 'research-agent', 'Research Agent', 'RESEARCH', 'ACTIVE', 'Research retrieval', '["knowledge_search"]', NOW(), NOW()),
('00000000-0000-0000-0000-000000000918', 'data-quality-agent', 'Data Quality Agent', 'DATA_QUALITY', 'ACTIVE', 'Data quality', '["duplicate_detection"]', NOW(), NOW());

-- Purpose: Seeds the governed tool registry expected by the agent API contract.
-- Why it exists: V8 registers ten tool definitions in PostgreSQL; H2 tests must see the same catalog without running V8.
-- Architecture fit: Tests registry discovery and permission metadata while keeping tool execution explicitly test-controlled.
INSERT INTO agents.tool_definitions (id, tool_key, name, category, description, permissions_json, metadata_json, health_status, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000921', 'survey.service', 'Survey Service Tool', 'Survey Service', 'Survey metadata adapter', '["SURVEY_READ"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000922', 'evidence.service', 'Evidence Service Tool', 'Evidence Service', 'Evidence metadata adapter', '["EVIDENCE_READ"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000923', 'knowledge.service', 'Knowledge Service Tool', 'Knowledge Service', 'Knowledge retrieval adapter', '["AI_READ"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000924', 'geography.service', 'Geography Service Tool', 'Geography Service', 'Geography context adapter', '["GEO_READ"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000925', 'ai.foundation', 'AI Foundation Tool', 'AI Foundation', 'AI gateway adapter', '["AI_OPERATOR"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000926', 'rag.service', 'RAG Service Tool', 'RAG Service', 'RAG retrieval adapter', '["AI_OPERATOR"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000927', 'analytics.service', 'Analytics Tool', 'Analytics', 'Analytics context adapter', '["AI_OPERATOR"]', '{"directServiceAccess":false}', 'HEALTHY', NOW(), NOW()),
('00000000-0000-0000-0000-000000000928', 'future.weather.api', 'Future Weather API Tool', 'Future Weather API', 'Future weather adapter', '["AGENT_ADMIN"]', '{"directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW()),
('00000000-0000-0000-0000-000000000929', 'future.census.api', 'Future Census API Tool', 'Future Census API', 'Future census adapter', '["AGENT_ADMIN"]', '{"directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW()),
('00000000-0000-0000-0000-000000000930', 'future.satellite.api', 'Future Satellite API Tool', 'Future Satellite API', 'Future satellite adapter', '["AGENT_ADMIN"]', '{"directServiceAccess":false}', 'PLACEHOLDER', NOW(), NOW());

-- Purpose: Provides the six catalog models expected by the AI foundation integration contract.
-- Why it exists: Production Flyway V7 seeds these models, but H2 tests intentionally do not execute PostgreSQL migrations.
-- Architecture fit: Keeps the test catalog aligned with the production seed contract without altering production data or behavior.
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
('00000000-0000-0000-0000-000000000821', '00000000-0000-0000-0000-000000000811', '2.5', '7B', 'Q4_K_M', 'Model-specific license', '["chat","rag","summarization"]', '["en","hi","te"]', '8GB RAM', 'Optional GPU', 32768, FALSE, 'APPROVED', NOW()),
('00000000-0000-0000-0000-000000000822', '00000000-0000-0000-0000-000000000812', '3.1', '8B', 'Q4_K_M', 'Model-specific license', '["chat","rag"]', '["en"]', '8GB RAM', 'Optional GPU', 8192, FALSE, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000823', '00000000-0000-0000-0000-000000000813', '2', '9B', 'Q4_K_M', 'Model-specific license', '["chat"]', '["en"]', '10GB RAM', 'Optional GPU', 8192, FALSE, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000824', '00000000-0000-0000-0000-000000000814', 'latest', '7B', 'Q4_K_M', 'Apache-2.0 compatible where applicable', '["chat","rag"]', '["en"]', '8GB RAM', 'Optional GPU', 32768, FALSE, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000825', '00000000-0000-0000-0000-000000000815', 'latest', '7B', 'FP16', 'Model-specific license', '["chat","reasoning"]', '["en"]', '16GB RAM', 'GPU recommended', 32768, FALSE, 'AVAILABLE', NOW()),
('00000000-0000-0000-0000-000000000826', '00000000-0000-0000-0000-000000000816', '3', '3.8B', 'Q4_K_M', 'MIT where applicable', '["chat"]', '["en"]', '4GB RAM', 'Optional GPU', 4096, FALSE, 'AVAILABLE', NOW());

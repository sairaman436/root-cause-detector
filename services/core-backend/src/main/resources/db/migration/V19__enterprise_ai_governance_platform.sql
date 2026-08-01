-- Purpose: Creates the Enterprise AI Governance Platform schema for AI-9.
-- Why it exists: AI artifacts require policy governance, compliance mapping, risk management, prompt and agent governance, tamper-evident audit trails, and board reports.
-- Architecture fit: Adds governance controls without training models, deploying models, or changing production inference behavior.

CREATE SCHEMA IF NOT EXISTS governance;

CREATE TABLE governance.governance_policies (
    id UUID PRIMARY KEY,
    policy_key VARCHAR(120) NOT NULL,
    name VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    domain VARCHAR(80) NOT NULL,
    severity VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    conflict_strategy VARCHAR(80) NOT NULL,
    owner_id UUID,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_governance_policy_key UNIQUE (policy_key),
    CONSTRAINT ck_governance_policy_status CHECK (status IN ('DRAFT','ACTIVE','SUSPENDED','RETIRED')),
    CONSTRAINT ck_governance_policy_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

CREATE TABLE governance.governance_rules (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL,
    rule_key VARCHAR(120) NOT NULL,
    rule_type VARCHAR(80) NOT NULL,
    rule_expression TEXT NOT NULL,
    enforcement_mode VARCHAR(40) NOT NULL,
    priority INTEGER NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_governance_rule_policy FOREIGN KEY (policy_id) REFERENCES governance.governance_policies(id) ON DELETE CASCADE,
    CONSTRAINT uq_governance_rule_key UNIQUE (policy_id, rule_key),
    CONSTRAINT ck_governance_rule_enforcement CHECK (enforcement_mode IN ('MONITOR','WARN','BLOCK','HUMAN_APPROVAL')),
    CONSTRAINT ck_governance_rule_status CHECK (status IN ('ACTIVE','DISABLED','RETIRED'))
);

CREATE TABLE governance.compliance_controls (
    id UUID PRIMARY KEY,
    framework VARCHAR(80) NOT NULL,
    control_code VARCHAR(80) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    implementation_status VARCHAR(40) NOT NULL,
    evidence_ref VARCHAR(240) NOT NULL,
    owner_role VARCHAR(120) NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_compliance_control UNIQUE (framework, control_code),
    CONSTRAINT ck_compliance_status CHECK (implementation_status IN ('IMPLEMENTED','PARTIAL','PLANNED','NOT_APPLICABLE'))
);

CREATE TABLE governance.risk_register (
    id UUID PRIMARY KEY,
    risk_key VARCHAR(120) NOT NULL,
    risk_type VARCHAR(80) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    likelihood VARCHAR(40) NOT NULL,
    impact VARCHAR(40) NOT NULL,
    severity VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    mitigation_plan TEXT NOT NULL,
    owner_role VARCHAR(120) NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_risk_key UNIQUE (risk_key),
    CONSTRAINT ck_risk_type CHECK (risk_type IN ('MODEL_RISK','PROMPT_RISK','DATASET_RISK','KNOWLEDGE_RISK','SECURITY_RISK','OPERATIONAL_RISK','COMPLIANCE_RISK','BIAS_RISK')),
    CONSTRAINT ck_risk_status CHECK (status IN ('OPEN','MITIGATING','ACCEPTED','CLOSED')),
    CONSTRAINT ck_risk_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

CREATE TABLE governance.risk_assessments (
    id UUID PRIMARY KEY,
    risk_id UUID,
    artifact_type VARCHAR(80) NOT NULL,
    artifact_ref VARCHAR(180) NOT NULL,
    inherent_score INTEGER NOT NULL,
    residual_score INTEGER NOT NULL,
    assessment_notes TEXT NOT NULL,
    assessed_by UUID,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_risk_assessment_risk FOREIGN KEY (risk_id) REFERENCES governance.risk_register(id) ON DELETE SET NULL,
    CONSTRAINT ck_risk_score_range CHECK (inherent_score BETWEEN 0 AND 100 AND residual_score BETWEEN 0 AND 100)
);

CREATE TABLE governance.prompt_registry (
    id UUID PRIMARY KEY,
    prompt_key VARCHAR(120) NOT NULL,
    version VARCHAR(60) NOT NULL,
    owner_role VARCHAR(120) NOT NULL,
    risk_classification VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    rollback_version VARCHAR(60),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_prompt_registry_version UNIQUE (prompt_key, version),
    CONSTRAINT ck_prompt_status CHECK (status IN ('DRAFT','IN_REVIEW','APPROVED','REJECTED','ACTIVE','ROLLED_BACK','RETIRED')),
    CONSTRAINT ck_prompt_risk CHECK (risk_classification IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

CREATE TABLE governance.prompt_approvals (
    id UUID PRIMARY KEY,
    prompt_id UUID NOT NULL,
    decision VARCHAR(40) NOT NULL,
    rationale TEXT NOT NULL,
    decided_by UUID,
    approval_chain TEXT NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prompt_approval_prompt FOREIGN KEY (prompt_id) REFERENCES governance.prompt_registry(id) ON DELETE CASCADE,
    CONSTRAINT ck_prompt_approval_decision CHECK (decision IN ('APPROVED','REJECTED','ROLLED_BACK'))
);

CREATE TABLE governance.agent_registry (
    id UUID PRIMARY KEY,
    agent_key VARCHAR(120) NOT NULL,
    version VARCHAR(60) NOT NULL,
    owner_role VARCHAR(120) NOT NULL,
    maximum_autonomy_level VARCHAR(40) NOT NULL,
    allowed_tools TEXT NOT NULL,
    escalation_rules TEXT NOT NULL,
    human_approval_rules TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_agent_registry_version UNIQUE (agent_key, version),
    CONSTRAINT ck_agent_status CHECK (status IN ('DRAFT','IN_REVIEW','APPROVED','ACTIVE','SUSPENDED','RETIRED'))
);

CREATE TABLE governance.agent_policies (
    id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    policy_id UUID NOT NULL,
    tool_permissions TEXT NOT NULL,
    execution_policy TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_agent_policy_agent FOREIGN KEY (agent_id) REFERENCES governance.agent_registry(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_policy_policy FOREIGN KEY (policy_id) REFERENCES governance.governance_policies(id) ON DELETE CASCADE,
    CONSTRAINT uq_agent_policy UNIQUE (agent_id, policy_id),
    CONSTRAINT ck_agent_policy_status CHECK (status IN ('ACTIVE','DISABLED','RETIRED'))
);

CREATE TABLE governance.audit_records (
    id UUID PRIMARY KEY,
    event_type VARCHAR(120) NOT NULL,
    artifact_type VARCHAR(80) NOT NULL,
    artifact_ref VARCHAR(180) NOT NULL,
    actor_id UUID,
    decision VARCHAR(40) NOT NULL,
    evidence_json TEXT NOT NULL,
    policy_compliance_status VARCHAR(80) NOT NULL,
    event_hash VARCHAR(128) NOT NULL,
    previous_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_audit_event_hash UNIQUE (event_hash)
);

CREATE TABLE governance.policy_violations (
    id UUID PRIMARY KEY,
    policy_id UUID,
    artifact_type VARCHAR(80) NOT NULL,
    artifact_ref VARCHAR(180) NOT NULL,
    violation_type VARCHAR(120) NOT NULL,
    severity VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    remediation TEXT NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_policy_violation_policy FOREIGN KEY (policy_id) REFERENCES governance.governance_policies(id) ON DELETE SET NULL,
    CONSTRAINT ck_policy_violation_status CHECK (status IN ('OPEN','ACKNOWLEDGED','MITIGATED','WAIVED')),
    CONSTRAINT ck_policy_violation_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

CREATE TABLE governance.governance_reports (
    id UUID PRIMARY KEY,
    report_type VARCHAR(80) NOT NULL,
    period VARCHAR(80) NOT NULL,
    policy_violations INTEGER NOT NULL,
    open_risks INTEGER NOT NULL,
    compliance_score DOUBLE PRECISION NOT NULL,
    model_trust_score DOUBLE PRECISION NOT NULL,
    dataset_trust_score DOUBLE PRECISION NOT NULL,
    summary_json TEXT NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_governance_policy_domain_status ON governance.governance_policies(domain, status);
CREATE INDEX idx_governance_rule_policy_priority ON governance.governance_rules(policy_id, priority);
CREATE INDEX idx_compliance_framework_status ON governance.compliance_controls(framework, implementation_status);
CREATE INDEX idx_risk_type_status_severity ON governance.risk_register(risk_type, status, severity);
CREATE INDEX idx_risk_assessment_artifact ON governance.risk_assessments(artifact_type, artifact_ref);
CREATE INDEX idx_prompt_registry_status ON governance.prompt_registry(status, risk_classification);
CREATE INDEX idx_agent_registry_status ON governance.agent_registry(status, maximum_autonomy_level);
CREATE INDEX idx_audit_artifact_created ON governance.audit_records(artifact_type, artifact_ref, created_at);
CREATE INDEX idx_audit_event_type_created ON governance.audit_records(event_type, created_at);
CREATE INDEX idx_policy_violation_status ON governance.policy_violations(status, severity, detected_at);

-- Purpose: Adds operational governance data structures for production hardening evidence.
-- Why it exists: Captures deployment, migration, performance, and backup reports required by enterprise readiness gates.
-- Architecture fit: Supports Milestone 11 operations, DevSecOps, high availability, disaster recovery, and auditability without changing business domains.

CREATE SCHEMA IF NOT EXISTS operations;

CREATE TABLE operations.operational_dashboards (
    id UUID PRIMARY KEY,
    dashboard_key VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(80) NOT NULL,
    owner_team VARCHAR(120) NOT NULL,
    grafana_uid VARCHAR(120),
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE operations.migration_validation_reports (
    id UUID PRIMARY KEY,
    service_name VARCHAR(120) NOT NULL,
    migration_version VARCHAR(80) NOT NULL,
    environment VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    checksum VARCHAR(128),
    validated_at TIMESTAMP NOT NULL,
    evidence_uri VARCHAR(512),
    notes TEXT
);

CREATE TABLE operations.performance_reports (
    id UUID PRIMARY KEY,
    report_key VARCHAR(120) NOT NULL UNIQUE,
    environment VARCHAR(80) NOT NULL,
    workload_name VARCHAR(160) NOT NULL,
    p95_latency_ms NUMERIC(12, 2) NOT NULL,
    p99_latency_ms NUMERIC(12, 2) NOT NULL,
    error_rate NUMERIC(8, 5) NOT NULL,
    throughput_per_second NUMERIC(12, 2) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    evidence_uri VARCHAR(512)
);

CREATE TABLE operations.backup_reports (
    id UUID PRIMARY KEY,
    backup_key VARCHAR(160) NOT NULL UNIQUE,
    resource_type VARCHAR(80) NOT NULL,
    resource_name VARCHAR(160) NOT NULL,
    environment VARCHAR(80) NOT NULL,
    backup_status VARCHAR(40) NOT NULL,
    restore_status VARCHAR(40) NOT NULL,
    recovery_point_objective_minutes INTEGER NOT NULL,
    recovery_time_objective_minutes INTEGER NOT NULL,
    backup_started_at TIMESTAMP NOT NULL,
    backup_completed_at TIMESTAMP,
    restore_validated_at TIMESTAMP,
    evidence_uri VARCHAR(512)
);

CREATE INDEX idx_migration_validation_service_env ON operations.migration_validation_reports(service_name, environment, validated_at);
CREATE INDEX idx_performance_reports_env_workload ON operations.performance_reports(environment, workload_name, generated_at);
CREATE INDEX idx_backup_reports_resource_env ON operations.backup_reports(resource_type, resource_name, environment, backup_started_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001101', 'PLATFORM_READ', 'PLATFORM', 'READ', 'Read platform operations status, version, and dashboards', NOW(), NOW()),
('00000000-0000-0000-0000-000000001102', 'PLATFORM_ADMIN', 'PLATFORM', 'ADMIN', 'Administer production operations, deployment evidence, and readiness gates', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('PLATFORM_READ', 'PLATFORM_ADMIN');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000205', id
FROM identity.permissions
WHERE name = 'PLATFORM_READ';

INSERT INTO operations.operational_dashboards (id, dashboard_key, title, category, owner_team, grafana_uid, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000001121', 'platform-overview', 'Platform Overview', 'INFRASTRUCTURE', 'SRE', 'airural-platform-overview', 'Tracks service health, request volume, errors, and saturation.', NOW(), NOW()),
('00000000-0000-0000-0000-000000001122', 'ai-mlops-governance', 'AI and MLOps Governance', 'AI', 'MLOps', 'airural-ai-mlops', 'Tracks model, prompt, embedding, guardrail, and drift signals.', NOW(), NOW()),
('00000000-0000-0000-0000-000000001123', 'decision-intelligence-quality', 'Decision Intelligence Quality', 'BUSINESS', 'Decision Intelligence', 'airural-decision-quality', 'Tracks confidence, rule violations, overrides, and recommendation acceptance.', NOW(), NOW());

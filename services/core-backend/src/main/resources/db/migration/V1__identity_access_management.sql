-- Purpose: Creates the Identity & Access Management schema for Milestone 2.
-- Why it exists: Stores organizations, users, roles, permissions, refresh tokens, and audit events in PostgreSQL.
-- Architecture fit: Implements the approved identity, RBAC, token lifecycle, and audit logging data foundation.

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE identity.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE identity.permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    resource VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action)
);

CREATE TABLE identity.roles (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE identity.role_permissions (
    role_id UUID NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES identity.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES identity.organizations(id),
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    full_name VARCHAR(180) NOT NULL,
    phone_number VARCHAR(40),
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE identity.user_roles (
    user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE identity.refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE audit.audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    event_type VARCHAR(120) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    ip_address VARCHAR(80),
    user_agent VARCHAR(500),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_users_organization ON identity.users (organization_id);
CREATE INDEX idx_users_status ON identity.users (status);
CREATE INDEX idx_refresh_tokens_user ON identity.refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON identity.refresh_tokens (expires_at);
CREATE INDEX idx_audit_events_actor ON audit.audit_events (actor_user_id);
CREATE INDEX idx_audit_events_type_created ON audit.audit_events (event_type, created_at);

INSERT INTO identity.organizations (id, name, code, status, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Platform Administration', 'PLATFORM', 'ACTIVE', NOW(), NOW());

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000101', 'USER_READ', 'USER', 'READ', 'View users and profiles', NOW(), NOW()),
('00000000-0000-0000-0000-000000000102', 'USER_MANAGE', 'USER', 'MANAGE', 'Create and administer users', NOW(), NOW()),
('00000000-0000-0000-0000-000000000103', 'ROLE_MANAGE', 'ROLE', 'MANAGE', 'Create and administer roles', NOW(), NOW()),
('00000000-0000-0000-0000-000000000104', 'PERMISSION_READ', 'PERMISSION', 'READ', 'View permissions', NOW(), NOW()),
('00000000-0000-0000-0000-000000000105', 'ORGANIZATION_MANAGE', 'ORGANIZATION', 'MANAGE', 'Create and administer organizations', NOW(), NOW()),
('00000000-0000-0000-0000-000000000106', 'AUDIT_READ', 'AUDIT', 'READ', 'View audit events', NOW(), NOW());

INSERT INTO identity.roles (id, name, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000201', 'ADMINISTRATOR', 'Full platform management', NOW(), NOW()),
('00000000-0000-0000-0000-000000000202', 'FIELD_SURVEYOR', 'Survey collection user', NOW(), NOW()),
('00000000-0000-0000-0000-000000000203', 'ANALYST', 'Analytics and reporting user', NOW(), NOW()),
('00000000-0000-0000-0000-000000000204', 'AI_OPERATOR', 'AI model operations user', NOW(), NOW()),
('00000000-0000-0000-0000-000000000205', 'AUDITOR', 'Read-only audit user', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id FROM identity.permissions;

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000101'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000106');

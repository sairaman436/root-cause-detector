-- Purpose: Creates the Enterprise Survey Management schema for Milestone 3.
-- Why it exists: Surveys are the operational evidence foundation for future AI decision intelligence.
-- Architecture fit: Adds the approved survey domain without introducing AI, analytics, Kafka, or reporting concerns.

CREATE SCHEMA IF NOT EXISTS survey;

CREATE TABLE survey.survey_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    metadata_json TEXT,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_survey_templates_name_category UNIQUE (name, category)
);

CREATE TABLE survey.surveys (
    id UUID PRIMARY KEY,
    template_id UUID REFERENCES survey.survey_templates(id),
    organization_id UUID NOT NULL REFERENCES identity.organizations(id),
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(40) NOT NULL,
    current_version INTEGER NOT NULL,
    cloned_from_survey_id UUID REFERENCES survey.surveys(id),
    archived_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_surveys_org_name_active UNIQUE (organization_id, name, is_active)
);

CREATE TABLE survey.survey_versions (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    snapshot_json TEXT,
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_survey_versions_number UNIQUE (survey_id, version_number)
);

CREATE TABLE survey.survey_sections (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    parent_section_id UUID REFERENCES survey.survey_sections(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    title VARCHAR(220) NOT NULL,
    description VARCHAR(1000),
    order_index INTEGER NOT NULL,
    is_repeatable BOOLEAN NOT NULL DEFAULT FALSE,
    condition_expression TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_survey_sections_code UNIQUE (survey_id, code)
);

CREATE TABLE survey.survey_questions (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    section_id UUID NOT NULL REFERENCES survey.survey_sections(id) ON DELETE CASCADE,
    parent_question_id UUID REFERENCES survey.survey_questions(id) ON DELETE CASCADE,
    code VARCHAR(120) NOT NULL,
    prompt VARCHAR(500) NOT NULL,
    help_text VARCHAR(1000),
    question_type VARCHAR(80) NOT NULL,
    order_index INTEGER NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    default_value TEXT,
    condition_expression TEXT,
    calculation_expression TEXT,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_survey_questions_code UNIQUE (survey_id, code)
);

CREATE TABLE survey.question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES survey.survey_questions(id) ON DELETE CASCADE,
    option_value VARCHAR(220) NOT NULL,
    label VARCHAR(220) NOT NULL,
    order_index INTEGER NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_question_options_value UNIQUE (question_id, option_value)
);

CREATE TABLE survey.validation_rules (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    question_id UUID REFERENCES survey.survey_questions(id) ON DELETE CASCADE,
    rule_type VARCHAR(80) NOT NULL,
    expression TEXT,
    message VARCHAR(500) NOT NULL,
    params_json TEXT,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE survey.survey_assignments (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    assignment_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(160) NOT NULL,
    target_name VARCHAR(220),
    assigned_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_survey_assignment_target UNIQUE (survey_id, assignment_type, target_id, is_active)
);

CREATE TABLE survey.survey_status_history (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    changed_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE survey.survey_tags (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES survey.surveys(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_survey_tags_name UNIQUE (survey_id, name)
);

CREATE INDEX idx_surveys_status ON survey.surveys (status);
CREATE INDEX idx_surveys_organization ON survey.surveys (organization_id);
CREATE INDEX idx_surveys_created_by ON survey.surveys (created_by_user_id);
CREATE INDEX idx_surveys_updated_at ON survey.surveys (updated_at);
CREATE INDEX idx_survey_templates_category_status ON survey.survey_templates (category, status);
CREATE INDEX idx_survey_versions_survey ON survey.survey_versions (survey_id, version_number);
CREATE INDEX idx_survey_sections_survey_order ON survey.survey_sections (survey_id, order_index);
CREATE INDEX idx_survey_questions_section_order ON survey.survey_questions (section_id, order_index);
CREATE INDEX idx_survey_questions_type ON survey.survey_questions (question_type);
CREATE INDEX idx_question_options_question_order ON survey.question_options (question_id, order_index);
CREATE INDEX idx_validation_rules_question ON survey.validation_rules (question_id);
CREATE INDEX idx_validation_rules_survey ON survey.validation_rules (survey_id);
CREATE INDEX idx_survey_assignments_target ON survey.survey_assignments (assignment_type, target_id);
CREATE INDEX idx_survey_status_history_survey ON survey.survey_status_history (survey_id, created_at);
CREATE INDEX idx_survey_tags_name ON survey.survey_tags (name);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000301', 'SURVEY_READ', 'SURVEY', 'READ', 'View surveys and survey templates', NOW(), NOW()),
('00000000-0000-0000-0000-000000000302', 'SURVEY_MANAGE', 'SURVEY', 'MANAGE', 'Create and maintain survey definitions', NOW(), NOW()),
('00000000-0000-0000-0000-000000000303', 'SURVEY_PUBLISH', 'SURVEY', 'PUBLISH', 'Approve, publish, activate, complete, and archive surveys', NOW(), NOW()),
('00000000-0000-0000-0000-000000000304', 'SURVEY_ASSIGN', 'SURVEY_ASSIGNMENT', 'MANAGE', 'Assign surveys to organizations, teams, users, and regions', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('SURVEY_READ', 'SURVEY_MANAGE', 'SURVEY_PUBLISH', 'SURVEY_ASSIGN');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000301'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000301');

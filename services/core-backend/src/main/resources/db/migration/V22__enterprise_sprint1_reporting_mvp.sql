-- Purpose: Adds Sprint 1 report generation persistence and RBAC.
-- Why it exists: The end-to-end MVP must persist executive, village, and district reports generated from AI decision outputs.
-- Architecture fit: Creates the Reports bounded context without changing survey, evidence, AI, or decision ownership.

CREATE SCHEMA IF NOT EXISTS reports;

CREATE TABLE reports.generated_reports (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    survey_id UUID,
    organization_id UUID,
    report_type VARCHAR(40) NOT NULL,
    title VARCHAR(220) NOT NULL,
    status VARCHAR(40) NOT NULL,
    executive_summary TEXT NOT NULL,
    content_markdown TEXT NOT NULL,
    csv_content TEXT NOT NULL,
    generated_by UUID,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_reports_decision ON reports.generated_reports (decision_id, generated_at);
CREATE INDEX idx_reports_survey ON reports.generated_reports (survey_id, generated_at);
CREATE INDEX idx_reports_organization ON reports.generated_reports (organization_id, generated_at);
CREATE INDEX idx_reports_type_status ON reports.generated_reports (report_type, status);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000002201', 'REPORT_READ', 'REPORT', 'READ', 'Read generated reports and exports', NOW(), NOW()),
('00000000-0000-0000-0000-000000002202', 'REPORT_GENERATE', 'REPORT', 'GENERATE', 'Generate PDF and CSV reports from decision intelligence outputs', NOW(), NOW()),
('00000000-0000-0000-0000-000000002203', 'REPORT_ADMIN', 'REPORT', 'ADMIN', 'Administer generated report lifecycle and governance', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('REPORT_READ', 'REPORT_GENERATE', 'REPORT_ADMIN');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000002201'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000002202'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000002201'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000002201');

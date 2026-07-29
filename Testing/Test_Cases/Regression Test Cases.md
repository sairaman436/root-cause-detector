# Regression Test Cases

**Document ID:** TC-REG-AIRRCD-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** Regression Testing  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** QA Team  
**Reviewed By:** QA Lead, Solution Architect  
**Approved By:** Project Manager

---

# Revision History

| Version | Date | Author | Description |
|----------|------|--------|-------------|
| 0.1 | DD-MM-YYYY | QA Team | Initial Draft |
| 0.5 | DD-MM-YYYY | QA Lead | Technical Review |
| 1.0 | DD-MM-YYYY | Project Manager | Approved |

---

# Purpose

This document defines regression test cases for validating that enhancements, defect fixes, infrastructure changes, AI model updates, and configuration changes do not negatively impact existing functionality within the AI Rural Root Cause Discovery System.

Regression testing ensures that previously verified functionality continues to operate correctly after every software release.

---

# Scope

Regression testing covers:

- Authentication
- Authorization
- User Management
- Survey Management
- AI Prediction
- Recommendation Engine
- Reporting
- Dashboard
- Notifications
- API Gateway
- Database
- Security
- Performance
- Monitoring
- AI Model Updates
- End-to-End Business Workflows

---

# Requirement Traceability

| Requirement ID | Description |
|----------------|-------------|
| REG-001 | Functional Regression |
| REG-002 | Security Regression |
| REG-003 | Performance Regression |
| REG-004 | AI Model Regression |
| REG-005 | Database Regression |
| REG-006 | End-to-End Regression |

---

# Test Case Summary

| Category | Planned |
|----------|---------|
| Functional Regression | 40 |
| Security Regression | 10 |
| Performance Regression | 10 |
| AI Model Regression | 15 |
| Database Regression | 10 |
| End-to-End Regression | 15 |
| Total | 100 |

---

# Regression Test Cases

# Authentication Regression

## TC-REG-AUTH-001

### Title

Verify Successful Login After Release

### Requirement

REG-001

### Priority

Critical

### Severity

Critical

### Preconditions

- Application deployed.
- User account active.

### Steps

1. Navigate to Login page.
2. Enter valid credentials.
3. Click Login.

### Expected Result

- Login succeeds.
- Dashboard displayed.
- Session created.
- Audit log recorded.

---

## TC-REG-AUTH-002

### Title

Verify Invalid Login Handling

### Requirement

REG-001

### Priority

Critical

### Severity

High

### Steps

1. Enter incorrect password.
2. Submit login request.

### Expected Result

- Login denied.
- Error message displayed.
- Failed login logged.

---

## TC-REG-AUTH-003

### Title

Verify Session Timeout

### Requirement

REG-001

### Priority

Medium

### Severity

Medium

### Steps

1. Login successfully.
2. Remain inactive.

### Expected Result

- Session expires.
- User redirected to Login page.

---

# User Management Regression

## TC-REG-USER-001

### Title

Create New User

### Requirement

REG-001

### Priority

High

### Severity

Medium

### Steps

1. Create new user.

### Expected Result

- User created.
- Default role assigned.
- Audit record generated.

---

## TC-REG-USER-002

### Title

Modify Existing User

### Requirement

REG-001

### Priority

High

### Severity

Medium

### Steps

1. Update user profile.

### Expected Result

- Changes saved successfully.
- Existing functionality unaffected.

---

## TC-REG-USER-003

### Title

Deactivate User

### Requirement

REG-001

### Priority

Medium

### Severity

Medium

### Steps

1. Deactivate account.

### Expected Result

- User disabled.
- Login blocked.

---

# Survey Regression

## TC-REG-SURVEY-001

### Title

Create Survey

### Requirement

REG-001

### Priority

Critical

### Severity

High

### Steps

1. Create survey.
2. Publish survey.

### Expected Result

- Survey published successfully.

---

## TC-REG-SURVEY-002

### Title

Submit Survey

### Requirement

REG-001

### Priority

Critical

### Severity

Critical

### Steps

1. Complete survey.
2. Submit responses.

### Expected Result

- Responses saved.
- AI prediction triggered.

---

## TC-REG-SURVEY-003

### Title

Edit Draft Survey

### Requirement

REG-001

### Priority

Medium

### Severity

Low

### Steps

1. Open draft.
2. Modify questions.
3. Save.

### Expected Result

- Draft updated successfully.

---

# AI Prediction Regression

## TC-REG-AI-001

### Title

Generate Prediction

### Requirement

REG-004

### Priority

Critical

### Severity

Critical

### Steps

1. Submit completed survey.

### Expected Result

- Prediction generated successfully.
- Confidence score displayed.

---

## TC-REG-AI-002

### Title

Recommendation Generation

### Requirement

REG-004

### Priority

Critical

### Severity

High

### Steps

1. Execute prediction.

### Expected Result

- Recommendations generated correctly.
- Priority ordering maintained.

---

## TC-REG-AI-003

### Title

Explainability Generation

### Requirement

REG-004

### Priority

High

### Severity

Medium

### Steps

1. Open prediction details.

### Expected Result

- SHAP explanation displayed.
- Feature importance visible.

---

# Reporting Regression

## TC-REG-REPORT-001

### Title

Generate Standard Report

### Requirement

REG-001

### Priority

High

### Severity

Medium

### Steps

1. Generate report.

### Expected Result

- Report generated successfully.
- Latest system data included.

---

## TC-REG-REPORT-002

### Title

Export Report

### Requirement

REG-001

### Priority

Medium

### Severity

Low

### Steps

1. Export report to PDF.

### Expected Result

- Export completes successfully.
- Formatting preserved.

---

## TC-REG-REPORT-003

### Title

Dashboard Refresh

### Requirement

REG-001

### Priority

Medium

### Severity

Low

### Steps

1. Submit survey.
2. Generate prediction.

### Expected Result

- Dashboard updates automatically.

# Notification Regression

## TC-REG-NOTIFY-001

### Title

Survey Assignment Notification After Release

### Requirement

REG-001

### Priority

High

### Severity

Medium

### Preconditions

- Notification service operational.
- User notification preferences configured.

### Steps

1. Assign survey to Field Officer.
2. Save assignment.

### Expected Result

- Notification generated successfully.
- Email/In-App notification delivered.
- Delivery status updated.
- Audit entry created.

---

## TC-REG-NOTIFY-002

### Title

AI Prediction Notification

### Requirement

REG-001

### Priority

Medium

### Severity

Medium

### Steps

1. Execute AI prediction.

### Expected Result

- Prediction completion notification delivered.
- Notification history updated.
- Delivery timestamp recorded.

---

## TC-REG-NOTIFY-003

### Title

Report Generation Notification

### Requirement

REG-001

### Priority

Medium

### Severity

Low

### Steps

1. Generate analytics report.

### Expected Result

- Report availability notification delivered.
- Download link functional.

---

# API Regression

## TC-REG-API-001

### Title

Authentication API Regression

### Requirement

REG-001

### Priority

Critical

### Severity

Critical

### Steps

1. Invoke Login API using valid credentials.

### Expected Result

- HTTP 200 returned.
- JWT generated.
- Response format unchanged.

---

## TC-REG-API-002

### Title

Survey API Regression

### Requirement

REG-001

### Priority

Critical

### Severity

High

### Steps

1. Submit survey using REST API.

### Expected Result

- Survey stored successfully.
- API response matches specification.

---

## TC-REG-API-003

### Title

Prediction API Regression

### Requirement

REG-004

### Priority

Critical

### Severity

Critical

### Steps

1. Submit prediction request.

### Expected Result

- Prediction generated successfully.
- Confidence score returned.
- Response schema unchanged.

---

# Database Regression

## TC-REG-DB-001

### Title

Database CRUD Regression

### Requirement

REG-005

### Priority

Critical

### Severity

High

### Steps

1. Create record.
2. Read record.
3. Update record.
4. Delete record.

### Expected Result

- CRUD operations complete successfully.
- No regression observed.

---

## TC-REG-DB-002

### Title

Database Constraint Validation

### Requirement

REG-005

### Priority

High

### Severity

Medium

### Steps

1. Insert invalid foreign key.
2. Insert duplicate unique value.

### Expected Result

- Constraint enforcement unchanged.
- Database integrity maintained.

---

## TC-REG-DB-003

### Title

Stored Procedure Regression

### Requirement

REG-005

### Priority

Medium

### Severity

Medium

### Steps

1. Execute production stored procedures.

### Expected Result

- Procedures execute successfully.
- Results remain consistent.

---

# Security Regression

## TC-REG-SEC-001

### Title

Role-Based Access Regression

### Requirement

REG-002

### Priority

Critical

### Severity

Critical

### Steps

1. Login using Field Officer account.
2. Attempt Administrator operation.

### Expected Result

- Unauthorized access denied.
- Authorization unchanged.

---

## TC-REG-SEC-002

### Title

SQL Injection Regression

### Requirement

REG-002

### Priority

Critical

### Severity

Critical

### Steps

1. Submit SQL injection payload.

### Expected Result

- Request rejected.
- Database protected.
- Security logs updated.

---

## TC-REG-SEC-003

### Title

Cross-Site Scripting Regression

### Requirement

REG-002

### Priority

High

### Severity

High

### Steps

1. Submit malicious JavaScript payload.

### Expected Result

- Input sanitized.
- Script execution prevented.

---

## TC-REG-SEC-004

### Title

Session Management Regression

### Requirement

REG-002

### Priority

High

### Severity

Medium

### Steps

1. Login.
2. Logout.
3. Attempt reuse of previous session.

### Expected Result

- Session invalidated.
- Access denied.

---

# Performance Regression

## TC-REG-PERF-001

### Title

Login Response Time Regression

### Requirement

REG-003

### Priority

High

### Severity

Medium

### Steps

1. Execute login request.

### Expected Result

- Login completes within SLA.
- No degradation from previous release.

---

## TC-REG-PERF-002

### Title

Survey Submission Performance Regression

### Requirement

REG-003

### Priority

High

### Severity

Medium

### Steps

1. Submit production-sized survey.

### Expected Result

- Submission completes within SLA.
- Database performance unchanged.

---

## TC-REG-PERF-003

### Title

AI Prediction Performance Regression

### Requirement

REG-004

### Priority

Critical

### Severity

High

### Steps

1. Execute AI prediction.

### Expected Result

- Prediction latency remains within SLA.
- No measurable degradation.

---

## TC-REG-PERF-004

### Title

Dashboard Performance Regression

### Requirement

REG-003

### Priority

Medium

### Severity

Low

### Steps

1. Load analytics dashboard.

### Expected Result

- Dashboard loads within SLA.
- Charts render successfully.

---

# AI Model Regression

## TC-REG-AI-MODEL-001

### Title

Prediction Accuracy After Model Update

### Requirement

REG-004

### Priority

Critical

### Severity

Critical

### Steps

1. Execute benchmark dataset.
2. Compare predictions.

### Expected Result

- Accuracy meets approved threshold.
- No significant regression detected.

---

## TC-REG-AI-MODEL-002

### Title

Confidence Score Stability

### Requirement

REG-004

### Priority

High

### Severity

Medium

### Steps

1. Execute repeated predictions.

### Expected Result

- Confidence scores remain consistent.
- Calibration unchanged.

---

## TC-REG-AI-MODEL-003

### Title

Explainability Regression

### Requirement

REG-004

### Priority

High

### Severity

Medium

### Steps

1. Generate SHAP explanation.

### Expected Result

- Feature importance generated correctly.
- Explanation service operational.

---

## TC-REG-AI-MODEL-004

### Title

Model Drift Validation After Release

### Requirement

REG-004

### Priority

High

### Severity

High

### Steps

1. Execute drift monitoring.

### Expected Result

- Drift calculations accurate.
- Alerts generated only when thresholds exceeded.

---

# Monitoring Regression

## TC-REG-MON-001

### Title

Application Health Dashboard Regression

### Requirement

REG-001

### Priority

Medium

### Severity

Low

### Steps

1. Open monitoring dashboard.

### Expected Result

- All services displayed correctly.
- Health indicators accurate.

---

## TC-REG-MON-002

### Title

Alert Generation Regression

### Requirement

REG-001

### Priority

Medium

### Severity

Medium

### Steps

1. Simulate application failure.

### Expected Result

- Alert generated.
- Notification delivered.
- Incident logged.

# End-to-End Regression

## TC-REG-E2E-001

### Title

Complete Survey-to-Prediction Workflow Regression

### Requirement

REG-006

### Priority

Critical

### Severity

Critical

### Preconditions

- System deployed successfully.
- AI services operational.
- Test user available.

### Steps

1. Login as Field Officer.
2. Access assigned survey.
3. Complete all survey questions.
4. Submit survey.
5. Wait for AI prediction.
6. Review recommendations.

### Expected Result

- Survey submitted successfully.
- AI prediction generated.
- Confidence score displayed.
- Recommendations generated.
- Audit logs recorded.

---

## TC-REG-E2E-002

### Title

Survey-to-Report Workflow Regression

### Requirement

REG-006

### Priority

Critical

### Severity

High

### Steps

1. Submit completed survey.
2. Generate AI prediction.
3. Generate analytics report.
4. Export report.

### Expected Result

- Complete workflow executes successfully.
- Exported report matches generated data.

---

## TC-REG-E2E-003

### Title

Administrator Workflow Regression

### Requirement

REG-006

### Priority

High

### Severity

Medium

### Steps

1. Login as Administrator.
2. Create user.
3. Assign role.
4. Activate account.
5. Login using new account.

### Expected Result

- User lifecycle functions correctly.
- Permissions applied immediately.

---

## TC-REG-E2E-004

### Title

Notification Workflow Regression

### Requirement

REG-006

### Priority

Medium

### Severity

Medium

### Steps

1. Assign survey.
2. Submit survey.
3. Complete prediction.
4. Generate report.

### Expected Result

- All notifications delivered successfully.
- Notification history maintained.

---

## TC-REG-E2E-005

### Title

Audit Logging Regression

### Requirement

REG-006

### Priority

High

### Severity

Medium

### Steps

1. Perform user login.
2. Submit survey.
3. Generate prediction.
4. Export report.
5. Logout.

### Expected Result

- Every action logged.
- Timestamps accurate.
- User identifiers recorded.
- Audit integrity maintained.

---

# Regression Coverage Summary

| Functional Area | Coverage Status |
|-----------------------------|----------------|
| Authentication | Complete |
| Authorization | Complete |
| User Management | Complete |
| Survey Management | Complete |
| AI Prediction | Complete |
| Recommendation Engine | Complete |
| Reporting | Complete |
| Dashboard | Complete |
| Notifications | Complete |
| API Gateway | Complete |
| Database | Complete |
| Security | Complete |
| Performance | Complete |
| Monitoring | Complete |
| AI Model Validation | Complete |
| End-to-End Workflows | Complete |

---

# Regression Quality Metrics

| Metric | Target |
|---------|--------|
| Regression Pass Rate | ≥98% |
| Critical Test Pass Rate | 100% |
| High Priority Test Pass Rate | ≥99% |
| Functional Coverage | 100% |
| Automated Regression Coverage | ≥90% |
| AI Prediction Accuracy | ≥90% |
| AI Prediction Consistency | ≥95% |
| API Success Rate | ≥99% |
| Dashboard Availability | ≥99.9% |
| Report Generation Success | ≥99% |
| Notification Success Rate | ≥99% |
| Database Integrity | 100% |
| Security Regression Pass Rate | 100% |
| Performance Degradation | ≤5% |
| Open Critical Defects | 0 |
| Open High Severity Defects | 0 |

---

# Entry Criteria

Regression testing shall begin only when:

- Code changes are merged into the release branch.
- Build verification testing has passed.
- Functional testing completed successfully.
- Deployment completed successfully.
- Regression environment is stable.
- Test data refreshed.
- Automated regression suite updated.
- Known blocker defects resolved.

---

# Exit Criteria

Regression testing shall be considered complete when:

- All Critical regression test cases pass.
- All High priority regression test cases pass.
- No Critical defects remain open.
- No High severity defects remain unresolved.
- AI prediction quality remains within approved thresholds.
- Performance regression remains within acceptable limits.
- Product Owner approves release readiness.
- QA Lead signs off regression execution.

---

# Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Regression suite outdated | High | Review and update regression suite every release |
| AI model update changes prediction behavior | High | Execute benchmark datasets and compare against approved baselines |
| Configuration drift | Medium | Validate environment configuration before execution |
| API contract changes | High | Execute automated API contract validation |
| Database schema modifications | High | Perform schema validation and migration testing |
| Third-party service changes | Medium | Execute integration smoke tests before regression |
| Performance degradation | High | Run automated performance benchmarks after every release |
| Incomplete automation coverage | Medium | Continuously increase automation coverage and prioritize critical workflows |

---

# Test Deliverables

The following deliverables shall be produced:

- Regression Test Plan
- Regression Test Cases
- Automated Regression Execution Report
- Manual Regression Execution Report
- Defect Report
- AI Regression Validation Report
- Performance Regression Report
- Security Regression Report
- Requirement Traceability Matrix
- Release Readiness Report
- Regression Test Summary Report
- QA Sign-Off Document

---

# References

## Standards

- ISO/IEC 29119 – Software Testing
- ISO/IEC 25010 – Systems and Software Quality Models
- IEEE 829 – Software Test Documentation
- ISO/IEC 27001 – Information Security Management Systems
- NIST AI Risk Management Framework (AI RMF 1.0)
- OWASP ASVS
- OWASP API Security Top 10

---

## Project Documents

- Software Requirements Specification (SRS)
- Software Architecture Document (SAD)
- System Design Specification
- AI Architecture Document
- Database Design Specification
- Master Test Plan
- System Test Plan
- AI Model Test Plan
- Performance Test Plan
- Security Test Plan
- Deployment Guide
- Operations Runbook

---

# Approval

| Role | Responsibility |
|------|----------------|
| QA Lead | Review and approve regression execution |
| Solution Architect | Validate system integrity after changes |
| AI/ML Lead | Approve AI model regression results |
| Security Lead | Validate security regression testing |
| DevOps Lead | Verify deployment stability |
| Product Owner | Confirm business functionality |
| Project Manager | Final release approval |

---

# Document Control

| Attribute | Value |
|-----------|-------|
| Document Owner | Quality Assurance Team |
| Repository | 06_Testing/Test_Cases |
| Review Frequency | Every Release |
| Classification | Internal – Confidential |
| Version | 1.0 |
| Status | Approved |

---

# End of Document
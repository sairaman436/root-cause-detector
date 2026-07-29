# Automated Regression Pack

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Quality Assurance & Test Automation Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Automated Regression Test Specification  

---

# Automated Regression Pack

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Automated Regression Pack |
| Domain | Automated Quality Assurance |
| Version | 1.0 |
| Status | Approved |
| Owner | Automation QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document details the test scenarios, automated script references, execution triggers, and pass/fail thresholds comprising the automated regression test pack for the AI Rural Root Cause Discovery System.

---

# Scope

The automated regression pack executes on every GitHub Actions pull request to `main` and release candidate tag. It automates 185 test cases across 5 functional areas:

1. **Authentication & Authorization (25 Test Cases)**
2. **Survey Ingestion & Form Validation (45 Test Cases)**
3. **AI Inference & Recommendation Output (35 Test Cases)**
4. **Analytics & Report Generation (40 Test Cases)**
5. **System Admin & Configuration (40 Test Cases)**

---

# Automated Suite Architecture & Tech Stack

```text
+-------------------------------------------------------------------+
|                  GitHub Actions Trigger (PR / Tag)                |
+---------------------------------+---------------------------------+
                                  |
         +------------------------+------------------------+
         |                                                 |
         ▼                                                 ▼
+----------------------------------+     +----------------------------------+
| API Regression Suite (PyTest)    |     | E2E UI Suite (Playwright / TS)   |
| - Endpoint Schema Validation     |     | - Field Officer Survey Flow      |
| - Security & JWT Token Verification|   | - Admin Dashboard Visualization  |
+----------------------------------+     +----------------------------------+
         |                                                 |
         +------------------------+------------------------+
                                  |
                                  ▼
+-------------------------------------------------------------------+
|               Allure Reports & Slack Notification                 |
+-------------------------------------------------------------------+
```

---

# Key Automated Test Scenarios

### REG-001: End-to-End Survey Submission & Storage Verification
- **Framework**: Playwright / TypeScript
- **Script Path**: `e2e/tests/survey/submission.spec.ts`
- **Execution Flow**:
  1. Log in as Field Officer (`field_user_01`).
  2. Navigate to `/surveys/new` and populate all mandatory fields (Village, PH level, GPS coordinates).
  3. Attach sample ground photograph (`test_water_pipe.jpg`).
  4. Submit form and capture Toast notification message.
- **Assertion**: Survey appears in Grid View with status `PENDING_ANALYSIS`; database record verified in PostgreSQL.

### REG-002: Role-Based Access Control (RBAC) Boundary Enforcement
- **Framework**: PyTest / Requests
- **Script Path**: `api/tests/security/test_rbac_matrix.py`
- **Execution Flow**: Attempt accessing `/api/v1/admin/users` using a Field Officer JWT token.
- **Assertion**: API returns HTTP `403 Forbidden` with body `{ "code": "ACCESS_DENIED" }`.

### REG-003: AI Recommendation Schema Consistency Check
- **Framework**: PyTest / JSONSchema
- **Script Path**: `api/tests/ai/test_recommendation_contract.py`
- **Execution Flow**: Query `/api/v1/recommendations/srv-90823-abc`.
- **Assertion**: Validate response matches strict JSON Schema draft-07. Required keys `recommendation_id`, `priority_rank`, `cost_estimate`, and `action_plan` must exist.

---

# CI/CD Execution Command

```bash
# Execute API & E2E Regression Pack concurrently
npm run test:e2e:headless && pytest tests/regression/ --junitxml=reports/regression_results.xml
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| QA Lead | David Miller | 2026-07-28 |
| Automation Lead | Karen Davis | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Automated Regression Pack | QA Team |

---

# End of Document

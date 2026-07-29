# 🎭 Use Cases

Version: 1.0

Project: AI Rural Root Cause Discovery System

Status:
Approved

---

# Purpose

This document describes how stakeholders interact with the AI-Powered Rural Root Cause Discovery & Evidence-Based Decision Support System to achieve specific objectives.

Each use case defines an interaction between an actor and the system that results in measurable business value.

---

# Primary Actors

- Field Officer
- Government Official
- District Administrator
- System Administrator

---

# UC-001 — User Login

## Primary Actor

Authorized User

## Goal

Securely access the system.

## Preconditions

- User account exists.
- User credentials are valid.

## Main Flow

1. User opens login page.
2. User enters credentials.
3. System validates credentials.
4. System determines user role.
5. Dashboard is displayed.

## Alternate Flow

- Invalid credentials → Error message displayed.
- Disabled account → Access denied.

## Postconditions

Authenticated session is established.

---

# UC-002 — Create Survey

## Primary Actor

Field Officer

## Goal

Record a rural field survey.

## Preconditions

- User is authenticated.
- Survey form is available.

## Main Flow

1. Open survey form.
2. Enter survey details.
3. Capture GPS location.
4. Upload supporting photographs.
5. Submit survey.
6. System validates information.
7. Survey is stored.

## Alternate Flow

- Missing mandatory fields.
- GPS unavailable.
- Image upload failure.

## Postconditions

Survey becomes available for analysis.

---

# UC-003 — Manage Complaints

## Primary Actor

Field Officer

## Goal

Record community complaints.

## Preconditions

Survey exists.

## Main Flow

1. Select survey.
2. Add complaint.
3. Categorize complaint.
4. Save complaint.

## Postconditions

Complaint linked with survey.

---

# UC-004 — AI Analysis

## Primary Actor

Government Official

## Goal

Obtain AI-supported analysis.

## Preconditions

Sufficient survey evidence exists.

## Main Flow

1. Select district or village.
2. Request analysis.
3. System aggregates evidence.
4. AI identifies recurring issues.
5. AI determines probable root causes.
6. AI generates explainable recommendations.
7. Results displayed.

## Alternate Flow

- Insufficient evidence.
- AI confidence too low.
- Processing error.

## Postconditions

Evidence-backed insights are available.

---

# UC-005 — Review Dashboard

## Primary Actor

Government Official

## Goal

Review analytical summaries.

## Main Flow

1. Open dashboard.
2. View statistics.
3. Review issue trends.
4. Examine AI recommendations.
5. Open supporting evidence.

## Postconditions

Decision-maker gains situational awareness.

---

# UC-006 — Generate Reports

## Primary Actor

District Administrator

## Goal

Produce analytical reports.

## Main Flow

1. Select reporting period.
2. Choose filters.
3. Generate report.
4. Export report.

## Postconditions

Report available for distribution.

---

# UC-007 — Manage Users

## Primary Actor

System Administrator

## Goal

Maintain user accounts.

## Main Flow

1. View users.
2. Add user.
3. Edit user.
4. Disable user.
5. Assign roles.

## Postconditions

User information updated.

---

# UC-008 — Review Audit Logs

## Primary Actor

System Administrator

## Goal

Monitor important system events.

## Main Flow

1. Open audit log.
2. Apply filters.
3. Review activities.
4. Export logs if necessary.

## Postconditions

Administrative activities verified.

---

# Use Case Relationships

| Use Case | Actor |
|-----------|-----------------------|
| UC-001 | All Users |
| UC-002 | Field Officer |
| UC-003 | Field Officer |
| UC-004 | Government Official |
| UC-005 | Government Official |
| UC-006 | District Administrator |
| UC-007 | System Administrator |
| UC-008 | System Administrator |

---

# Traceability

Each use case traces to:

- Functional Requirements
- Business Rules
- User Stories
- Acceptance Criteria
- Test Cases

---

# Success Criteria

Use cases are considered complete when:

- Every stakeholder interaction is documented.
- Each workflow can be implemented by the development team.
- Every use case maps to functional requirements.
- All primary business processes are covered.

---

# Guiding Principle

Every interaction between a user and the system should produce clear business value while maintaining transparency, security, and evidence-based decision support.
# Defect_Report_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Quality Assurance Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** Defect Report

---

# Defect Report

---

# Document Information

| Field | Value |
|--------|--------|
| Defect ID | DEF-XXX-001 |
| Related Test Case ID | TC-XXX-001 |
| Related Requirement ID | FR-001 |
| Module | |
| Feature | |
| Release Version | |
| Build Number | |
| Environment | Development / QA / UAT / Staging / Production |
| Reported By | |
| Reported Date | YYYY-MM-DD |
| Assigned To | |
| Current Status | New |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Version | QA Team |

---

# Defect Summary

Provide a concise description of the issue.

Example:

> Survey submission fails when optional demographic information is omitted, resulting in an HTTP 500 Internal Server Error.

---

# Business Impact

Describe how the defect affects business operations.

Example:

- Prevents survey completion
- Blocks AI prediction generation
- Delays government reporting
- Causes incorrect recommendations
- Impacts production users

---

# Defect Classification

| Field | Value |
|--------|--------|
| Category | Functional / UI / API / Database / Security / Performance / AI Model / Infrastructure |
| Severity | Critical / High / Medium / Low |
| Priority | P1 / P2 / P3 / P4 |
| Reproducibility | Always / Intermittent / Rare |
| Customer Impact | Yes / No |
| Production Issue | Yes / No |
| Security Related | Yes / No |
| Regression | Yes / No |

---

# Affected Components

List all impacted modules.

| Component | Impact |
|------------|--------|
| Authentication | |
| Survey Management | |
| AI Inference | |
| Recommendation Engine | |
| Reporting | |
| Notification | |
| Database | |
| API Gateway | |

---

# Preconditions

State the required conditions before reproducing the defect.

Example:

- User account exists
- User authenticated
- Required permissions assigned
- Survey available
- AI service operational

---

# Steps to Reproduce

| Step | Action |
|------|--------|
| 1 | |
| 2 | |
| 3 | |
| 4 | |
| 5 | |

---

# Expected Result

Describe the expected system behavior.

Example:

> Survey should be submitted successfully, AI processing should begin, and a success notification should be displayed.

---

# Actual Result

Describe the observed behavior.

Example:

> System returns HTTP 500, survey is not saved, and no error message is displayed to the user.

---

# Evidence

Attach supporting evidence.

- Screenshots
- Screen recordings
- API responses
- Browser console logs
- Application logs
- Database logs
- Monitoring alerts
- AI inference logs

Evidence Location:

```text
/Evidence/
    DEF-XXX-001/
```

---

# Environment Details

| Item | Value |
|------|-------|
| Environment | QA |
| Browser | Chrome Latest |
| Operating System | |
| Database Version | |
| Kubernetes Version | |
| API Version | |
| AI Model Version | |
| Build Version | |

---

# Frequency of Occurrence

Select one.

- Always
- Frequently
- Occasionally
- Rarely
- Unable to Reproduce

---

# Root Cause Analysis

(To be completed after investigation.)

Include:

- Root cause summary
- Affected component
- Technical explanation
- Business explanation

---

# Impact Assessment

Assess the impact.

| Area | Impact |
|------|--------|
| Business Workflow | |
| Users | |
| Data Integrity | |
| Security | |
| AI Predictions | |
| Performance | |
| Compliance | |

---

# Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Production outage | High | Immediate hotfix |
| Data corruption | High | Database recovery validation |
| Incorrect AI prediction | High | Model validation |
| Security exposure | Critical | Emergency security patch |

---

# Defect Lifecycle

```text
New

↓

Assigned

↓

In Progress

↓

Resolved

↓

Ready for QA

↓

Retest

↓

Closed

or

Reopened
```

---

# Resolution Details

(To be completed by Development Team.)

| Field | Value |
|--------|--------|
| Resolution Type | Code Fix / Configuration / Data Correction / Infrastructure / AI Model Update |
| Commit ID | |
| Pull Request | |
| Deployment Version | |
| Resolution Date | |
| Developer | |

---

# Retest Information

| Field | Value |
|--------|--------|
| Retested By | |
| Retest Date | |
| Retest Result | Pass / Fail |
| Regression Tested | Yes / No |

---

# Verification Checklist

| Validation | Status |
|------------|--------|
| Functional Behavior Verified | ☐ |
| Business Rules Verified | ☐ |
| API Verified | ☐ |
| Database Verified | ☐ |
| Security Verified | ☐ |
| Performance Verified | ☐ |
| AI Prediction Verified | ☐ |
| Audit Logs Verified | ☐ |

---

# Closure Criteria

The defect may be closed only when:

- Fix implemented
- QA verification completed
- Regression testing passed
- Documentation updated
- Required approvals received
- No remaining Critical impact

---

# Traceability

| Artifact | Reference |
|-----------|-----------|
| Requirement ID | |
| User Story | |
| Test Plan | |
| Test Scenario | |
| Test Case | |
| Design Document | |
| Release Version | |

---

# Related Defects

| Defect ID | Relationship |
|------------|-------------|
| DEF-001 | Duplicate |
| DEF-015 | Related |
| DEF-028 | Parent |

---

# Lessons Learned

Document findings.

Example:

- Missing validation
- Inadequate unit testing
- Requirement ambiguity
- Environment inconsistency
- AI model edge case

---

# Recommendations

Examples:

- Improve automated regression testing
- Strengthen API validation
- Expand AI test coverage
- Enhance monitoring alerts
- Update coding standards

---

# Approvals

| Role | Name | Signature | Date |
|------|------|-----------|------|
| QA Engineer | | | |
| Development Lead | | | |
| QA Lead | | | |
| Release Manager | | | |

---

# Appendices

## Appendix A – Screenshots

---

## Appendix B – API Logs

---

## Appendix C – Server Logs

---

## Appendix D – Database Validation

---

## Appendix E – Root Cause Analysis

---

## Appendix F – Related Test Cases

---

## Appendix G – Deployment Evidence

---

**End of Template**
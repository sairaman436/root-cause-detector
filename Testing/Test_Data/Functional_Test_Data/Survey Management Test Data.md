# Survey Management Test Data

**Document ID:** TD-FUNC-SURVEY-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** Survey Management Test Data  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** Quality Assurance Team  
**Reviewed By:** Business Analyst, Data Engineering Team  
**Approved By:** Project Manager

---

# Purpose

This document defines standardized datasets used to validate all Survey Management functionality within the AI Rural Root Cause Discovery System.

The datasets support verification of survey creation, assignment, completion, validation, submission, approval, archival, AI input preparation, reporting, and integration workflows.

These datasets are intended for Functional, Integration, System, Regression, API, Performance, and AI Validation testing.

---

# Scope

The datasets support validation of:

- Survey Creation
- Survey Update
- Survey Assignment
- Survey Completion
- Survey Submission
- Survey Approval
- Survey Rejection
- Survey Archival
- Survey Search
- Survey Filters
- Survey Status Management
- Bulk Survey Operations
- AI Feature Extraction
- Reporting

---

# Dataset Summary

| Dataset ID | Description | Status |
|------------|-------------|--------|
| SUR-001 | Valid Surveys | Approved |
| SUR-002 | Invalid Surveys | Approved |
| SUR-003 | Draft Surveys | Approved |
| SUR-004 | Submitted Surveys | Approved |
| SUR-005 | Approved Surveys | Approved |
| SUR-006 | Rejected Surveys | Approved |
| SUR-007 | Archived Surveys | Approved |
| SUR-008 | Boundary Test Data | Approved |
| SUR-009 | Bulk Survey Dataset | Approved |
| SUR-010 | AI Input Dataset | Approved |

---

# Valid Survey Dataset

| Survey ID | Type | Status | Expected Result |
|------------|------|---------|----------------|
| SUR0001 | Agriculture | Draft | Editable |
| SUR0002 | Healthcare | Submitted | Processing |
| SUR0003 | Education | Approved | Read Only |
| SUR0004 | Water Supply | Completed | Awaiting Approval |
| SUR0005 | Employment | Archived | Historical Access |

---

# Survey Creation Dataset

| Scenario | Input | Expected Result |
|----------|-------|-----------------|
| Complete Survey | Valid Data | Created Successfully |
| Missing Title | Blank Title | Validation Error |
| Missing Category | Blank Category | Validation Error |
| Missing Questions | Empty Questionnaire | Validation Error |
| Invalid Dates | End < Start | Validation Error |

---

# Survey Update Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Update Draft Survey | Success |
| Update Published Survey | Restricted |
| Update Archived Survey | Not Allowed |
| Modify Survey Description | Success |
| Add New Question | Success |

---

# Survey Assignment Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Assign Survey to Officer | Success |
| Assign Multiple Officers | Success |
| Reassign Survey | Success |
| Assign Invalid User | Validation Error |
| Duplicate Assignment | Prevented |

---

# Survey Completion Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Complete All Questions | Success |
| Save Draft | Success |
| Resume Draft | Success |
| Mandatory Question Missing | Validation Error |
| Invalid Response Type | Validation Error |

---

# Survey Submission Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Submit Completed Survey | Success |
| Submit Incomplete Survey | Validation Error |
| Duplicate Submission | Prevented |
| Offline Submission Sync | Success |
| Expired Survey Submission | Rejected |

---

# Survey Approval Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Approve Survey | Approved |
| Reject Survey | Rejected |
| Reopen Survey | Returned to Draft |
| Approve Invalid Survey | Rejected |
| Approve Archived Survey | Not Allowed |

---

# Survey Status Dataset

| Status | Expected Result |
|---------|-----------------|
| Draft | Editable |
| Assigned | Available for Completion |
| In Progress | Editable |
| Submitted | Read Only |
| Approved | Available for Reporting |
| Rejected | Requires Update |
| Archived | Historical Access Only |

---

# Survey Search Dataset

| Search Criteria | Expected Result |
|-----------------|-----------------|
| Survey ID | Matching Survey |
| Survey Name | Matching Results |
| Village | Filtered Results |
| District | Filtered Results |
| Status | Matching Surveys |
| Category | Matching Surveys |
| Assigned Officer | Matching Surveys |

---

# Survey Filter Dataset

Supported filters:

- Status
- District
- Village
- Category
- Assigned Officer
- Submission Date
- Approval Status
- Survey Type

Expected Result:

Correct surveys returned according to selected criteria.

---

# Bulk Survey Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Import 100 Surveys | Success |
| Import Duplicate Surveys | Validation Error |
| Import Invalid Records | Failed Records Logged |
| Bulk Assignment | Success |
| Bulk Archive | Success |

---

# Boundary Value Dataset

| Field | Test Value | Expected Result |
|-------|------------|-----------------|
| Survey Title | Minimum Length | Accepted |
| Survey Title | Maximum Length | Accepted |
| Survey Title | Maximum +1 | Rejected |
| Questions | Minimum Allowed | Accepted |
| Questions | Maximum Allowed | Accepted |
| Description | Maximum Length | Accepted |

---

# Invalid Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Missing Survey Title | Validation Error |
| Invalid Category | Validation Error |
| Invalid Village Code | Validation Error |
| Invalid Date Range | Validation Error |
| Unsupported Question Type | Validation Error |

---

# Duplicate Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Duplicate Survey ID | Rejected |
| Duplicate Survey Name | Warning |
| Duplicate Assignment | Prevented |
| Duplicate Submission | Prevented |

---

# AI Input Dataset

The following survey fields shall be available for AI processing:

| Feature | Status |
|----------|--------|
| Household Size | Available |
| Water Availability | Available |
| Income Level | Available |
| Agricultural Output | Available |
| Healthcare Access | Available |
| Education Level | Available |
| Infrastructure Availability | Available |
| Geographic Location | Available |
| Seasonal Information | Available |
| Historical Survey Data | Available |

---

# Reporting Dataset

Supported reporting scenarios:

- Survey Summary
- District-wise Reports
- Village-wise Reports
- Category Reports
- Officer Productivity
- Completion Rates
- Approval Statistics
- AI Prediction Summary

---

# API Dataset

| Endpoint | Input | Expected Result |
|----------|-------|-----------------|
| POST /surveys | Valid Survey | HTTP 201 |
| POST /surveys | Invalid Survey | HTTP 400 |
| GET /surveys | Existing Survey | HTTP 200 |
| PUT /surveys | Valid Update | HTTP 200 |
| DELETE /surveys | Existing Survey | HTTP 204 |

---

# Automation Compatibility

Supported tools:

- Selenium
- Playwright
- Cypress
- Robot Framework
- Postman
- REST Assured
- JMeter

---

# Validation Checklist

Before dataset approval verify:

- Survey uniqueness
- Question integrity
- Mandatory field validation
- Status transitions
- AI feature completeness
- Reporting compatibility
- API compatibility
- Automation readiness

---

# Requirement Traceability

| Requirement | Dataset Coverage |
|-------------|------------------|
| SUR-001 | Survey Creation |
| SUR-002 | Survey Update |
| SUR-003 | Survey Assignment |
| SUR-004 | Survey Completion |
| SUR-005 | Survey Submission |
| SUR-006 | Survey Approval |
| SUR-007 | Survey Search |
| SUR-008 | AI Input Mapping |
| SUR-009 | Reporting |
| SUR-010 | Bulk Operations |

---

# References

- Survey Management Test Cases
- Integration Test Cases
- System Test Cases
- Regression Test Cases
- AI Prediction Test Cases
- Test Data Management Standards
- ISO/IEC 29119
- ISO/IEC 25010

---

# Approval

| Role | Approval |
|------|----------|
| QA Lead | ✔ |
| Business Analyst | ✔ |
| Data Engineering Lead | ✔ |
| Project Manager | ✔ |

---

# Document Control

| Attribute | Value |
|-----------|-------|
| Repository | 06_Testing/Test_Data/Functional_Test_Data |
| Owner | Quality Assurance Team |
| Review Frequency | Every Release |
| Classification | Internal – Confidential |
| Version | 1.0 |
| Status | Approved |

---

# End of Document
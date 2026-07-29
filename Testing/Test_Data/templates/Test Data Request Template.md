# Test Data Request Template

**Template ID:** TD-TPL-AIRRCD-001  
**Project:** AI Rural Root Cause Discovery System  
**Template Version:** 1.0  
**Classification:** Internal – Confidential

---

# Purpose

This template shall be used whenever a project team requires the creation, provisioning, refresh, or modification of test data for any testing activity.

The completed request serves as the official approval document for Data Engineering, QA, Security, and DevOps teams.

---

# Request Information

| Field | Details |
|--------|---------|
| Request ID | TDR-YYYY-XXX |
| Request Date | DD-MM-YYYY |
| Requested By | |
| Department | |
| Email | |
| Contact Number | |
| Project Name | AI Rural Root Cause Discovery System |
| Sprint / Release | |
| Jira / Work Item ID | |
| Priority | ☐ Critical ☐ High ☐ Medium ☐ Low |

---

# Request Type

Select one or more.

| Type | Select |
|------|--------|
| New Dataset | ☐ |
| Dataset Refresh | ☐ |
| Dataset Modification | ☐ |
| Dataset Copy | ☐ |
| Synthetic Dataset Generation | ☐ |
| AI Training Dataset | ☐ |
| AI Validation Dataset | ☐ |
| AI Testing Dataset | ☐ |
| Performance Dataset | ☐ |
| Security Dataset | ☐ |
| Database Seed Data | ☐ |
| Reference Data | ☐ |
| Production Masked Dataset | ☐ |

---

# Testing Phase

| Phase | Select |
|--------|--------|
| Unit Testing | ☐ |
| Integration Testing | ☐ |
| System Testing | ☐ |
| Regression Testing | ☐ |
| User Acceptance Testing | ☐ |
| API Testing | ☐ |
| Security Testing | ☐ |
| Performance Testing | ☐ |
| Disaster Recovery Testing | ☐ |
| AI Model Validation | ☐ |

---

# Target Environment

| Environment | Select |
|-------------|--------|
| Development | ☐ |
| QA | ☐ |
| UAT | ☐ |
| Performance | ☐ |
| Security | ☐ |
| Production Validation | ☐ |

---

# Dataset Information

| Field | Value |
|--------|-------|
| Dataset Name | |
| Dataset Owner | |
| Dataset Version | |
| Business Domain | |
| Functional Module | |
| Purpose | |
| Estimated Record Count | |
| Expected Dataset Size | |
| Required By | |

---

# Data Classification

| Classification | Select |
|----------------|--------|
| Public | ☐ |
| Internal | ☐ |
| Confidential | ☐ |
| Restricted | ☐ |

---

# Dataset Characteristics

Indicate the required characteristics.

| Characteristic | Required |
|----------------|----------|
| Valid Records | ☐ |
| Invalid Records | ☐ |
| Boundary Values | ☐ |
| Empty Values | ☐ |
| Maximum Values | ☐ |
| Minimum Values | ☐ |
| Duplicate Records | ☐ |
| Missing Fields | ☐ |
| Special Characters | ☐ |
| Unicode Data | ☐ |
| Historical Data | ☐ |
| Seasonal Data | ☐ |
| Regional Data | ☐ |

---

# AI Dataset Requirements

Complete only if applicable.

| Field | Details |
|--------|---------|
| AI Model Name | |
| Model Version | |
| Prediction Type | |
| Number of Features | |
| Number of Target Classes | |
| Required Accuracy Benchmark | |
| Benchmark Dataset | |
| Drift Dataset Required | Yes / No |
| Explainability Dataset Required | Yes / No |

---

# Performance Dataset Requirements

| Field | Details |
|--------|---------|
| Expected Concurrent Users | |
| Number of Records | |
| Estimated Database Size | |
| API Requests per Minute | |
| AI Predictions per Minute | |
| Report Volume | |

---

# Security Dataset Requirements

Select applicable scenarios.

- ☐ SQL Injection
- ☐ Cross-Site Scripting (XSS)
- ☐ CSRF
- ☐ Authentication Testing
- ☐ Authorization Testing
- ☐ JWT Validation
- ☐ Expired Tokens
- ☐ Privilege Escalation
- ☐ API Fuzzing
- ☐ Malformed Payloads
- ☐ Boundary Testing
- ☐ Invalid Input Validation

---

# Production Data Usage

Will production-derived data be used?

- ☐ Yes
- ☐ No

If **Yes**, provide:

| Field | Details |
|--------|---------|
| Source System | |
| Business Owner Approval | |
| Security Approval | |
| Privacy Approval | |
| Data Masking Method | |
| Expected Retention Period | |

---

# Data Masking Requirements

Mask the following fields where applicable.

| Data Element | Required |
|--------------|----------|
| Names | ☐ |
| Phone Numbers | ☐ |
| Email Addresses | ☐ |
| National IDs | ☐ |
| Addresses | ☐ |
| GPS Coordinates | ☐ |
| Device Identifiers | ☐ |
| Financial Information | ☐ |
| Authentication Credentials | ☐ |

---

# Validation Requirements

Select required validations.

- ☐ Schema Validation
- ☐ Referential Integrity
- ☐ Duplicate Detection
- ☐ Missing Value Validation
- ☐ Business Rule Validation
- ☐ AI Feature Validation
- ☐ Data Distribution Validation
- ☐ Performance Validation
- ☐ Security Validation

---

# Acceptance Criteria

The requested dataset shall satisfy:

- Correct schema implementation
- Required record count delivered
- Required classifications applied
- Sensitive information protected
- Validation completed successfully
- Approved by designated reviewers
- Compatible with automation framework
- Traceable through version control

---

# Dependencies

List dependencies required before provisioning.

| Dependency | Status |
|------------|--------|
| Environment Available | |
| Database Available | |
| Storage Available | |
| AI Model Available | |
| External APIs Available | |
| Test Automation Ready | |

---

# Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Incorrect dataset | | |
| Missing records | | |
| Privacy violation | | |
| Performance limitations | | |
| Schema mismatch | | |
| Environment mismatch | | |

---

# Approval Workflow

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Requestor | | | |
| QA Lead | | | |
| Data Engineering Lead | | | |
| AI/ML Lead | | | |
| Security Lead | | | |
| DevOps Lead | | | |
| Project Manager | | | |

---

# Provisioning Details

(To be completed by Data Engineering)

| Field | Value |
|--------|-------|
| Dataset Generated | |
| Dataset Version | |
| Provision Date | |
| Environment | |
| Validation Completed | Yes / No |
| Provisioned By | |

---

# Validation Checklist

| Validation | Status |
|------------|--------|
| Schema Validated | ☐ |
| Referential Integrity Verified | ☐ |
| Business Rules Validated | ☐ |
| Required Record Count Verified | ☐ |
| Duplicate Records Checked | ☐ |
| Missing Values Verified | ☐ |
| Data Masking Verified | ☐ |
| Security Review Completed | ☐ |
| Automation Compatibility Verified | ☐ |
| AI Dataset Validated | ☐ |

---

# Audit Information

| Field | Value |
|--------|-------|
| Request Number | |
| Dataset Identifier | |
| Repository Location | |
| Version | |
| Change Request Reference | |
| Retention Period | |
| Review Date | |
| Archived By | |

---

# Notes

- All requests shall comply with the Test Data Management Standards.
- Restricted datasets require Security and Privacy approval.
- Production-derived datasets shall be fully anonymized before provisioning.
- Every dataset shall be version controlled and traceable.
- Requests missing mandatory approvals shall not be processed.

---

# End of Template
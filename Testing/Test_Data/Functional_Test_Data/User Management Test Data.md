# User Management Test Data

**Document ID:** TD-FUNC-USER-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** User Management Test Data  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** Quality Assurance Team  
**Reviewed By:** Business Analyst, Security Team  
**Approved By:** Project Manager

---

# Purpose

This document defines standardized datasets used to validate the User Management module of the AI Rural Root Cause Discovery System.

The datasets support verification of user lifecycle operations including account creation, updates, role assignments, permissions, activation, deactivation, profile management, bulk operations, and administrative controls.

---

# Scope

This document supports testing for:

- User Registration
- User Creation
- User Profile Updates
- User Deletion
- User Activation
- User Deactivation
- Role Assignment
- Permission Management
- Password Management
- Bulk User Import
- Bulk User Update
- Search & Filtering
- Audit Logging

---

# Dataset Summary

| Dataset ID | Description | Status |
|------------|-------------|--------|
| USER-001 | Valid Users | Approved |
| USER-002 | Invalid Users | Approved |
| USER-003 | Duplicate Users | Approved |
| USER-004 | Role Assignment | Approved |
| USER-005 | Permission Validation | Approved |
| USER-006 | Bulk Operations | Approved |
| USER-007 | Boundary Values | Approved |
| USER-008 | Security Inputs | Approved |
| USER-009 | Search Dataset | Approved |
| USER-010 | Audit Dataset | Approved |

---

# Valid User Dataset

| User ID | Role | Status | Expected Result |
|---------|------|--------|-----------------|
| ADM001 | Administrator | Active | User Available |
| QA001 | QA Engineer | Active | User Available |
| FO001 | Field Officer | Active | User Available |
| SUP001 | Supervisor | Active | User Available |
| DIST001 | District Administrator | Active | User Available |

---

# New User Creation Dataset

| Scenario | Input | Expected Result |
|----------|-------|-----------------|
| Valid User | Complete Information | User Created |
| Missing Email | Email Empty | Validation Error |
| Missing Username | Username Empty | Validation Error |
| Missing Role | No Role | Validation Error |
| Invalid Phone | Invalid Format | Validation Error |

---

# Invalid User Dataset

| Scenario | Input | Expected Result |
|----------|-------|-----------------|
| Invalid Username | Unsupported Characters | Validation Error |
| Invalid Email | abc@test | Validation Error |
| Invalid Phone | Alphabetic Values | Validation Error |
| Missing Mandatory Fields | Blank Values | Validation Error |
| Invalid Role | Unknown Role | Validation Error |

---

# Duplicate User Dataset

| Scenario | Existing Value | Expected Result |
|----------|----------------|-----------------|
| Duplicate Username | admin01 | Duplicate Rejected |
| Duplicate Email | admin@test.com | Duplicate Rejected |
| Duplicate Employee ID | EMP001 | Duplicate Rejected |
| Duplicate Phone | 9876543210 | Duplicate Rejected |

---

# User Profile Update Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Update Name | Success |
| Update Phone | Success |
| Update Address | Success |
| Update Email | Success |
| Update Department | Success |

---

# User Activation Dataset

| Scenario | Initial State | Expected Result |
|----------|---------------|-----------------|
| Activate User | Inactive | Active |
| Reactivate User | Disabled | Active |
| Already Active User | Active | No Change |

---

# User Deactivation Dataset

| Scenario | Initial State | Expected Result |
|----------|---------------|-----------------|
| Disable User | Active | Disabled |
| Suspend User | Active | Suspended |
| Lock User | Active | Locked |

---

# Role Assignment Dataset

| User | Assigned Role | Expected Result |
|------|---------------|-----------------|
| QA001 | QA Lead | Updated |
| FO001 | Supervisor | Updated |
| DIST001 | Administrator | Updated |
| ADM001 | Administrator | No Change |

---

# Permission Dataset

| Role | Permission | Expected Result |
|------|------------|-----------------|
| Administrator | Full Access | Granted |
| Supervisor | Reports | Granted |
| QA Engineer | Test Management | Granted |
| Field Officer | Survey Submission | Granted |
| Guest | Administration | Denied |

---

# Password Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Valid Password | Accepted |
| Weak Password | Rejected |
| Expired Password | Reset Required |
| Reused Password | Rejected |
| Temporary Password | Change Required |

---

# Search Dataset

| Search Criteria | Expected Result |
|-----------------|-----------------|
| Username | Matching User |
| Email | Matching User |
| Department | Filtered Users |
| Role | Matching Roles |
| Status | Active/Inactive Users |

---

# Bulk Import Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Import 10 Users | Success |
| Import 100 Users | Success |
| Import Duplicate Records | Duplicate Errors |
| Import Invalid Records | Validation Errors |
| Mixed Dataset | Partial Success |

---

# Bulk Update Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Update Roles | Success |
| Update Departments | Success |
| Activate Users | Success |
| Deactivate Users | Success |
| Invalid User IDs | Failed Records Logged |

---

# Boundary Value Dataset

| Input | Test Value | Expected Result |
|-------|------------|-----------------|
| Username Length | Minimum | Accepted |
| Username Length | Maximum | Accepted |
| Username Length | Maximum +1 | Rejected |
| Email Length | Maximum | Accepted |
| Department Name | Maximum | Accepted |
| Phone Number | Exact Length | Accepted |

---

# Unicode Dataset

| Input | Expected Result |
|-------|-----------------|
| Unicode Name | Accepted |
| Regional Language Name | Accepted |
| Emoji in Name | Validation Applied |
| Mixed Language | Accepted |

---

# Special Character Dataset

| Input | Expected Result |
|-------|-----------------|
| Apostrophe | Accepted |
| Hyphen | Accepted |
| HTML Tags | Escaped |
| SQL Characters | Sanitized |
| Script Tags | Rejected |

---

# Security Validation Dataset

| Attack | Example | Expected Result |
|--------|---------|-----------------|
| SQL Injection | `' OR '1'='1` | Rejected |
| XSS | `<script>` | Sanitized |
| Command Injection | `&& rm -rf` | Rejected |
| LDAP Injection | `*)(uid=*))` | Rejected |
| JSON Injection | Malformed Payload | Validation Error |

---

# API Dataset

| Endpoint | Input | Expected Result |
|----------|-------|-----------------|
| POST /users | Valid User | HTTP 201 |
| POST /users | Invalid User | HTTP 400 |
| GET /users | Existing User | HTTP 200 |
| PUT /users | Update User | HTTP 200 |
| DELETE /users | Existing User | HTTP 204 |

---

# Audit Dataset

The following actions shall generate audit records:

- User Creation
- User Update
- User Deletion
- Role Assignment
- Permission Changes
- Password Reset
- Account Lock
- Account Unlock
- User Activation
- User Deactivation

---

# Automation Compatibility

Supported automation frameworks:

- Selenium
- Playwright
- Cypress
- Robot Framework
- REST Assured
- Postman
- JMeter

---

# Validation Checklist

Before dataset approval verify:

- User uniqueness
- Email uniqueness
- Role mappings
- Permission mappings
- Mandatory fields
- Business rules
- Automation compatibility
- API compatibility

---

# Requirement Traceability

| Requirement | Dataset |
|-------------|---------|
| USER-001 | User Creation |
| USER-002 | User Update |
| USER-003 | User Activation |
| USER-004 | User Deactivation |
| USER-005 | Role Assignment |
| USER-006 | Permission Management |
| USER-007 | Bulk Operations |
| USER-008 | Audit Logging |

---

# References

- User Management Test Cases
- System Test Cases
- Regression Test Cases
- Security Test Cases
- API Test Cases
- Test Data Management Standards
- ISO/IEC 29119
- ISO/IEC 27001
- OWASP ASVS

---

# Approval

| Role | Approval |
|------|----------|
| QA Lead | ✔ |
| Security Lead | ✔ |
| Business Analyst | ✔ |
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
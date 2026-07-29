# Authentication Test Data

**Document ID:** TD-FUNC-AUTH-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** Authentication Test Data  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** Quality Assurance Team  
**Reviewed By:** Security Team, QA Lead  
**Approved By:** Project Manager

---

# Purpose

This document defines standardized authentication datasets used to validate authentication, authorization, identity management, session handling, password policies, account security, and multi-factor authentication (MFA) within the AI Rural Root Cause Discovery System.

These datasets support functional, integration, system, regression, security, API, and automation testing.

---

# Scope

Authentication datasets support validation of:

- User Login
- User Logout
- Password Policy
- Password Reset
- Account Lockout
- Account Activation
- Account Deactivation
- Session Management
- Token Management
- JWT Validation
- Refresh Tokens
- MFA Authentication
- Authorization
- API Authentication

---

# Dataset Summary

| Dataset ID | Description | Status |
|------------|-------------|--------|
| AUTH-001 | Valid Users | Approved |
| AUTH-002 | Invalid Users | Approved |
| AUTH-003 | Locked Accounts | Approved |
| AUTH-004 | Disabled Accounts | Approved |
| AUTH-005 | Password Policy | Approved |
| AUTH-006 | MFA Accounts | Approved |
| AUTH-007 | Session Tokens | Approved |
| AUTH-008 | JWT Tokens | Approved |
| AUTH-009 | Boundary Inputs | Approved |
| AUTH-010 | Security Payloads | Approved |

---

# Valid Login Dataset

| Test ID | Username | Password | Expected Result |
|----------|----------|----------|-----------------|
| AUTH-VALID-001 | admin01 | Valid@123 | Login Successful |
| AUTH-VALID-002 | qa_user01 | Valid@123 | Login Successful |
| AUTH-VALID-003 | field_officer01 | Valid@123 | Login Successful |
| AUTH-VALID-004 | supervisor01 | Valid@123 | Login Successful |
| AUTH-VALID-005 | district_admin01 | Valid@123 | Login Successful |

---

# Invalid Login Dataset

| Test ID | Username | Password | Expected Result |
|----------|----------|----------|-----------------|
| AUTH-INVALID-001 | invalid_user | Valid@123 | Login Failed |
| AUTH-INVALID-002 | admin01 | WrongPassword | Login Failed |
| AUTH-INVALID-003 | admin01 | blank | Validation Error |
| AUTH-INVALID-004 | blank | Valid@123 | Validation Error |
| AUTH-INVALID-005 | blank | blank | Validation Error |

---

# Locked Account Dataset

| User | Status | Expected Result |
|------|--------|-----------------|
| locked_user01 | Locked | Access Denied |
| locked_admin01 | Locked | Access Denied |
| locked_field01 | Locked | Access Denied |

---

# Disabled Account Dataset

| User | Status | Expected Result |
|------|--------|-----------------|
| disabled_user01 | Disabled | Login Rejected |
| inactive_user01 | Disabled | Login Rejected |

---

# Password Policy Dataset

| Scenario | Password | Expected Result |
|----------|----------|-----------------|
| Valid Password | Secure@123 | Accepted |
| Too Short | Ab1! | Rejected |
| Missing Uppercase | secure@123 | Rejected |
| Missing Lowercase | SECURE@123 | Rejected |
| Missing Number | Secure@ABC | Rejected |
| Missing Special Character | Secure123 | Rejected |
| Maximum Length | 64 Characters | Accepted |
| Exceeds Maximum | 65 Characters | Rejected |

---

# Password Reset Dataset

| Test Case | Expected Result |
|-----------|-----------------|
| Valid Reset Link | Password Reset Successful |
| Expired Reset Link | Link Expired |
| Invalid Token | Reset Denied |
| Already Used Token | Reset Denied |
| Reset with Weak Password | Validation Failed |

---

# Multi-Factor Authentication Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Valid OTP | Authentication Successful |
| Incorrect OTP | Authentication Failed |
| Expired OTP | Authentication Failed |
| Reused OTP | Authentication Failed |
| Missing OTP | Validation Error |

---

# Session Management Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Valid Session | Access Granted |
| Expired Session | Login Required |
| Invalid Session ID | Access Denied |
| Concurrent Session | Allowed per Policy |
| Revoked Session | Access Denied |

---

# JWT Token Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Valid JWT | Authorized |
| Expired JWT | Unauthorized |
| Invalid Signature | Unauthorized |
| Modified Payload | Unauthorized |
| Missing JWT | Unauthorized |

---

# Refresh Token Dataset

| Scenario | Expected Result |
|----------|-----------------|
| Valid Refresh Token | New Access Token |
| Expired Refresh Token | Authentication Required |
| Revoked Refresh Token | Authentication Failed |
| Invalid Refresh Token | Authentication Failed |

---

# Authorization Dataset

| Role | Resource | Expected Result |
|------|----------|-----------------|
| Administrator | User Management | Allowed |
| QA Engineer | Test Reports | Allowed |
| Field Officer | Assigned Surveys | Allowed |
| Field Officer | User Management | Denied |
| Guest | Dashboard | Denied |

---

# Boundary Value Dataset

| Input | Test Value | Expected Result |
|-------|------------|-----------------|
| Username Length | 1 Character | Validated |
| Username Length | Maximum Allowed | Accepted |
| Username Length | Maximum +1 | Rejected |
| Password Length | Minimum Allowed | Accepted |
| Password Length | Maximum Allowed | Accepted |
| Password Length | Maximum +1 | Rejected |

---

# Unicode Dataset

| Test Value | Expected Result |
|------------|-----------------|
| Unicode Username | Validation Applied |
| Unicode Password | Supported per Policy |
| Emoji Characters | Validation Applied |
| Mixed Language Input | Validation Applied |

---

# Special Character Dataset

| Input | Expected Result |
|-------|-----------------|
| !@#$%^&* | Proper Validation |
| SQL Reserved Characters | Sanitized |
| HTML Tags | Escaped |
| JavaScript Payload | Rejected |

---

# Security Attack Dataset

| Attack Type | Example | Expected Result |
|-------------|---------|-----------------|
| SQL Injection | `' OR '1'='1` | Rejected |
| XSS | `<script>alert()</script>` | Sanitized |
| Command Injection | `; rm -rf /` | Rejected |
| LDAP Injection | `*)(uid=*))(|(uid=*` | Rejected |
| JWT Manipulation | Modified Token | Unauthorized |

---

# API Authentication Dataset

| Endpoint | Authentication | Expected Result |
|----------|----------------|-----------------|
| /login | Valid Credentials | HTTP 200 |
| /login | Invalid Credentials | HTTP 401 |
| /users | Missing JWT | HTTP 401 |
| /users | Expired JWT | HTTP 401 |
| /users | Valid JWT | HTTP 200 |

---

# Automation Support

These datasets are compatible with:

- Selenium
- Cypress
- Playwright
- Postman
- REST Assured
- JMeter
- Robot Framework

---

# Validation Checklist

Before use, verify:

- Username uniqueness
- Password policy compliance
- Token validity
- Session integrity
- MFA configuration
- Role mappings
- API authentication behavior
- Automation compatibility

---

# Requirement Traceability

| Requirement | Dataset Coverage |
|-------------|------------------|
| AUTH-001 | Valid Login |
| AUTH-002 | Invalid Login |
| AUTH-003 | Password Policy |
| AUTH-004 | Session Management |
| AUTH-005 | JWT Validation |
| AUTH-006 | MFA |
| AUTH-007 | Authorization |
| AUTH-008 | Security Validation |

---

# References

- Authentication Test Cases
- Security Test Cases
- API Test Cases
- Regression Test Cases
- Test Data Management Standards
- OWASP ASVS
- OWASP Top 10
- ISO/IEC 29119
- ISO/IEC 27001

---

# Approval

| Role | Approval |
|------|----------|
| QA Lead | ✔ |
| Security Lead | ✔ |
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
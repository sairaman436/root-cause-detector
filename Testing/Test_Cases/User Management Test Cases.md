# User Management Test Cases

**Document ID:** TC-USER-AIRRCD-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** User Management  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** QA Team  
**Reviewed By:** QA Lead, Product Owner  
**Approved By:** Project Manager

---

# Revision History

| Version | Date | Author | Description |
|----------|------|--------|-------------|
| 0.1 | DD-MM-YYYY | QA Team | Initial Draft |
| 0.5 | DD-MM-YYYY | Product Team | Functional Review |
| 1.0 | DD-MM-YYYY | QA Lead | Approved |

---

# Purpose

This document defines the detailed test cases required to validate all user management functionality within the AI Rural Root Cause Discovery System.

User Management includes user lifecycle operations, profile administration, role assignment, permissions, activation, deactivation, search capabilities, and administrative controls.

---

# Scope

This document validates:

- User Creation
- User Update
- User Deletion
- User Activation
- User Deactivation
- User Search
- User Profile
- Role Assignment
- Permission Validation
- User Status Management
- Bulk Operations
- Audit Logging

---

# Requirement Traceability

| Requirement ID | Feature |
|----------------|---------|
| USER-001 | User Creation |
| USER-002 | User Update |
| USER-003 | User Deletion |
| USER-004 | User Search |
| USER-005 | User Profile |
| USER-006 | Role Assignment |
| USER-007 | Permission Management |
| USER-008 | User Activation |
| USER-009 | User Deactivation |
| USER-010 | Audit Logging |

---

# Test Case Summary

| Category | Planned |
|----------|---------|
| Positive Tests | 30 |
| Negative Tests | 20 |
| Validation Tests | 15 |
| Security Tests | 15 |
| Boundary Tests | 10 |
| Total | 90 |

---

# Test Cases

---

## TC-USER-CREATE-001

### Title

Create User with Valid Information

### Requirement

USER-001

### Priority

Critical

### Severity

Critical

### Preconditions

- Administrator authenticated.
- User Management permission assigned.

### Test Data

| Field | Value |
|--------|-------|
| Name | John Smith |
| Email | john.smith@example.com |
| Role | District Officer |

### Steps

1. Navigate to User Management.
2. Select **Create User**.
3. Enter valid user details.
4. Assign role.
5. Save.

### Expected Result

- User created successfully.
- Unique User ID generated.
- Default account status set to Active.
- Audit log recorded.
- Success notification displayed.

---

## TC-USER-CREATE-002

### Title

Create User with Duplicate Email Address

### Requirement

USER-001

### Priority

Critical

### Severity

High

### Preconditions

User already exists with the specified email.

### Steps

1. Open Create User.
2. Enter duplicate email.
3. Save.

### Expected Result

- User creation rejected.
- Duplicate email validation displayed.
- Existing record unchanged.

---

## TC-USER-CREATE-003

### Title

Create User with Missing Mandatory Fields

### Requirement

USER-001

### Priority

High

### Severity

Medium

### Steps

1. Leave mandatory fields blank.
2. Save.

### Expected Result

- Validation messages displayed.
- Record not created.
- Required fields highlighted.

---

## TC-USER-CREATE-004

### Title

Create User with Invalid Email Format

### Requirement

USER-001

### Priority

High

### Severity

Medium

### Test Data

```
john.example.com
```

### Steps

1. Enter invalid email.
2. Save.

### Expected Result

- Email validation displayed.
- User not created.

---

## TC-USER-CREATE-005

### Title

Create User Using Maximum Allowed Field Length

### Requirement

USER-001

### Priority

Medium

### Severity

Low

### Steps

1. Enter values at maximum permitted lengths.
2. Save.

### Expected Result

- Record successfully created.
- No truncation occurs.
- Database stores complete values.

---

## TC-USER-CREATE-006

### Title

Create User with SQL Injection Payload

### Requirement

USER-001

### Priority

Critical

### Severity

Critical

### Test Data

```sql
' OR '1'='1
```

### Steps

1. Enter SQL payload in Name field.
2. Save.

### Expected Result

- Input sanitized.
- User not created.
- SQL execution prevented.
- Security event logged.

---

## TC-USER-CREATE-007

### Title

Create User with Cross-Site Scripting Payload

### Requirement

USER-001

### Priority

Critical

### Severity

Critical

### Test Data

```html
<script>alert('XSS')</script>
```

### Steps

1. Enter XSS payload.
2. Save.

### Expected Result

- Payload sanitized.
- Script not executed.
- Validation enforced.
- Security log generated.

---

## TC-USER-UPDATE-001

### Title

Update Existing User Information

### Requirement

USER-002

### Priority

Critical

### Severity

High

### Preconditions

Existing user available.

### Steps

1. Open user profile.
2. Update contact information.
3. Save.

### Expected Result

- Changes saved successfully.
- Updated data visible.
- Audit log recorded.

---

## TC-USER-UPDATE-002

### Title

Update User Email to Existing Email

### Requirement

USER-002

### Priority

High

### Severity

Medium

### Steps

1. Edit user.
2. Enter another user's email.
3. Save.

### Expected Result

- Duplicate validation displayed.
- Update rejected.

---

## TC-USER-UPDATE-003

### Title

Cancel User Update Before Saving

### Requirement

USER-002

### Priority

Low

### Severity

Low

### Steps

1. Modify user details.
2. Click Cancel.

### Expected Result

- Changes discarded.
- Original information retained.

---

## TC-USER-DELETE-001

### Title

Delete Existing User

### Requirement

USER-003

### Priority

Critical

### Severity

High

### Preconditions

User eligible for deletion.

### Steps

1. Select user.
2. Click Delete.
3. Confirm deletion.

### Expected Result

- User deleted or soft-deleted according to policy.
- Related references maintained.
- Audit log generated.

---

## TC-USER-DELETE-002

### Title

Cancel User Deletion

### Requirement

USER-003

### Priority

Medium

### Severity

Low

### Steps

1. Select Delete.
2. Cancel confirmation.

### Expected Result

- User remains unchanged.
- No deletion performed.

---

## TC-USER-DELETE-003

### Title

Delete Non-Existing User

### Requirement

USER-003

### Priority

Medium

### Severity

Medium

### Steps

1. Request deletion using invalid User ID.

### Expected Result

- Appropriate error displayed.
- No system failure occurs.

## TC-USER-SEARCH-001

### Title

Search User by Full Name

### Requirement

USER-004

### Priority

High

### Severity

Medium

### Preconditions

- Multiple user records exist.

### Steps

1. Navigate to User Management.
2. Enter a valid full name in the search field.
3. Execute search.

### Expected Result

- Matching user(s) displayed.
- Search results accurate.
- Response time within acceptable limits.

---

## TC-USER-SEARCH-002

### Title

Search User by Email Address

### Requirement

USER-004

### Priority

High

### Severity

Medium

### Steps

1. Enter a registered email address.
2. Execute search.

### Expected Result

- Correct user returned.
- User details match stored record.

---

## TC-USER-SEARCH-003

### Title

Search Using Partial Name

### Requirement

USER-004

### Priority

Medium

### Severity

Low

### Steps

1. Enter partial name.
2. Execute search.

### Expected Result

- Matching records displayed.
- Partial matching follows configured search rules.

---

## TC-USER-SEARCH-004

### Title

Search for Non-Existing User

### Requirement

USER-004

### Priority

Medium

### Severity

Low

### Steps

1. Search for a user that does not exist.

### Expected Result

- No matching records found.
- Informational message displayed.
- No application errors occur.

---

## TC-USER-SEARCH-005

### Title

Search with Empty Search Criteria

### Requirement

USER-004

### Priority

Low

### Severity

Low

### Steps

1. Leave search field empty.
2. Click Search.

### Expected Result

- System follows configured behavior.
- Either all users displayed or validation shown.

---

## TC-USER-PROFILE-001

### Title

View User Profile

### Requirement

USER-005

### Priority

High

### Severity

Medium

### Preconditions

User exists.

### Steps

1. Open User Management.
2. Select a user.

### Expected Result

- Complete profile displayed.
- Information accurate.
- Sensitive fields masked according to policy.

---

## TC-USER-PROFILE-002

### Title

Update User Profile Picture

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Preconditions

Supported image file available.

### Steps

1. Open profile.
2. Upload image.
3. Save.

### Expected Result

- Profile picture updated.
- Image validated.
- Unsupported formats rejected.

---

## TC-USER-PROFILE-003

### Title

Upload Unsupported Profile Image Format

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Test Data

Unsupported file type (e.g., `.exe`).

### Steps

1. Select unsupported file.
2. Upload.

### Expected Result

- Upload rejected.
- Validation message displayed.
- No file stored.

---

## TC-USER-ACTIVATE-001

### Title

Activate Inactive User

### Requirement

USER-008

### Priority

High

### Severity

Medium

### Preconditions

User account inactive.

### Steps

1. Select inactive user.
2. Click Activate.
3. Confirm.

### Expected Result

- Account status changed to Active.
- User can authenticate.
- Audit log generated.

---

## TC-USER-ACTIVATE-002

### Title

Activate Already Active User

### Requirement

USER-008

### Priority

Low

### Severity

Low

### Steps

1. Select active user.
2. Click Activate.

### Expected Result

- No duplicate activation.
- Appropriate message displayed.

---

## TC-USER-DEACTIVATE-001

### Title

Deactivate Active User

### Requirement

USER-009

### Priority

High

### Severity

High

### Preconditions

Active user account exists.

### Steps

1. Select active user.
2. Click Deactivate.
3. Confirm.

### Expected Result

- User status changed to Inactive.
- Authentication blocked.
- Existing sessions handled according to security policy.
- Audit event generated.

---

## TC-USER-DEACTIVATE-002

### Title

Deactivate Currently Logged-in User

### Requirement

USER-009

### Priority

Critical

### Severity

Critical

### Steps

1. Administrator deactivates an active user.
2. User attempts further actions.

### Expected Result

- Session invalidated according to policy.
- Access immediately restricted.
- Appropriate notification displayed.

---

## TC-USER-ROLE-001

### Title

Assign Valid Role to User

### Requirement

USER-006

### Priority

Critical

### Severity

High

### Preconditions

Administrator authenticated.

### Steps

1. Open user record.
2. Select new role.
3. Save.

### Expected Result

- Role assigned successfully.
- Updated permissions applied.
- Audit log recorded.

---

## TC-USER-ROLE-002

### Title

Assign Invalid Role

### Requirement

USER-006

### Priority

High

### Severity

Medium

### Steps

1. Attempt to assign a non-existent role.

### Expected Result

- Assignment rejected.
- Validation displayed.
- Existing role unchanged.

---

## TC-USER-ROLE-003

### Title

Remove Assigned Role

### Requirement

USER-006

### Priority

High

### Severity

Medium

### Steps

1. Remove assigned role.
2. Save changes.

### Expected Result

- Role removed according to business rules.
- User permissions updated.
- Audit record generated.

---

## TC-USER-PERM-001

### Title

User Access Limited to Assigned Permissions

### Requirement

USER-007

### Priority

Critical

### Severity

Critical

### Preconditions

User assigned limited role.

### Steps

1. Login.
2. Attempt authorized operations.
3. Attempt unauthorized operations.

### Expected Result

- Authorized operations succeed.
- Unauthorized operations denied.
- Security event logged when appropriate.

---

## TC-USER-PERM-002

### Title

Administrator Has Full Administrative Permissions

### Requirement

USER-007

### Priority

Critical

### Severity

High

### Preconditions

Administrator account exists.

### Steps

1. Login as Administrator.
2. Access administrative functions.

### Expected Result

- Administrative operations available.
- All permissions function correctly.
- Access complies with RBAC configuration.

## TC-USER-BULK-001

### Title

Bulk Create Multiple Users

### Requirement

USER-001

### Priority

High

### Severity

High

### Preconditions

- Administrator authenticated.
- Valid bulk upload file available.

### Steps

1. Navigate to Bulk User Management.
2. Upload valid user dataset.
3. Start import.

### Expected Result

- All valid users created.
- Unique User IDs assigned.
- Import summary displayed.
- Audit log generated.

---

## TC-USER-BULK-002

### Title

Bulk Create with Duplicate Users

### Requirement

USER-001

### Priority

High

### Severity

Medium

### Preconditions

Bulk upload contains duplicate email addresses.

### Steps

1. Upload duplicate dataset.
2. Execute import.

### Expected Result

- Duplicate records rejected.
- Valid users processed.
- Error report generated.

---

## TC-USER-BULK-003

### Title

Bulk Activate Users

### Requirement

USER-008

### Priority

Medium

### Severity

Medium

### Steps

1. Select multiple inactive users.
2. Click Bulk Activate.

### Expected Result

- Selected users activated.
- Operation summary displayed.
- Audit log recorded.

---

## TC-USER-BULK-004

### Title

Bulk Deactivate Users

### Requirement

USER-009

### Priority

Medium

### Severity

Medium

### Steps

1. Select multiple active users.
2. Click Bulk Deactivate.

### Expected Result

- Selected accounts deactivated.
- Active sessions handled according to policy.
- Audit records generated.

---

## TC-USER-BULK-005

### Title

Bulk Delete Users

### Requirement

USER-003

### Priority

High

### Severity

High

### Steps

1. Select multiple eligible users.
2. Confirm deletion.

### Expected Result

- Users deleted according to business policy.
- Referential integrity maintained.
- Audit logs created.

---

## TC-USER-IMPORT-001

### Title

Import Users Using Valid CSV File

### Requirement

USER-001

### Priority

High

### Severity

Medium

### Preconditions

CSV template follows approved format.

### Steps

1. Navigate to Import Users.
2. Upload valid CSV.
3. Execute import.

### Expected Result

- Import completed successfully.
- Imported user count displayed.
- Invalid records reported separately.

---

## TC-USER-IMPORT-002

### Title

Import Users Using Invalid File Format

### Requirement

USER-001

### Priority

Medium

### Severity

Low

### Test Data

Unsupported file type (.exe, .zip).

### Steps

1. Upload unsupported file.

### Expected Result

- Import rejected.
- Validation message displayed.
- No processing occurs.

---

## TC-USER-IMPORT-003

### Title

Import File with Missing Mandatory Columns

### Requirement

USER-001

### Priority

Medium

### Severity

Medium

### Steps

1. Upload incomplete CSV.

### Expected Result

- Import validation fails.
- Missing columns identified.
- File rejected.

---

## TC-USER-EXPORT-001

### Title

Export User List

### Requirement

USER-004

### Priority

Medium

### Severity

Low

### Steps

1. Open User Management.
2. Click Export.

### Expected Result

- Export file generated.
- User information accurate.
- Export format matches specification.

---

## TC-USER-EXPORT-002

### Title

Export Filtered User List

### Requirement

USER-004

### Priority

Medium

### Severity

Low

### Steps

1. Apply filters.
2. Export results.

### Expected Result

- Export contains only filtered users.
- Export metadata correct.

---

## TC-USER-AUDIT-001

### Title

User Creation Recorded in Audit Log

### Requirement

USER-010

### Priority

High

### Severity

Medium

### Steps

1. Create user.
2. Review audit logs.

### Expected Result

Audit record contains:

- Timestamp
- Administrator
- User ID
- Operation
- IP Address

---

## TC-USER-AUDIT-002

### Title

User Update Recorded in Audit Log

### Requirement

USER-010

### Priority

Medium

### Severity

Low

### Steps

1. Update user.
2. Review audit logs.

### Expected Result

- Update recorded successfully.
- Previous and updated values tracked where permitted.

---

## TC-USER-AUDIT-003

### Title

User Deletion Recorded in Audit Log

### Requirement

USER-010

### Priority

Medium

### Severity

Low

### Steps

1. Delete user.
2. Review logs.

### Expected Result

- Deletion event recorded.
- User identifier retained for audit purposes.

---

## TC-USER-SEC-001

### Title

Unauthorized User Attempts User Creation

### Requirement

USER-007

### Priority

Critical

### Severity

Critical

### Preconditions

User lacks Create User permission.

### Steps

1. Login using restricted account.
2. Attempt to create user.

### Expected Result

- Operation denied.
- HTTP 403 (or application equivalent) returned.
- Unauthorized attempt logged.

---

## TC-USER-SEC-002

### Title

Direct URL Access to User Administration

### Requirement

USER-007

### Priority

Critical

### Severity

Critical

### Steps

1. Login as standard user.
2. Enter administration URL manually.

### Expected Result

- Access denied.
- User redirected or shown authorization error.
- Security event logged.

---

## TC-USER-SEC-003

### Title

Cross-Site Scripting Validation During User Update

### Requirement

USER-002

### Priority

Critical

### Severity

Critical

### Test Data

```html
<script>alert('XSS')</script>
```

### Steps

1. Edit user profile.
2. Enter XSS payload.
3. Save.

### Expected Result

- Payload sanitized.
- Script never executed.
- Validation enforced.
- Security event recorded.

---

## TC-USER-SEC-004

### Title

SQL Injection Validation During User Search

### Requirement

USER-004

### Priority

Critical

### Severity

Critical

### Test Data

```sql
' OR 1=1 --
```

### Steps

1. Enter SQL payload in search field.
2. Execute search.

### Expected Result

- Input sanitized.
- SQL not executed.
- Search safely rejected or returns no unauthorized results.
- Security event logged.

---

## TC-USER-BOUNDARY-001

### Title

Create User Using Minimum Valid Input Length

### Requirement

USER-001

### Priority

Low

### Severity

Low

### Steps

1. Enter minimum permitted values.
2. Save.

### Expected Result

- User successfully created.
- Validation passes.

---

## TC-USER-BOUNDARY-002

### Title

Create User Exceeding Maximum Field Length

### Requirement

USER-001

### Priority

Medium

### Severity

Low

### Steps

1. Enter values exceeding maximum length.
2. Save.

### Expected Result

- Validation displayed.
- Record rejected.
- Database integrity maintained.

---

## TC-USER-BOUNDARY-003

### Title

Create User Using Unicode Characters

### Requirement

USER-001

### Priority

Medium

### Severity

Low

### Steps

1. Enter supported Unicode characters in user name.
2. Save.

### Expected Result

- Input accepted according to localization policy.
- Data stored correctly.

## TC-USER-NEG-001

### Title

Create User Without Required Role Assignment

### Requirement

USER-006

### Priority

High

### Severity

Medium

### Steps

1. Open Create User.
2. Enter all mandatory information.
3. Do not assign a role.
4. Save.

### Expected Result

- User creation rejected.
- Validation message displayed.
- No user record created.

---

## TC-USER-NEG-002

### Title

Update User with Invalid User ID

### Requirement

USER-002

### Priority

Medium

### Severity

Medium

### Steps

1. Attempt to update a user using an invalid User ID.

### Expected Result

- User not found.
- Update operation rejected.
- Appropriate error displayed.

---

## TC-USER-NEG-003

### Title

Delete Protected System Administrator Account

### Requirement

USER-003

### Priority

Critical

### Severity

Critical

### Preconditions

Protected administrator account exists.

### Steps

1. Select protected administrator account.
2. Attempt deletion.

### Expected Result

- Deletion prevented.
- Appropriate validation displayed.
- Security event logged.

---

## TC-USER-NEG-004

### Title

Assign Multiple Mutually Exclusive Roles

### Requirement

USER-006

### Priority

Medium

### Severity

Medium

### Steps

1. Edit user roles.
2. Assign conflicting roles.
3. Save.

### Expected Result

- Validation prevents conflicting assignments.
- User roles remain unchanged.

---

## TC-USER-NEG-005

### Title

Access Deleted User Profile

### Requirement

USER-003

### Priority

Medium

### Severity

Medium

### Preconditions

User previously deleted.

### Steps

1. Attempt to open deleted user's profile.

### Expected Result

- User not found.
- No sensitive information displayed.

---

## TC-USER-ACCESS-001

### Title

Keyboard Navigation Across User Management Screens

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Navigate User Management using keyboard only.
2. Access forms, tables, and buttons.

### Expected Result

- All interactive controls accessible.
- Logical tab order maintained.
- Keyboard shortcuts function correctly.

---

## TC-USER-ACCESS-002

### Title

Screen Reader Accessibility

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Open User Management using supported screen reader.
2. Navigate forms and tables.

### Expected Result

- Labels announced correctly.
- Table headers properly identified.
- Error messages accessible.
- Controls have accessible names.

---

## TC-USER-ACCESS-003

### Title

Color Contrast Compliance

### Requirement

USER-005

### Priority

Low

### Severity

Low

### Steps

1. Review User Management interface.

### Expected Result

- Interface complies with WCAG 2.1 AA contrast requirements.
- Status indicators remain understandable without color alone.

---

## TC-USER-BROWSER-001

### Title

User Management on Google Chrome

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Execute CRUD operations using Chrome.

### Expected Result

- All functions operate correctly.
- Layout renders properly.

---

## TC-USER-BROWSER-002

### Title

User Management on Microsoft Edge

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Execute major user management workflows using Edge.

### Expected Result

- Feature behavior consistent with supported browsers.

---

## TC-USER-BROWSER-003

### Title

User Management on Mozilla Firefox

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Execute CRUD operations using Firefox.

### Expected Result

- Functionality identical to supported browser baseline.

---

## TC-USER-BROWSER-004

### Title

User Management on Safari

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Execute user management workflows using Safari.

### Expected Result

- Interface renders correctly.
- Operations complete successfully.

---

## TC-USER-MOBILE-001

### Title

User Management on Mobile Browser

### Requirement

USER-005

### Priority

Medium

### Severity

Low

### Steps

1. Open application on supported mobile device.
2. Navigate User Management.

### Expected Result

- Responsive layout displayed.
- Forms usable.
- Tables remain readable.

---

## TC-USER-MOBILE-002

### Title

User Creation Using Tablet Device

### Requirement

USER-001

### Priority

Low

### Severity

Low

### Steps

1. Access application on tablet.
2. Create new user.

### Expected Result

- User creation completed successfully.
- Responsive interface maintained.

---

## TC-USER-PERF-001

### Title

User Search Performance

### Requirement

USER-004

### Priority

Medium

### Severity

Low

### Preconditions

Database populated with large number of users.

### Steps

1. Execute user search.

### Expected Result

- Search results returned within defined performance SLA.
- No application errors occur.

---

## TC-USER-PERF-002

### Title

Bulk Import Performance

### Requirement

USER-001

### Priority

Medium

### Severity

Medium

### Preconditions

Large valid import file available.

### Steps

1. Import bulk user dataset.

### Expected Result

- Import completes within acceptable processing time.
- System remains responsive.
- No data corruption occurs.

---

# Test Coverage Summary

| Functional Area | Coverage |
|-----------------|----------|
| User Creation | Complete |
| User Update | Complete |
| User Deletion | Complete |
| User Search | Complete |
| User Profile | Complete |
| User Activation | Complete |
| User Deactivation | Complete |
| Role Assignment | Complete |
| Permission Management | Complete |
| Bulk Operations | Complete |
| Import / Export | Complete |
| Audit Logging | Complete |
| Security Validation | Complete |
| Boundary Validation | Complete |
| Negative Testing | Complete |
| Accessibility | Complete |
| Browser Compatibility | Complete |
| Mobile Compatibility | Complete |
| Performance Validation | Complete |

---

# Quality Metrics

| Metric | Target |
|---------|--------|
| Requirement Coverage | 100% |
| Functional Coverage | 100% |
| Security Coverage | ≥95% |
| Automation Coverage | ≥85% |
| Critical Test Pass Rate | 100% |
| High Priority Test Pass Rate | ≥98% |
| User Management Coverage | 100% |
| Defect Leakage | 0 Critical |

---

# References

- ISO/IEC 29119 – Software Testing
- ISO/IEC 25010 – Software Product Quality
- IEEE 829 – Test Documentation
- OWASP ASVS
- OWASP Testing Guide
- NIST SP 800-53
- User Management Module Design Specification
- Software Requirements Specification (SRS)
- Master Test Plan
- Security Testing Standards

---

# End of Document
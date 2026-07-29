# System Administrator Manual

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Systems Engineering & Infrastructure Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** System Administration Manual  

---

# System Administrator Manual

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | System Administrator Manual |
| Target Persona | System Administrator / Platform Engineer |
| Required Role | `ROLE_SYSTEM_ADMIN` |
| Version | 1.0 |
| Last Updated | 2026-07-28 |

---

# Purpose

This manual specifies system configuration procedures, identity provider (Keycloak / Active Directory) integration, security audit log monitoring, system backup administration, license management, and platform maintenance tasks for System Administrators.

---

# Core Administrative Tasks

### Task 1: Provisioning New System Administrators & Roles
1. Access the Keycloak Admin Console at `https://auth.csp.gov.in/admin`.
2. Navigate to **Users -> Add User**.
3. Input official government email address and username.
4. Under **Role Mappings**, assign appropriate RBAC roles (`ROLE_SYSTEM_ADMIN`, `ROLE_AUDITOR`).
5. Enforce **Required User Actions -> Configure TOTP** (Enforce 2FA).

### Task 2: Reviewing Audit Logs
1. Navigate to `https://csp.gov.in/admin/audit-logs`.
2. Filter logs by Event Type (e.g., `LOGIN_FAILURE`, `PERMISSION_CHANGE`, `DATA_EXPORT`).
3. Export log archive for quarterly security audits.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Systems Lead | Alan Turing | 2026-07-28 |
| CISO | Victoria Sterling | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of System Admin Manual | Systems Team |

---

# End of Document

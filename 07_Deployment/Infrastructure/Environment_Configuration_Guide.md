# Environment Configuration Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Systems Engineering & DevOps Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Environment Configuration Specification  

---

# Environment Configuration Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Environment Configuration Guide |
| Domain | Systems Engineering |
| Version | 1.0 |
| Status | Approved |
| Owner | Systems Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document specifies the required environment variables, configuration files, feature flags, secret keys, and runtime parameters across development, staging, and production environments of the AI Rural Root Cause Discovery System.

---

# Environment Variable Matrix

| Variable Name | Data Type | Required | Description | Example (Staging / Prod) |
|---------------|-----------|----------|-------------|--------------------------|
| `NODE_ENV` | String | Yes | Application execution mode | `production` |
| `PORT` | Integer | Yes | API Gateway listening port | `8080` |
| `DATABASE_URL` | Connection String | Yes | PostgreSQL connection string | `postgres://user:pass@db.csp.gov.in:5432/csp_prod` |
| `REDIS_URL` | Connection String | Yes | Redis Feature Store URL | `redis://cache.csp.gov.in:6379/0` |
| `JWT_SECRET_KEY_ID` | KMS Key ID | Yes | AWS KMS Key ID for signing tokens | `arn:aws:kms:us-east-1:123456789012:key/k-8f92a` |
| `AI_INFERENCE_BATCH_SIZE` | Integer | Yes | Model vector batch size | `32` |
| `FEATURE_ENABLE_SMS_ALERTS` | Boolean | Yes | Enable emergency SMS notifications | `true` |

---

# Secret Management via HashiCorp Vault

All sensitive variables (DB passwords, KMS keys, API credentials) are stored under secret engine path `secret/data/csp/<environment>/`.

```bash
# Example Vault CLI Secret Injection Command
vault kv get -format=json secret/data/csp/production/database | jq .data.data
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Lead Systems Engineer | Alan Turing | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Environment Config Guide | Systems Team |

---

# End of Document

# Release Sanity Verification

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Release & Quality Assurance Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Production Deployment Verification Checklist  

---

# Release Sanity Verification

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Release Sanity Verification |
| Domain | Release Management & Production QA |
| Version | 1.0 |
| Status | Approved |
| Owner | Release QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document defines the post-deployment smoke testing checklist and sanity verification procedure executed immediately following a production software release or hotfix deployment for the AI Rural Root Cause Discovery System.

---

# Business Context

Deploying code updates into live government production environments requires rapid, non-destructive validation within 15 minutes of deployment to verify system health before user traffic is un-throttled.

---

# Post-Deployment Sanity Checklist

| Verification Step | Target Endpoint / Subsystem | Execution Method | Expected Result | Verified By |
|-------------------|-----------------------------|------------------|-----------------|-------------|
| **1. Service Healthcheck** | `GET /healthz` | Automated Curl Probe | HTTP `200 OK`, JSON `{ "status": "UP" }` | Release Eng |
| **2. Database Connectivity** | `GET /readyz` | Automated Probe | DB & Redis Ping Response < 10ms | Release Eng |
| **3. Web UI Availability** | `https://csp.gov.in/login` | Headless Browser Check | Page renders HTTP 200, assets load cleanly | QA Lead |
| **4. Authentication Flow** | `POST /api/v1/auth/login` | Automated API Probe | Valid JWT token issued for test admin account | Security Lead |
| **5. AI Model Loaded** | `GET /api/v1/ai/status` | Model Health API | Model version `RC-XGB-v2` loaded in GPU memory | MLOps Eng |
| **6. Message Queue Connection** | RabbitMQ Management API | Management API Call | Exchange `csp.events` active with 0 unacknowledged messages | DevOps Eng |
| **7. Monitoring & Logging** | Prometheus / Grafana | Dashboard Inspection | Metrics ingestion active; zero error spikes | Site Reliability Eng |

---

# Rollback Trigger Conditions

If any of the following critical failures occur during post-deployment verification, the Release Manager shall immediately issue an automated Kubernetes deployment rollback:

- Any HTTP `500 Internal Server Error` on core healthcheck or authentication endpoints.
- DB migration failure or unresolved schema locks lasting > 2 minutes.
- Unhandled model loading failure in the AI Inference Engine.
- Failure of more than 1 item in the Sanity Checklist above.

```bash
# Automated Rollback Execution Command
kubectl rollout undo deployment/csp-api-gateway -n production
kubectl rollout undo deployment/csp-ai-inference -n production
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Release Manager | Rachel Adams | 2026-07-28 |
| QA Lead | David Miller | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Release Sanity Verification | Release QA Team |

---

# End of Document

# REST API User Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** API Engineering & Integration Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Developer API Reference & Integration Guide  

---

# REST API User Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | REST API User Guide |
| Domain | REST API Reference |
| Version | 1.0 |
| OpenAPI Version | OpenAPI 3.0.3 |
| Status | Approved |
| Owner | API Engineering Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This guide provides developers and integration partners with complete OpenAPI specification references, authentication headers, request/response schemas, code examples, and error handling rules for the AI Rural Root Cause Discovery System REST API.

---

# Base URL & Authentication

- **Production Base URL**: `https://api.csp.gov.in/api/v1`
- **Staging Base URL**: `https://staging-api.csp.gov.in/api/v1`

All requests require HTTP Bearer Token authentication:
```http
Authorization: Bearer <your_jwt_access_token>
Content-Type: application/json
```

---

# Key Endpoints Reference

### 1. Ingest Rural Survey Record
`POST /surveys`

**Request Headers**:
- `Authorization`: `Bearer <jwt_token>`
- `Content-Type`: `application/json`

**Request Payload**:
```json
{
  "district_id": "DIST-804",
  "village_code": "VIL-102",
  "surveyor_id": "USR-409",
  "water_ph_level": 5.4,
  "turbidity_ntu": 12.5,
  "complaint_text": "Severe pipe corrosion near school pump."
}
```

**Response (201 Created)**:
```json
{
  "survey_id": "srv-90823-abc",
  "status": "QUEUED_FOR_AI_ANALYSIS",
  "created_at": "2026-07-28T12:00:00Z"
}
```

---

### 2. Fetch AI Root Cause Prediction
`GET /ai/root-cause/{survey_id}`

**Response (200 OK)**:
```json
{
  "survey_id": "srv-90823-abc",
  "primary_root_cause": "CHEMICAL_CONTAMINATION",
  "confidence_score": 0.942,
  "shap_factors": [
    { "feature": "water_ph_level", "value": 5.4, "weight": 0.45 }
  ]
}
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| API Lead | Sarah Jenkins | 2026-07-28 |
| Integration Architect | Marcus Vance | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of REST API Guide | API Team |

---

# End of Document

# API Integration Test Suite

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Integration Quality Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** API Contract & Integration Specification  

---

# API Integration Test Suite

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | API Integration Test Suite |
| Domain | REST API & Service Integration QA |
| Version | 1.0 |
| Status | Approved |
| Owner | Integration QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document outlines the integration test suite for verifying HTTP contract schemas, status codes, JWT authentication headers, payload validation rules, and error handling behaviors across all REST API endpoints of the AI Rural Root Cause Discovery System.

---

# Scope

The suite validates endpoints across the API Gateway for:
- Authentication & Session Management (`/api/v1/auth`)
- User Administration (`/api/v1/users`)
- Rural Survey Ingestion (`/api/v1/surveys`)
- AI Inference & Root Cause Discovery (`/api/v1/ai/root-cause`)
- Recommendation Engine (`/api/v1/recommendations`)
- Reporting & Analytics Exports (`/api/v1/reports`)

---

# Test Execution Framework

Tests are authored in Postman / Newman and PyTest, integrated into the CI/CD pipeline.

```bash
# Command execution for REST API Integration Suite
newman run Testing/Integration_Testing/CSP_API_Collection.json \
  --environment Testing/Integration_Testing/QA_Environment.json \
  --reporters cli,junit --reporter-junit-export Testing/Reports/API_Test_Results.xml
```

---

# API Test Cases & Contracts

### TC-API-INT-001: User Login & JWT Token Issuance
- **Endpoint**: `POST /api/v1/auth/login`
- **Request Payload**: `{ "username": "field_officer_01", "password": "SecurePassword123!" }`
- **Expected Status**: `200 OK`
- **Contract Validation**: Response body must contain valid JWT `access_token`, `refresh_token`, `token_type`: "Bearer", and `expires_in`: 3600.

### TC-API-INT-002: Survey Submission & AI Ingestion Trigger
- **Endpoint**: `POST /api/v1/surveys`
- **Headers**: `Authorization: Bearer <valid_jwt_token>`
- **Request Payload**:
  ```json
  {
    "district_id": "DIST-804",
    "village_code": "VIL-102",
    "surveyor_id": "USR-409",
    "water_quality_ph": 5.4,
    "complaint_text": "Severe pipe corrosion detected near primary school water pump."
  }
  ```
- **Expected Status**: `201 Created`
- **Contract Validation**: Response returns `survey_id` UUID and status `"QUEUED_FOR_AI_ANALYSIS"`. Validates that message is published to RabbitMQ / Kafka `survey-ingest` queue.

### TC-API-INT-003: AI Root Cause Inference Endpoint Contract
- **Endpoint**: `POST /api/v1/ai/root-cause/analyze`
- **Headers**: `Authorization: Bearer <valid_jwt_token>`
- **Request Payload**: `{ "survey_id": "srv-90823-abc" }`
- **Expected Status**: `200 OK`
- **Contract Validation**:
  ```json
  {
    "survey_id": "srv-90823-abc",
    "primary_root_cause": "CHEMICAL_CONTAMINATION",
    "confidence_score": 0.942,
    "contributing_factors": [
      { "factor": "LOW_PH_LEVEL", "weight": 0.45 },
      { "factor": "INFRASTRUCTURE_AGE", "weight": 0.38 }
    ]
  }
  ```

### TC-API-INT-004: Invalid Payload & Input Validation Error Schema
- **Endpoint**: `POST /api/v1/surveys`
- **Request Payload**: `{ "district_id": "" }` (Missing mandatory fields)
- **Expected Status**: `400 Bad Request`
- **Contract Validation**: Response conforms to RFC 7807 Problem Details schema containing `type`, `title`, `status`, `detail`, and `invalid_params` array.

---

# Key Quality Benchmarks

| Metric | Target |
|--------|--------|
| API Contract Compliance | 100% |
| Test Execution Pass Rate | ≥ 99.0% |
| API Response Time (p95) | ≤ 150 ms |

---

# Approval

| Role | Name | Date |
|------|------|------|
| QA Lead | David Miller | 2026-07-28 |
| Integration Architect | Sarah Jenkins | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of API Integration Test Suite | Integration QA Team |

---

# End of Document

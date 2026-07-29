# REST_API_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Backend Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** REST API Template

---

# REST API Template

---

# Template Information

| Field | Value |
|---------|---------|
| API Name | |
| Module | |
| Version | |
| Owner | |
| Status | Draft / Review / Approved |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe the purpose of the API.

Example

> Retrieves AI-generated recommendations for a completed rural survey.

---

# Endpoint Information

| Property | Value |
|----------|-------|
| Method | GET / POST / PUT / PATCH / DELETE |
| Endpoint | |
| API Version | v1 |
| Category | Public / Internal / Admin |
| Idempotent | Yes / No |

---

# Description

Provide a detailed description of the endpoint.

Include

- Business purpose
- Functional behavior
- Expected outcome
- Usage scenarios

---

# Authentication

Authentication Type

- JWT Bearer Token
- OAuth2
- API Key
- Public

Example

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# Authorization

Required Roles

-

-

Permissions

-

-

---

# Request Headers

| Header | Required | Description |
|----------|----------|-------------|
| Authorization | Yes | JWT access token |
| Content-Type | Yes | application/json |
| Accept | Yes | application/json |
| Idempotency-Key | Optional | Prevent duplicate processing |
| X-Correlation-ID | Optional | Request tracing |

---

# Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| | | | |

---

# Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| page | Integer | No | 0 | Page number |
| size | Integer | No | 20 | Page size |
| sort | String | No | | Sorting criteria |

---

# Request Body

Example

```json
{
  "field": "value"
}
```

---

# Request Schema

| Field | Type | Required | Validation | Description |
|---------|------|----------|------------|-------------|
| | | | | |

---

# Validation Rules

Validate

- Required fields
- Data types
- Length
- Format
- Range
- Enum values
- Business rules

---

# Business Logic

Processing Steps

1.

2.

3.

4.

---

# Success Response

HTTP Status

```
200 OK
```

Example

```json
{
  "success": true,
  "data": {},
  "timestamp": "YYYY-MM-DDTHH:MM:SSZ",
  "requestId": "REQ-123456",
  "correlationId": "CORR-789012"
}
```

---

# Error Responses

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
|400|VAL-001|Validation failed|
|401|AUTH-101|Authentication failed|
|403|AUTHZ-201|Unauthorized access|
|404|RES-301|Resource not found|
|409|BUS-401|Business rule violation|
|422|VAL-002|Constraint validation failed|
|429|SYS-429|Rate limit exceeded|
|500|SYS-500|Internal server error|
|503|SYS-503|Service unavailable|

---

# Response Headers

| Header | Description |
|----------|-------------|
| Content-Type | application/json |
| Cache-Control | Response caching policy |
| ETag | Entity version identifier |
| X-Correlation-ID | Request correlation identifier |

---

# Pagination

Applicable

Yes / No

Example

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 120,
  "totalPages": 6
}
```

---

# Filtering

Supported Filters

| Filter | Description |
|---------|-------------|
| | |

---

# Sorting

Supported Fields

-

-

-

Default Sort

-

---

# Rate Limiting

Limit

Example

```
100 requests/minute/user
```

Behavior

- Return HTTP 429 when exceeded
- Include retry information where applicable

---

# Caching

Cacheable

Yes / No

TTL

-

Invalidation Strategy

-

---

# Security

Input Validation

✓

Output Encoding

✓

Authorization

✓

Rate Limiting

✓

Sensitive Data Protection

✓

---

# Logging

Log

- Request ID
- Correlation ID
- User ID (where applicable)
- Response status
- Processing time

Do not log

- Passwords
- Tokens
- Sensitive personal information

---

# Monitoring

Metrics

- Request count
- Response time
- Error rate
- Success rate
- Throughput

Alerts

-

-

---

# Dependencies

Internal Services

-

External Services

-

Database Tables

-

Cache

-

---

# Performance Requirements

| Metric | Target |
|---------|---------|
| P95 Latency | <200 ms |
| Availability | ≥99.9% |
| Error Rate | <1% |

---

# Testing

Unit Tests

-

Integration Tests

-

Security Tests

-

Performance Tests

-

Contract Tests

-

---

# OpenAPI Documentation

Ensure the endpoint includes

- Summary
- Description
- Tags
- Parameters
- Request schema
- Response schema
- Error responses
- Security requirements
- Examples

---

# Deployment Considerations

Feature Flag

-

Backward Compatibility

-

Versioning

-

Rollback Strategy

-

---

# Risks

| Risk | Mitigation |
|------|------------|
| Breaking API changes | Versioning strategy |
| Invalid requests | Strong validation |
| Unauthorized access | RBAC enforcement |
| Performance degradation | Monitoring and load testing |

---

# Assumptions

-

-

-

---

# Open Issues

| ID | Description | Owner |
|----|-------------|-------|
| | | |

---

# References

- API Implementation Standards
- Secure Coding Standards
- Error Handling Standards
- OpenAPI Specification
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| Backend Developer | | |
| API Reviewer | | |
| Technical Lead | | |
| Architect | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Backend Engineering Team |
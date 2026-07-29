# Controller_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Backend Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** REST Controller Template

---

# Controller Template

---

# Template Information

| Field | Value |
|---------|---------|
| Controller Name | |
| Module | |
| Package | |
| API Version | |
| Owner | |
| Version | |
| Status | Draft / Review / Approved |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe the responsibility of this controller.

Example

> Exposes REST endpoints for managing rural surveys, validating requests, invoking business services, and returning standardized API responses.

---

# Business Context

Describe

- Business capability
- Domain area
- Supported user roles
- Related services

---

# Controller Definition

Example

```java
@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@Validated
public class SurveyController {

}
```

---

# Responsibilities

The controller shall

- Accept HTTP requests
- Validate request data
- Authenticate requests
- Authorize access
- Invoke service layer
- Return standardized responses
- Never contain business logic
- Never access repositories directly

---

# Base URL

| Property | Value |
|----------|-------|
| Base Path | |
| API Version | v1 |
| Content Type | application/json |

---

# Supported Endpoints

| Method | Endpoint | Description |
|----------|----------|-------------|
| GET | | |
| POST | | |
| PUT | | |
| PATCH | | |
| DELETE | | |

---

# Request Parameters

### Path Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| | | | |

### Query Parameters

| Name | Type | Required | Default | Description |
|------|------|----------|----------|-------------|
| page | Integer | No | 0 | Page number |
| size | Integer | No | 20 | Page size |
| sort | String | No | | Sort criteria |

### Request Headers

| Header | Required | Description |
|----------|----------|-------------|
| Authorization | Yes | JWT Bearer Token |
| Content-Type | Yes | application/json |
| Accept | Yes | application/json |
| X-Correlation-ID | Optional | Request tracing |
| Idempotency-Key | Optional | Duplicate request prevention |

---

# Request Body

Example

```json
{
  "field": "value"
}
```

---

# Validation

Validate

- Required fields
- Bean Validation annotations
- Input format
- Enum values
- Path variables
- Query parameters

Use

```java
@Valid

@Validated
```

---

# Authentication

Supported Methods

- JWT Bearer Token
- OAuth2
- API Gateway Authentication

Document authentication requirements for each endpoint.

---

# Authorization

Supported Controls

- RBAC
- Method-level security
- Resource ownership checks

Example

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

# Service Dependencies

| Service | Purpose |
|----------|----------|
| | |

The controller shall delegate all business logic to services.

---

# Response Structure

Success Example

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {},
  "timestamp": "YYYY-MM-DDTHH:MM:SSZ",
  "requestId": "REQ-123456",
  "correlationId": "CORR-987654"
}
```

---

# Error Responses

| HTTP Status | Description |
|-------------|-------------|
| 400 | Validation failed |
| 401 | Authentication failed |
| 403 | Access denied |
| 404 | Resource not found |
| 409 | Business conflict |
| 422 | Validation error |
| 429 | Rate limit exceeded |
| 500 | Internal server error |

---

# API Versioning

Strategy

- URI Versioning
- Header Versioning (if applicable)

Backward Compatibility

-

Deprecation Policy

-

---

# Rate Limiting

Configuration

-

Throttle Policy

-

Retry Behavior

-

---

# OpenAPI Documentation

Annotate using

- @Operation
- @ApiResponses
- @Parameter
- @Tag
- @SecurityRequirement

Document

- Summary
- Description
- Parameters
- Request body
- Responses
- Security requirements

---

# Logging

Log

- Request received
- Response status
- Request ID
- Correlation ID
- Processing duration

Do not log

- Passwords
- Access tokens
- Sensitive personal information (PII)

---

# Monitoring

Metrics

- Request count
- Success rate
- Failure rate
- Response time
- Endpoint latency

Health Indicators

-

Alerts

-

---

# Security Considerations

Protect against

- Injection attacks
- Broken authentication
- Mass assignment
- Insecure direct object references
- Excessive data exposure

Apply

- Input validation
- Output encoding
- Standardized error handling

---

# Performance Considerations

Consider

- Pagination
- Response compression
- Caching headers
- Efficient DTOs

Avoid

- Returning large payloads
- Blocking operations
- Business processing inside controllers

---

# Exception Handling

Use

- Global exception handler
- Standardized error responses
- Correlation IDs

Avoid

- Catching generic exceptions unnecessarily
- Returning stack traces

---

# Testing

Unit Tests

-

Controller Tests

-

Integration Tests

-

Security Tests

-

API Contract Tests

-

Recommended Tools

- Spring Boot Test
- MockMvc
- RestAssured
- JUnit 5

---

# Deployment Considerations

API Gateway

-

Load Balancer

-

Feature Flags

-

Version Compatibility

-

---

# Documentation

Document

- Endpoint purpose
- Authentication
- Authorization
- Request examples
- Response examples
- Known limitations

---

# Risks

| Risk | Mitigation |
|------|------------|
| Unauthorized access | RBAC and JWT validation |
| Invalid requests | Strong validation |
| Breaking API changes | API versioning |
| Excessive traffic | Rate limiting and throttling |

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
- Backend Implementation Standards
- Secure Coding Standards
- REST API Template
- Spring Framework Documentation
- OpenAPI Specification
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| Backend Developer | | |
| API Reviewer | | |
| Technical Lead | | |
| Solution Architect | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Backend Engineering Team |
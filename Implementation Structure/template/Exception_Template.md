# Exception_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Backend Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** Exception Handling Template

---

# Exception Template

---

# Template Information

| Field | Value |
|---------|---------|
| Exception Name | |
| Module | |
| Package | |
| Error Code | |
| HTTP Status | |
| Version | |
| Status | Draft / Review / Approved |
| Author | |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe why this exception exists.

Example

> Thrown when a requested survey cannot be located within the system.

---

# Business Context

Describe

- Business capability
- Failure scenario
- Expected system behavior
- Consumer impact

---

# Exception Classification

| Category | Description |
|----------|-------------|
| Validation | Invalid client input |
| Authentication | User identity verification failure |
| Authorization | Insufficient permissions |
| Business | Business rule violation |
| Resource | Missing resource |
| Integration | External dependency failure |
| Infrastructure | Database, cache, filesystem |
| AI | AI inference or model failure |
| System | Unexpected internal failure |

---

# Exception Definition

Example

```java
public class SurveyNotFoundException extends RuntimeException {

    public SurveyNotFoundException(UUID surveyId) {
        super("Survey not found: " + surveyId);
    }

}
```

---

# Error Code

Every exception shall have a unique application error code.

Example

| Error Code | Description |
|------------|-------------|
| VAL-001 | Validation failed |
| AUTH-101 | Authentication failed |
| AUTHZ-201 | Authorization denied |
| RES-301 | Resource not found |
| BUS-401 | Business rule violation |
| AI-501 | AI processing failure |
| SYS-500 | Internal server error |

---

# HTTP Status Mapping

| Exception Type | HTTP Status |
|----------------|-------------|
| ValidationException | 400 Bad Request |
| AuthenticationException | 401 Unauthorized |
| AccessDeniedException | 403 Forbidden |
| ResourceNotFoundException | 404 Not Found |
| BusinessException | 409 Conflict |
| ConstraintViolationException | 422 Unprocessable Entity |
| RateLimitException | 429 Too Many Requests |
| SystemException | 500 Internal Server Error |
| ExternalServiceException | 503 Service Unavailable |

---

# Global Exception Handling

Use

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

}
```

Responsibilities

- Catch uncaught exceptions
- Map exceptions to HTTP responses
- Return standardized error payloads
- Log failures
- Preserve correlation IDs

---

# Standard Error Response

Preferred format follows **RFC 7807 Problem Details** with project-specific extensions.

Example

```json
{
  "type": "https://example.com/errors/resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Survey with ID 12345 was not found.",
  "instance": "/api/v1/surveys/12345",
  "errorCode": "RES-301",
  "timestamp": "YYYY-MM-DDTHH:MM:SSZ",
  "requestId": "REQ-123456",
  "correlationId": "CORR-987654"
}
```

---

# Exception Flow

```text
Controller

↓

Validation

↓

Service

↓

Repository / External Service

↓

Exception Raised

↓

Global Exception Handler

↓

Standardized Error Response
```

---

# Validation Errors

Include

- Field name
- Rejected value (where safe)
- Validation message
- Constraint violated

Example

```json
{
  "errors": [
    {
      "field": "village",
      "message": "Village name is required."
    }
  ]
}
```

---

# Retry Guidance

Specify whether the client should retry.

| Exception | Retry |
|-----------|--------|
| Validation | No |
| Authentication | No |
| Authorization | No |
| Resource Not Found | No |
| External Service Timeout | Yes |
| Database Deadlock | Yes |
| Rate Limit | Yes (after delay) |
| Internal Server Error | Depends |

---

# Logging

Log

- Error code
- Exception class
- Stack trace (server-side only)
- Request ID
- Correlation ID
- User ID (if authenticated)
- Execution duration

Do not log

- Passwords
- Tokens
- Secrets
- Personally identifiable information (PII)

---

# Distributed Tracing

Include

- Trace ID
- Span ID
- Correlation ID
- Request ID

Ensure exceptions are linked to distributed traces.

---

# Security Considerations

Never expose

- Stack traces
- SQL queries
- Internal implementation details
- File paths
- Secrets
- Infrastructure information

Return only safe, user-friendly messages.

---

# Monitoring

Track

- Exception frequency
- Error rate
- Top exceptions
- Failed endpoints
- AI inference failures
- External service failures

Generate alerts for

- High error rates
- Repeated infrastructure failures
- AI service degradation

---

# Metrics

Recommended metrics

- Exception count
- Exceptions by type
- HTTP status distribution
- Mean time between failures (MTBF)
- Error rate by endpoint

---

# Recovery Strategy

Document

- Automatic retry
- Circuit breaker behavior
- Fallback response
- Manual intervention steps

---

# Testing

Validate

- Exception mapping
- HTTP status codes
- Error payload structure
- Logging behavior
- Security of error messages
- Retry behavior
- Correlation ID propagation

Recommended Tools

- JUnit 5
- MockMvc
- Spring Boot Test
- WireMock (for integration failures)

---

# Documentation

Document

- Exception purpose
- Error code
- Trigger conditions
- Expected client behavior
- Recovery guidance

---

# Risks

| Risk | Mitigation |
|------|------------|
| Information disclosure | Sanitize responses |
| Inconsistent error handling | Global exception handler |
| Missing error codes | Centralized registry |
| Unhandled runtime exceptions | Catch-all handler and monitoring |

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

- Error Handling Standards
- Secure Coding Standards
- Backend Implementation Standards
- RFC 7807 – Problem Details for HTTP APIs
- Spring Framework Documentation
- OWASP ASVS
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| Backend Developer | | |
| QA Lead | | |
| Technical Lead | | |
| Solution Architect | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Backend Engineering Team |
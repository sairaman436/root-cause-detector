# API Gateway Test Cases

**Document ID:** TC-APIGW-AIRRCD-001  
**Project:** AI Rural Root Cause Discovery System  
**Module:** API Gateway  
**Version:** 1.0  
**Classification:** Internal – Confidential  
**Prepared By:** QA Team  
**Reviewed By:** Solution Architect, QA Lead  
**Approved By:** Project Manager

---

# Revision History

| Version | Date | Author | Description |
|----------|------|--------|-------------|
| 0.1 | DD-MM-YYYY | QA Team | Initial Draft |
| 0.5 | DD-MM-YYYY | Solution Architect | Technical Review |
| 1.0 | DD-MM-YYYY | QA Lead | Approved |

---

# Purpose

This document defines comprehensive test cases for validating the API Gateway responsible for routing, authentication, authorization, rate limiting, monitoring, logging, caching, request transformation, response transformation, and secure communication between clients and backend services.

---

# Scope

Testing covers:

- API Routing
- Authentication
- Authorization
- JWT Validation
- OAuth2 Integration
- API Key Validation
- Rate Limiting
- Throttling
- Request Validation
- Response Validation
- API Versioning
- Caching
- Circuit Breaker
- Logging
- Monitoring
- Error Handling
- Security
- Performance

---

# Requirement Traceability

| Requirement ID | Description |
|----------------|-------------|
| API-001 | Request Routing |
| API-002 | Authentication |
| API-003 | Authorization |
| API-004 | JWT Validation |
| API-005 | OAuth2 Authentication |
| API-006 | API Key Validation |
| API-007 | Rate Limiting |
| API-008 | Request Validation |
| API-009 | Response Transformation |
| API-010 | API Versioning |
| API-011 | Gateway Logging |
| API-012 | Monitoring & Metrics |
| API-013 | Error Handling |

---

# Test Case Summary

| Category | Planned |
|----------|---------|
| Functional Tests | 42 |
| Security Tests | 18 |
| Performance Tests | 10 |
| Compatibility Tests | 8 |
| Negative Tests | 10 |
| Total | 88 |

---

# Test Cases

---

## TC-API-ROUTE-001

### Title

Route Request to User Service

### Requirement

API-001

### Priority

Critical

### Severity

Critical

### Preconditions

- API Gateway running.
- User Service available.

### Steps

1. Send request to `/api/users`.
2. Observe gateway routing.

### Expected Result

- Request routed correctly.
- HTTP 200 returned.
- Response matches backend service.

---

## TC-API-ROUTE-002

### Title

Route Request to Survey Service

### Requirement

API-001

### Priority

Critical

### Severity

Critical

### Steps

1. Invoke `/api/surveys`.

### Expected Result

- Request forwarded successfully.
- Correct backend receives request.
- Response returned without modification unless configured.

---

## TC-API-ROUTE-003

### Title

Route Request to AI Analysis Service

### Requirement

API-001

### Priority

Critical

### Severity

High

### Steps

1. Invoke AI prediction endpoint.

### Expected Result

- Gateway forwards request.
- AI service responds successfully.
- Correlation ID preserved.

---

## TC-API-ROUTE-004

### Title

Route Request to Reporting Service

### Requirement

API-001

### Priority

High

### Severity

Medium

### Steps

1. Request report generation endpoint.

### Expected Result

- Gateway routes request correctly.
- Response delivered successfully.

---

## TC-API-AUTH-001

### Title

Authenticate Using Valid JWT

### Requirement

API-002

### Priority

Critical

### Severity

Critical

### Preconditions

Valid JWT available.

### Steps

1. Include JWT in Authorization header.
2. Invoke protected endpoint.

### Expected Result

- Authentication succeeds.
- Request forwarded.
- User context propagated.

---

## TC-API-AUTH-002

### Title

Reject Missing JWT

### Requirement

API-002

### Priority

Critical

### Severity

Critical

### Steps

1. Call protected API without Authorization header.

### Expected Result

- HTTP 401 Unauthorized.
- Request rejected.
- Security log generated.

---

## TC-API-AUTH-003

### Title

Reject Expired JWT

### Requirement

API-004

### Priority

Critical

### Severity

Critical

### Preconditions

Expired JWT available.

### Steps

1. Invoke protected endpoint.

### Expected Result

- Token rejected.
- HTTP 401 returned.
- Expiration reason recorded.

---

## TC-API-AUTH-004

### Title

Reject Tampered JWT

### Requirement

API-004

### Priority

Critical

### Severity

Critical

### Steps

1. Modify JWT payload.
2. Submit request.

### Expected Result

- Signature validation fails.
- Request denied.
- Security event logged.

---

## TC-API-OAUTH-001

### Title

Authenticate Using OAuth2 Access Token

### Requirement

API-005

### Priority

Critical

### Severity

Critical

### Preconditions

Valid OAuth2 token available.

### Steps

1. Invoke protected API.

### Expected Result

- OAuth2 token validated.
- Access granted.

---

## TC-API-OAUTH-002

### Title

Reject Invalid OAuth2 Token

### Requirement

API-005

### Priority

Critical

### Severity

Critical

### Steps

1. Submit invalid OAuth token.

### Expected Result

- Token rejected.
- HTTP 401 returned.
- Security log generated.

---

## TC-API-KEY-001

### Title

Access API Using Valid API Key

### Requirement

API-006

### Priority

High

### Severity

Medium

### Preconditions

Valid API key available.

### Steps

1. Submit request with API key.

### Expected Result

- API key validated.
- Request processed successfully.

---

## TC-API-KEY-002

### Title

Reject Invalid API Key

### Requirement

API-006

### Priority

High

### Severity

High

### Steps

1. Submit invalid API key.

### Expected Result

- Request rejected.
- HTTP 401 or 403 returned.
- Security event recorded.

---

## TC-API-AUTHZ-001

### Title

Authorized User Access

### Requirement

API-003

### Priority

Critical

### Severity

Critical

### Preconditions

Authorized role assigned.

### Steps

1. Invoke protected endpoint.

### Expected Result

- Authorization successful.
- Resource returned.

---

## TC-API-AUTHZ-002

### Title

Unauthorized Role Access

### Requirement

API-003

### Priority

Critical

### Severity

Critical

### Steps

1. Login using unauthorized role.
2. Access protected API.

### Expected Result

- HTTP 403 Forbidden.
- Access denied.
- Security log generated.

## TC-API-RATE-001

### Title

Allow Requests Within Rate Limit

### Requirement

API-007

### Priority

High

### Severity

Medium

### Preconditions

Rate limiting configured.

### Steps

1. Send requests below configured threshold.

### Expected Result

- All requests processed successfully.
- HTTP 200 returned.
- No throttling applied.

---

## TC-API-RATE-002

### Title

Reject Requests Exceeding Rate Limit

### Requirement

API-007

### Priority

Critical

### Severity

High

### Steps

1. Send requests exceeding configured rate limit.

### Expected Result

- Excess requests rejected.
- HTTP 429 Too Many Requests returned.
- Retry-After header included.
- Event logged.

---

## TC-API-RATE-003

### Title

Rate Limit Reset Validation

### Requirement

API-007

### Priority

Medium

### Severity

Low

### Steps

1. Reach rate limit.
2. Wait until reset period expires.
3. Send another request.

### Expected Result

- Request accepted.
- Rate limit counter reset correctly.

---

## TC-API-RATE-004

### Title

Independent Rate Limits Per Client

### Requirement

API-007

### Priority

High

### Severity

Medium

### Steps

1. Send requests from Client A until limit reached.
2. Send request from Client B.

### Expected Result

- Client A receives HTTP 429.
- Client B requests continue successfully.

---

## TC-API-REQ-001

### Title

Validate Required Request Parameters

### Requirement

API-008

### Priority

High

### Severity

Medium

### Steps

1. Submit request missing mandatory fields.

### Expected Result

- Validation error returned.
- HTTP 400 Bad Request.
- Clear validation message provided.

---

## TC-API-REQ-002

### Title

Validate Request Data Types

### Requirement

API-008

### Priority

High

### Severity

Medium

### Steps

1. Submit invalid data types.

### Expected Result

- Invalid request rejected.
- Validation errors returned.
- Backend service not invoked.

---

## TC-API-REQ-003

### Title

Validate Request Size Limit

### Requirement

API-008

### Priority

Medium

### Severity

Medium

### Steps

1. Submit payload larger than configured limit.

### Expected Result

- Request rejected.
- HTTP 413 Payload Too Large returned.
- Request logged.

---

## TC-API-REQ-004

### Title

Validate Unsupported Content Type

### Requirement

API-008

### Priority

Medium

### Severity

Medium

### Steps

1. Send request with unsupported Content-Type header.

### Expected Result

- HTTP 415 Unsupported Media Type returned.
- Backend service not invoked.

---

## TC-API-RESP-001

### Title

Validate Response Transformation

### Requirement

API-009

### Priority

Medium

### Severity

Low

### Steps

1. Invoke API requiring response transformation.

### Expected Result

- Response transformed according to configuration.
- Response schema maintained.

---

## TC-API-RESP-002

### Title

Remove Sensitive Fields from Response

### Requirement

API-009

### Priority

Critical

### Severity

High

### Steps

1. Invoke secured API.

### Expected Result

- Internal fields removed.
- Sensitive information masked.
- Client receives only permitted data.

---

## TC-API-RESP-003

### Title

Response Header Transformation

### Requirement

API-009

### Priority

Medium

### Severity

Low

### Steps

1. Invoke API.

### Expected Result

- Required headers added.
- Restricted headers removed.
- Security headers present.

---

## TC-API-VERSION-001

### Title

Access API Version 1

### Requirement

API-010

### Priority

Medium

### Severity

Low

### Steps

1. Invoke `/api/v1/...`

### Expected Result

- Version 1 endpoints function correctly.
- Appropriate response returned.

---

## TC-API-VERSION-002

### Title

Access API Version 2

### Requirement

API-010

### Priority

Medium

### Severity

Low

### Steps

1. Invoke `/api/v2/...`

### Expected Result

- Version 2 endpoints function correctly.
- Correct schema returned.

---

## TC-API-VERSION-003

### Title

Reject Unsupported API Version

### Requirement

API-010

### Priority

Medium

### Severity

Medium

### Steps

1. Invoke unsupported API version.

### Expected Result

- HTTP 404 or 400 returned.
- Clear version error message displayed.

---

## TC-API-CACHE-001

### Title

Serve Cached Response

### Requirement

API-009

### Priority

Medium

### Severity

Low

### Preconditions

Caching enabled.

### Steps

1. Invoke cacheable endpoint twice.

### Expected Result

- First request reaches backend.
- Second request served from cache.
- Response identical.

---

## TC-API-CACHE-002

### Title

Cache Expiration Validation

### Requirement

API-009

### Priority

Medium

### Severity

Low

### Steps

1. Request cached resource.
2. Wait until TTL expires.
3. Request resource again.

### Expected Result

- Cache invalidated.
- Backend queried again.
- Cache refreshed successfully.

---

## TC-API-CACHE-003

### Title

Cache Bypass for Non-Cacheable Endpoints

### Requirement

API-009

### Priority

Medium

### Severity

Low

### Steps

1. Invoke endpoint configured as non-cacheable.

### Expected Result

- Request always reaches backend.
- No cached response returned.

---

## TC-API-CB-001

### Title

Circuit Breaker Opens During Backend Failure

### Requirement

API-013

### Priority

Critical

### Severity

High

### Preconditions

Backend service unavailable.

### Steps

1. Send repeated requests until failure threshold reached.

### Expected Result

- Circuit breaker transitions to Open state.
- Requests rejected immediately.
- Failure logged.

---

## TC-API-CB-002

### Title

Circuit Breaker Recovery Validation

### Requirement

API-013

### Priority

High

### Severity

Medium

### Preconditions

Circuit breaker open.

### Steps

1. Restore backend service.
2. Wait for recovery interval.
3. Send request.

### Expected Result

- Circuit enters Half-Open state.
- Successful request closes circuit.
- Normal routing resumes.

---

## TC-API-CB-003

### Title

Fallback Response During Service Failure

### Requirement

API-013

### Priority

Medium

### Severity

Medium

### Steps

1. Make request while backend unavailable.

### Expected Result

- Configured fallback response returned.
- Gateway remains available.
- User receives meaningful error information.

## TC-API-LOG-001

### Title

Log Successful API Request

### Requirement

API-011

### Priority

High

### Severity

Medium

### Preconditions

Gateway logging enabled.

### Steps

1. Invoke a valid API endpoint.
2. Review gateway logs.

### Expected Result

Log entry contains:

- Timestamp
- Request ID
- Correlation ID
- Client IP
- HTTP Method
- Endpoint
- Response Status
- Processing Time
- Authenticated User

---

## TC-API-LOG-002

### Title

Log Failed API Request

### Requirement

API-011

### Priority

High

### Severity

Medium

### Steps

1. Invoke API with invalid payload.
2. Review logs.

### Expected Result

- Validation failure recorded.
- HTTP status captured.
- Error code stored.
- Correlation ID present.

---

## TC-API-LOG-003

### Title

Sensitive Data Masking in Logs

### Requirement

API-011

### Priority

Critical

### Severity

Critical

### Steps

1. Send request containing passwords, JWTs, API keys, and PII.
2. Inspect gateway logs.

### Expected Result

- Sensitive values masked.
- Passwords never logged.
- Tokens partially masked.
- Logging complies with organizational security policy.

---

## TC-API-LOG-004

### Title

Distributed Trace Correlation

### Requirement

API-011

### Priority

Medium

### Severity

Low

### Steps

1. Invoke request passing Correlation-ID header.
2. Verify downstream service logs.

### Expected Result

- Same Correlation ID propagated.
- End-to-end trace maintained.
- Logs linked across services.

---

## TC-API-MON-001

### Title

API Metrics Collection

### Requirement

API-012

### Priority

High

### Severity

Medium

### Steps

1. Execute multiple API requests.
2. Open monitoring dashboard.

### Expected Result

Metrics collected include:

- Request Count
- Error Rate
- Average Response Time
- Throughput
- Active Connections

---

## TC-API-MON-002

### Title

Gateway Health Check

### Requirement

API-012

### Priority

High

### Severity

Medium

### Steps

1. Invoke health endpoint.

### Expected Result

- HTTP 200 returned.
- Gateway status reported as Healthy.
- Dependencies accurately reflected.

---

## TC-API-MON-003

### Title

Backend Service Availability Monitoring

### Requirement

API-012

### Priority

Medium

### Severity

Medium

### Steps

1. Stop backend service.
2. Observe monitoring dashboard.

### Expected Result

- Backend marked unhealthy.
- Alert generated.
- Service availability updated.

---

## TC-API-MON-004

### Title

Alert Generation for High Error Rate

### Requirement

API-012

### Priority

Medium

### Severity

Medium

### Steps

1. Generate repeated API failures.

### Expected Result

- Error threshold exceeded.
- Monitoring alert triggered.
- Alert includes affected service and error metrics.

---

## TC-API-ERR-001

### Title

Handle Backend Service Timeout

### Requirement

API-013

### Priority

Critical

### Severity

High

### Preconditions

Backend response delayed beyond timeout threshold.

### Steps

1. Invoke API.

### Expected Result

- Gateway timeout enforced.
- HTTP 504 Gateway Timeout returned.
- Timeout logged.

---

## TC-API-ERR-002

### Title

Handle Backend Service Unavailable

### Requirement

API-013

### Priority

Critical

### Severity

High

### Steps

1. Stop backend service.
2. Invoke API.

### Expected Result

- HTTP 503 Service Unavailable returned.
- Friendly error message displayed.
- Failure logged.

---

## TC-API-ERR-003

### Title

Handle Invalid JSON Payload

### Requirement

API-008

### Priority

Medium

### Severity

Medium

### Steps

1. Submit malformed JSON request.

### Expected Result

- HTTP 400 Bad Request returned.
- Parsing error reported.
- Backend not invoked.

---

## TC-API-ERR-004

### Title

Handle Unsupported HTTP Method

### Requirement

API-001

### Priority

Medium

### Severity

Low

### Steps

1. Send unsupported HTTP method to endpoint.

### Expected Result

- HTTP 405 Method Not Allowed returned.
- Allowed methods identified.

---

## TC-API-SEC-001

### Title

SQL Injection Protection

### Requirement

API-008

### Priority

Critical

### Severity

Critical

### Test Data

```sql
' OR 1=1 --
```

### Steps

1. Submit SQL injection payload through API.

### Expected Result

- Payload sanitized.
- Request rejected where appropriate.
- Backend protected.
- Security event logged.

---

## TC-API-SEC-002

### Title

Cross-Site Scripting Protection

### Requirement

API-008

### Priority

Critical

### Severity

Critical

### Test Data

```html
<script>alert('api')</script>
```

### Steps

1. Submit XSS payload.

### Expected Result

- Payload sanitized.
- Script not executed.
- Security log created.

---

## TC-API-SEC-003

### Title

Cross-Origin Resource Sharing (CORS) Validation

### Requirement

API-002

### Priority

High

### Severity

Medium

### Steps

1. Send request from unauthorized origin.

### Expected Result

- Request blocked according to CORS policy.
- Appropriate headers returned.

---

## TC-API-SEC-004

### Title

HTTP Security Headers Validation

### Requirement

API-002

### Priority

High

### Severity

Medium

### Steps

1. Invoke secured endpoint.
2. Inspect response headers.

### Expected Result

Response includes configured security headers such as:

- Strict-Transport-Security
- X-Content-Type-Options
- X-Frame-Options
- Content-Security-Policy
- Referrer-Policy

---

## TC-API-NEG-001

### Title

Access Undefined Endpoint

### Requirement

API-001

### Priority

Medium

### Severity

Low

### Steps

1. Request nonexistent API endpoint.

### Expected Result

- HTTP 404 returned.
- Standard error response returned.
- Request logged.

---

## TC-API-NEG-002

### Title

Send Empty Request Body

### Requirement

API-008

### Priority

Medium

### Severity

Medium

### Steps

1. Submit request without body to endpoint requiring payload.

### Expected Result

- Validation failure returned.
- Backend not invoked.

---

## TC-API-NEG-003

### Title

Duplicate Request Submission

### Requirement

API-001

### Priority

Medium

### Severity

Medium

### Steps

1. Submit identical request repeatedly.

### Expected Result

- Duplicate handling follows business rules.
- No unintended duplicate processing.

---

## TC-API-COMP-001

### Title

Gateway Compatibility with Postman

### Requirement

API-001

### Priority

Low

### Severity

Low

### Steps

1. Execute API collection using Postman.

### Expected Result

- All supported endpoints operate correctly.
- Responses consistent with specification.

---

## TC-API-COMP-002

### Title

Gateway Compatibility with Browser Clients

### Requirement

API-001

### Priority

Low

### Severity

Low

### Steps

1. Invoke APIs through browser-based application.

### Expected Result

- API calls succeed.
- CORS policies enforced correctly.
- Responses handled successfully.

---

## TC-API-PERF-001

### Title

High Concurrent Request Processing

### Requirement

API-012

### Priority

Critical

### Severity

High

### Preconditions

Load testing environment available.

### Steps

1. Simulate concurrent API requests.

### Expected Result

- Gateway processes requests within SLA.
- No unexpected failures.
- Stable resource utilization.

---

## TC-API-PERF-002

### Title

Gateway Throughput Validation

### Requirement

API-012

### Priority

High

### Severity

Medium

### Steps

1. Execute sustained workload.

### Expected Result

- Throughput meets performance objectives.
- CPU and memory remain within acceptable thresholds.
- No significant degradation observed.

---

# Test Coverage Summary

| Functional Area | Coverage |
|-----------------|----------|
| Request Routing | Complete |
| Authentication | Complete |
| Authorization | Complete |
| JWT Validation | Complete |
| OAuth2 Authentication | Complete |
| API Key Validation | Complete |
| Rate Limiting | Complete |
| Request Validation | Complete |
| Response Transformation | Complete |
| API Versioning | Complete |
| Caching | Complete |
| Circuit Breaker | Complete |
| Logging | Complete |
| Monitoring | Complete |
| Error Handling | Complete |
| Security Validation | Complete |
| Negative Testing | Complete |
| API Client Compatibility | Complete |
| Performance Validation | Complete |

---

# Quality Metrics

| Metric | Target |
|---------|--------|
| Requirement Coverage | 100% |
| Functional Coverage | 100% |
| API Availability | ≥99.9% |
| API Success Rate | ≥99% |
| Gateway Response Time (P95) | ≤500 ms |
| Authentication Success Rate | ≥99% |
| Rate Limit Enforcement Accuracy | 100% |
| Security Test Coverage | ≥95% |
| Automation Coverage | ≥90% |
| Critical Test Pass Rate | 100% |
| Defect Leakage | 0 Critical |

---

# References

- ISO/IEC 29119 – Software Testing
- ISO/IEC 25010 – Software Product Quality
- IEEE 829 – Test Documentation
- OWASP ASVS
- OWASP API Security Top 10
- OWASP Testing Guide
- NIST SP 800-53
- RFC 7519 (JWT)
- OAuth 2.0 Framework (RFC 6749)
- OpenAPI Specification
- Software Requirements Specification (SRS)
- API Gateway Design Specification
- Master Test Plan

---

# End of Document
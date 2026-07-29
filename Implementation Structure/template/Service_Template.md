# Service_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Backend Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** Service Layer Template

---

# Service Template

---

# Template Information

| Field | Value |
|---------|---------|
| Service Name | |
| Module | |
| Package | |
| Owner | |
| Version | |
| Status | Draft / Review / Approved |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe the business capability provided by this service.

Example

> Processes rural survey submissions, validates input data, stores survey responses, triggers AI analysis, and returns recommendations.

---

# Business Context

Describe

- Business domain
- Business capability
- Responsibilities
- Related workflows

---

# Service Definition

Example

```java
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService {

}
```

---

# Responsibilities

The service shall

- Execute business logic
- Validate business rules
- Coordinate repositories
- Invoke external services
- Publish domain events
- Handle transactions
- Return DTOs
- Never expose persistence entities directly

---

# Dependencies

Repositories

-

-

External Services

-

-

Utilities

-

Configuration

-

Event Publishers

-

---

# Public Operations

| Method | Purpose |
|---------|----------|
| | |
| | |

Example

```java
createSurvey()

updateSurvey()

deleteSurvey()

getSurvey()

searchSurveys()
```

---

# Business Workflow

Example

```text
Client Request

↓

Validation

↓

Business Rules

↓

Repository

↓

External Services

↓

AI Processing

↓

Event Publishing

↓

Response DTO
```

---

# Business Rules

Rule 1

-

Rule 2

-

Rule 3

-

Rule 4

-

---

# Input Validation

Validate

- Required fields
- Business constraints
- Data consistency
- Duplicate records
- Authorization rules

Validation failures shall produce standardized exceptions.

---

# Transactions

Transaction Type

- Read Only
- Read Write

Example

```java
@Transactional

@Transactional(readOnly = true)
```

Rollback Conditions

-

-

Propagation

- REQUIRED
- REQUIRES_NEW
- MANDATORY (when applicable)

---

# Repository Usage

Repositories Used

-

-

Operations

- save
- update
- delete
- search
- exists
- pagination

---

# External Integrations

| Service | Purpose |
|----------|----------|
| AI Service | Root cause prediction |
| Notification Service | User alerts |
| GIS Service | Location validation |

Integration Requirements

- Timeout handling
- Retry policy
- Circuit breaker
- Fallback strategy

---

# DTO Mapping

Request DTOs

-

Response DTOs

-

Mapper

- MapStruct
- Manual Mapping (if justified)

Do not expose JPA entities outside the service layer.

---

# Event Processing

Published Events

-

-

Consumed Events

-

-

Message Broker

- Kafka
- RabbitMQ
- Other

---

# Caching

Cacheable Methods

-

Cache Names

-

TTL

-

Eviction Strategy

-

---

# Security

Authentication

- JWT
- OAuth2

Authorization

- Role-based access control
- Method-level security

Sensitive Data

- Encrypt where required
- Never log secrets
- Apply data masking

---

# Error Handling

Handle

- Validation failures
- Entity not found
- Business rule violations
- External service failures
- Database exceptions
- Timeout exceptions

Convert internal exceptions into standardized application exceptions.

---

# Logging

Log

- Service entry
- Service exit
- Business events
- Processing duration
- Integration calls
- Exceptions

Avoid logging

- Passwords
- Tokens
- Personally identifiable information (PII)
- Secrets

---

# Performance

Consider

- Batch processing
- Pagination
- Parallel execution (where appropriate)
- Caching
- Lazy loading
- Query optimization

Avoid

- Duplicate database calls
- Long-running transactions
- Blocking external calls without timeout

---

# Configuration

Configuration Properties

```yaml
service:
  enabled: true
  timeout: 30s
```

Environment Variables

| Variable | Description |
|----------|-------------|
| | |

---

# Monitoring

Metrics

- Request count
- Success rate
- Failure rate
- Execution time
- External service latency
- Cache hit ratio

Health Indicators

-

Alerts

-

---

# Testing

Unit Tests

-

Mock Tests

-

Integration Tests

-

Transaction Tests

-

Exception Tests

-

Performance Tests

-

Recommended Tools

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

---

# Documentation

Document

- Business rules
- Public methods
- Integration dependencies
- Known limitations
- Performance assumptions

---

# Risks

| Risk | Mitigation |
|------|------------|
| Long-running transactions | Keep transactions short |
| External dependency failures | Retry and circuit breaker |
| Business rule inconsistency | Centralized validation |
| Performance bottlenecks | Profiling and caching |

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

- Backend Implementation Standards
- Secure Coding Standards
- Repository Template
- API Implementation Standards
- Spring Framework Documentation
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| Backend Developer | | |
| Technical Lead | | |
| Solution Architect | | |
| QA Lead | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Backend Engineering Team |
# Unit_Test_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Quality Assurance Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** Unit Testing Template

---

# Unit Test Template

---

# Template Information

| Field | Value |
|---------|---------|
| Test Suite Name | |
| Class Under Test | |
| Module | |
| Package | |
| Owner | |
| Version | |
| Status | Draft / Review / Approved |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe the objective of this unit test.

Example

> Validate the SurveyService business logic independently of external systems.

---

# Business Context

Describe

- Business capability
- Tested functionality
- Expected behavior
- Critical business rules

---

# Unit Under Test

| Property | Value |
|----------|-------|
| Class | |
| Package | |
| Layer | Controller / Service / Repository / Utility / Mapper |
| Dependencies | |

---

# Scope

## Included

-

-

-

## Excluded

-

-

-

---

# Test Framework

Frameworks

- JUnit 5
- Mockito
- AssertJ
- Spring Boot Test (if required)

Coverage Tool

- JaCoCo

Build Tool

- Maven / Gradle

---

# Test Structure

Follow the **AAA Pattern**

```text
Arrange

↓

Act

↓

Assert
```

---

# Naming Convention

Pattern

```text
methodName_condition_expectedResult
```

Examples

```text
createSurvey_validRequest_returnsCreatedSurvey

deleteSurvey_existingSurvey_deletesSuccessfully

calculateScore_invalidInput_throwsValidationException
```

---

# Dependencies

Mock

- Repository
- External APIs
- AI Service
- Cache
- Event Publisher

Do Not Mock

- Value Objects
- DTOs
- Simple Utility Classes (unless justified)

---

# Test Data

## Input Data

| Variable | Value |
|----------|-------|
| | |

---

## Expected Result

Describe expected outcome.

-

-

---

# Test Scenarios

| ID | Scenario | Priority |
|----|----------|----------|
| UNIT-001 | | High |
| UNIT-002 | | Medium |
| UNIT-003 | | High |

---

# Positive Test Cases

Validate

- Successful execution
- Expected return values
- Correct state changes
- Repository interaction
- Event publication (if applicable)

---

# Negative Test Cases

Validate

- Invalid input
- Business rule violations
- Missing data
- Unauthorized operations
- Unexpected failures

---

# Edge Cases

Test

- Null values
- Empty collections
- Boundary values
- Large inputs
- Duplicate values
- Invalid formats

---

# Exception Testing

Verify

- Correct exception type
- Error message
- Error code (if applicable)

Example

```java
assertThrows(
    ValidationException.class,
    () -> service.createSurvey(request)
);
```

---

# Mocking Strategy

Use

```java
@Mock

@InjectMocks

@ExtendWith(MockitoExtension.class)
```

Verify interactions

```java
verify(repository).save(entity);

verifyNoMoreInteractions(repository);
```

---

# Assertions

Preferred Library

- AssertJ

Example

```java
assertThat(result)
    .isNotNull()
    .hasFieldOrPropertyWithValue("status", ACTIVE);
```

Avoid

- Weak assertions
- Multiple unrelated assertions in one test

---

# Test Isolation

Ensure

- Independent execution
- No shared mutable state
- No external dependencies
- Repeatable outcomes

---

# Code Coverage

Minimum Targets

| Metric | Target |
|---------|--------|
| Line Coverage | ≥90% |
| Branch Coverage | ≥85% |
| Method Coverage | ≥90% |

Critical business logic should approach 100% coverage.

---

# Performance Validation

Verify

- Fast execution
- No unnecessary object creation
- No blocking operations

Expected execution

- <100 ms per unit test (typical)

---

# Logging Validation

Verify (when applicable)

- Business events logged
- Errors logged
- Sensitive information not logged

---

# Security Validation

Validate

- Input validation
- Authorization checks
- Secure defaults
- No sensitive data exposure

---

# Test Automation

Execute

- On every commit
- Pull requests
- Nightly builds
- Release pipeline

Failure Policy

- Fail build on critical test failures
- Generate coverage reports
- Publish test results

---

# CI/CD Integration

Pipeline Steps

1. Compile
2. Execute unit tests
3. Generate coverage report
4. Perform quality gate validation
5. Publish artifacts

Quality Gates

- Coverage threshold met
- No failing tests
- Static analysis passed

---

# Test Artifacts

Collect

- Test reports
- Coverage reports
- Execution logs
- Build artifacts

---

# Pass/Fail Criteria

Pass

- All assertions succeed
- Coverage targets achieved
- No unexpected exceptions
- Static analysis passes

Fail

- Assertion failures
- Coverage below threshold
- Unhandled exceptions
- Mock verification failures

---

# Risks

| Risk | Mitigation |
|------|------------|
| Flaky tests | Eliminate shared state |
| Low coverage | Mandatory quality gates |
| Excessive mocking | Mock only external dependencies |
| False positives | Strong assertions and edge-case testing |

---

# Documentation

Document

- Test purpose
- Covered business rules
- Mocking strategy
- Known limitations
- Assumptions

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

- Testing Standards
- Backend Implementation Standards
- Service Template
- Repository Template
- JUnit 5 Documentation
- Mockito Documentation
- AssertJ Documentation
- JaCoCo Documentation
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| QA Engineer | | |
| Backend Developer | | |
| Technical Lead | | |
| Release Manager | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Quality Assurance Team |
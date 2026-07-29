# API_Design.md

> **Document Version:** 1.0
> **Status:** Draft
> **Owner:** Backend Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **API Version:** v1
> **Last Updated:** 2026-07-28

---

# API Design

---

# Document Information

| Field | Value |
|---------|---------|
| Project | AI Rural Root Cause Discovery System |
| Module | REST APIs |
| Version | 1.0 |
| API Version | v1 |
| Owner | Backend Team |

---

# Purpose

This document defines all REST APIs exposed by the AI Rural Root Cause Discovery System.

The APIs provide secure interfaces for:

- Citizen Portal
- Government Portal
- Administrator Portal
- AI Services
- Mobile Applications
- External Government Systems

---

# API Design Principles

The APIs shall:

- Follow REST principles
- Use JSON payloads
- Be stateless
- Support versioning
- Return standardized error responses
- Support pagination
- Support filtering
- Support sorting
- Use HTTPS only

---

# Base URL

Production

```
https://api.example.gov.in/api/v1
```

Development

```
http://localhost:8080/api/v1
```

---

# Authentication

Supported authentication

- JWT
- Refresh Token
- OAuth2 (Future)

Authorization

Role-Based Access Control (RBAC)

Roles

- Citizen
- Officer
- Analyst
- Administrator

---

# Common Headers

Request

```
Authorization: Bearer <token>

Content-Type: application/json

Accept: application/json

X-Correlation-ID
```

Response

```
Content-Type: application/json

X-Request-ID

X-Trace-ID
```

---

# API Groups

Authentication

Users

Villages

Surveys

Root Causes

AI Predictions

Recommendations

Analytics

Reports

Administration

Notifications

Audit

---

# Authentication APIs

## Login

POST

```
/auth/login
```

Request

```json
{
  "email": "",
  "password": ""
}
```

Response

```json
{
  "accessToken": "",
  "refreshToken": "",
  "expiresIn": 3600
}
```

---

## Refresh Token

POST

```
/auth/refresh
```

---

## Logout

POST

```
/auth/logout
```

---

# User APIs

## Get Current User

GET

```
/users/me
```

---

## Update Profile

PUT

```
/users/me
```

---

## Change Password

POST

```
/users/change-password
```

---

# Survey APIs

## Create Survey

POST

```
/surveys
```

---

## Get Survey

GET

```
/surveys/{id}
```

---

## Update Survey

PUT

```
/surveys/{id}
```

---

## Delete Survey

DELETE

```
/surveys/{id}
```

---

## Search Surveys

GET

```
/surveys
```

Query Parameters

```
page

size

sort

status

village

date
```

---

# AI Prediction APIs

## Predict Root Cause

POST

```
/predictions
```

---

## Prediction Status

GET

```
/predictions/{id}
```

---

## Prediction Explanation

GET

```
/predictions/{id}/explanation
```

---

# Recommendation APIs

GET

```
/recommendations
```

GET

```
/recommendations/{id}
```

POST

```
/recommendations/{id}/feedback
```

---

# Analytics APIs

GET

```
/analytics/dashboard
```

GET

```
/analytics/trends
```

GET

```
/analytics/root-causes
```

---

# Administration APIs

User Management

Role Management

Audit Logs

System Configuration

AI Model Management

---

# Notification APIs

Email Status

SMS Status

Push Notification Status

---

# Standard Success Response

```json
{
  "success": true,
  "data": {},
  "timestamp": "",
  "requestId": ""
}
```

---

# Standard Error Response

```json
{
  "success": false,
  "errorCode": "ERR-001",
  "message": "",
  "details": [],
  "timestamp": "",
  "requestId": "",
  "traceId": ""
}
```

---

# HTTP Status Codes

| Status | Meaning |
|---------|----------|
|200|OK|
|201|Created|
|202|Accepted|
|204|No Content|
|400|Bad Request|
|401|Unauthorized|
|403|Forbidden|
|404|Not Found|
|409|Conflict|
|422|Validation Failed|
|429|Too Many Requests|
|500|Internal Server Error|
|503|Service Unavailable|

---

# Validation Rules

Examples

Email

Password

Phone

Survey Fields

Location Coordinates

Required Fields

Business Rules

---

# Pagination

Request

```
?page=0&size=20
```

Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalPages": 10,
  "totalElements": 200
}
```

---

# Filtering

Examples

Village

District

State

Date

Status

Category

Priority

---

# Sorting

Examples

```
sort=createdAt,desc

sort=name,asc
```

---

# Rate Limiting

Authentication

20 requests/minute

Prediction API

100 requests/minute

Analytics

200 requests/minute

---

# Idempotency

Supported endpoints

POST /surveys

POST /predictions

Header

```
Idempotency-Key
```

---

# API Versioning

URI Versioning

```
/api/v1
```

Future

```
/api/v2
```

---

# Security

HTTPS Only

JWT

Input Validation

Output Encoding

Rate Limiting

CSRF Protection (where applicable)

Security Headers

---

# AI Integration

Backend communicates with AI Service using:

REST

or

gRPC

Payload includes

- Survey Data
- Engineered Features
- Metadata
- Model Version

---

# Monitoring

Monitor

- Request Rate
- Response Time
- Error Rate
- Prediction Latency
- API Availability

---

# Logging

Log

- Endpoint
- Method
- User
- Duration
- Status Code
- Correlation ID
- Trace ID

---

# OpenAPI

Maintain an OpenAPI 3.1 specification.

Generate

- Swagger UI

- SDKs

- API Documentation

---

# Testing

API Unit Tests

Integration Tests

Contract Tests

Load Tests

Security Tests

---

# Risks

| Risk | Mitigation |
|------|------------|
|API Abuse|Rate Limiting|
|Breaking Changes|Versioning|
|Unauthorized Access|JWT + RBAC|
|Slow AI Service|Async Processing|

---

# Traceability

| Requirement | Endpoint |
|-------------|----------|
|FR-001|POST /surveys|
|FR-002|POST /predictions|
|FR-003|GET /recommendations|

---

# References

- Backend Design
- System Overview
- AI Component Design
- OpenAPI Specification
- Error Handling Design
- ADRs

---

# Revision History

| Version | Date | Description |
|----------|------|-------------|
|1.0|2026-07-28|Initial Version|
# 🏛 Architecture Decision Record Log

Version: 1.0

Project: AI Rural Root Cause Discovery System

---

# Purpose

This document serves as the index of all approved Architecture Decision Records (ADRs) for the project.

Each ADR captures a significant architectural decision, the reasoning behind it, and its current status.

This log provides reviewers and contributors with a chronological view of how the system architecture has evolved.

---

# ADR Summary

| ADR ID | Decision | Status |
|----------|-----------------------------------------------|----------|
| ADR-001 | Adopt Modular System Architecture | Accepted |
| ADR-002 | Use Evidence-Driven AI Pipeline | Accepted |
| ADR-003 | Separate AI and Business Logic | Accepted |
| ADR-004 | Use REST APIs for Service Communication | Accepted |
| ADR-005 | Adopt PostgreSQL as Primary Database | Proposed |
| ADR-006 | Use JWT-Based Authentication | Proposed |
| ADR-007 | Deploy Frontend and Backend Independently | Proposed |
| ADR-008 | Store Uploaded Images in Dedicated Storage | Proposed |
| ADR-009 | Maintain Explainable Recommendation Engine | Accepted |
| ADR-010 | Version AI Models Independently | Accepted |

---

# ADR-001

## Title

Adopt Modular System Architecture

### Status

Accepted

### Context

The project contains multiple independent domains including survey management, AI inference, authentication, dashboards, and reporting.

A tightly coupled architecture would reduce maintainability.

### Decision

The system will be divided into independent modules with clearly defined responsibilities.

### Consequences

Advantages

- Easier maintenance
- Better scalability
- Independent testing
- Cleaner architecture

Trade-offs

- More interfaces
- Slightly increased project structure complexity

---

# ADR-002

## Title

Use Evidence-Driven AI Pipeline

### Status

Accepted

### Context

AI recommendations must be explainable and verifiable.

### Decision

Every recommendation must originate from verified evidence before AI inference.

Pipeline:

Survey Records
↓

GPS Clustering
↓

Complaint Similarity
↓

Image Verification
↓

Root Cause Discovery
↓

Recommendation Generation

### Consequences

Advantages

- Explainable AI
- Reviewer confidence
- Traceable recommendations

Trade-offs

- Additional preprocessing
- Slightly increased computation time

---

# ADR-003

## Title

Separate AI and Business Logic

### Status

Accepted

### Context

Mixing AI inference with business logic increases complexity and reduces maintainability.

### Decision

AI components will operate as dedicated services that receive validated inputs and return recommendations.

### Consequences

Advantages

- Easier testing
- Independent model updates
- Cleaner architecture

Trade-offs

- Additional API integration
- More service communication

---

# ADR-004

## Title

Use REST APIs

### Status

Accepted

### Context

Frontend, backend, and AI services require standardized communication.

### Decision

REST APIs will be used for service interactions.

### Consequences

Advantages

- Platform independence
- Standardized interfaces
- Easier frontend integration

Trade-offs

- Additional request overhead

---

# ADR-005

## Title

Adopt PostgreSQL as Primary Database

### Status

Proposed

### Context

The application requires reliable relational data storage with support for structured survey records and future scalability.

### Proposed Decision

Use PostgreSQL as the primary relational database.

### Reason for Proposal

Pending implementation and performance validation.

---

# ADR-006

## Title

Use JWT Authentication

### Status

Proposed

### Context

The application requires stateless authentication for secure API access.

### Proposed Decision

Use JSON Web Tokens (JWT) for user authentication and authorization.

---

# ADR-007

## Title

Independent Deployment

### Status

Proposed

### Context

Frontend, backend, and AI services should be deployable independently.

### Proposed Decision

Deploy each major component separately while maintaining secure API communication.

---

# ADR-008

## Title

Dedicated Image Storage

### Status

Proposed

### Context

Survey evidence includes uploaded images.

### Proposed Decision

Store uploaded images separately from relational data and reference them using database identifiers.

---

# ADR-009

## Title

Explainable Recommendation Engine

### Status

Accepted

### Context

Users must understand why recommendations are generated.

### Decision

Every recommendation will include supporting evidence and reasoning.

---

# ADR-010

## Title

Independent AI Model Versioning

### Status

Accepted

### Context

Multiple AI models may evolve independently.

### Decision

Each AI model will maintain:

- Version Number
- Training Date
- Evaluation Metrics
- Dataset Reference
- Change History

---

# ADR Maintenance Rules

When a new architectural decision is approved:

1. Create a new ADR.
2. Assign the next sequential identifier.
3. Record it in this log.
4. Update its status throughout its lifecycle.
5. Never delete historical ADRs.

---

# Final Principle

Architecture is a long-term asset.

Every major architectural decision should be documented, traceable, and understandable to future contributors.
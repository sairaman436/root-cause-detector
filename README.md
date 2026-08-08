# AI Rural Root Cause Discovery System

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Core Enterprise Architecture & Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Master Enterprise Repository Index & Governance Guide

---

# AI Rural Root Cause Discovery System — Enterprise Repository Index

---

# Document Information

| Field             | Value                                                                                |
| ----------------- | ------------------------------------------------------------------------------------ |
| Project Name      | AI Rural Root Cause Discovery System                                                 |
| Domain            | Artificial Intelligence, Machine Learning, Government Analytics, Root Cause Analysis |
| Repository Status | Recovery-stabilized implementation foundation with live local runtime verification   |
| Version           | 1.0                                                                                  |
| Last Updated      | 2026-07-28                                                                           |

---

# System Purpose & Vision

The **AI Rural Root Cause Discovery & Evidence-Based Decision Support System** is an enterprise-grade government decision support platform. It collects rural survey records, water quality metrics, infrastructure failure reports, and citizen complaints across agricultural districts. Using machine learning (XGBoost, Multi-Criteria Decision Analysis, TreeSHAP), the system discovers root cause patterns and generates transparent, explainable recommendations for government officials to allocate public works resources effectively.

### Core Architecture Philosophy:

> **"Evidence Before Intelligence"**  
> AI models serve to illuminate verified ground evidence for human decision makers, ensuring full transparency, explainability, and human-in-the-loop governance.

---

# Master Repository Structure & Module Directory

This repository started as an enterprise documentation repository and now also contains a runnable implementation monorepo. The current source of truth for verified implementation status is:

- `CURRENT_STATE.md`
- `FOUNDATION_RECOVERY_REPORT.md`
- `docs/operations/*`
- `docs/architecture/RAG_ARCHITECTURE.md`
- `docs/architecture/RAG_IMPLEMENTATION_REPORT.md`

Do not treat older milestone or target-state documentation as proof of implemented functionality unless it matches runnable code, tests, migrations, and configuration.

The current RAG implementation supports trusted document ingestion, deterministic chunk embeddings, Qdrant indexing, hybrid retrieval, citation validation, insufficient-evidence refusal, and local Qwen/Ollama answer generation through the AI inference service.

Legacy documentation modules:

```text
c:\Users\saira\OneDrive\Desktop\MyProps\CSP\
├── Requirements/                      # Module 01: System Requirements & Business Rules
├── System_Design/                     # Module 02: High & Low-Level Component Design
├── Architecture/                      # Module 03: System Topology, ADRs & Mermaid Diagrams
├── Implementation Structure/          # Module 04: Module Specifications & Coding Standards
├── governance/                        # Module 05: Engineering Constitution, DoD & Risk Register
├── Testing/                           # Module 06: Comprehensive Quality Assurance Suite
├── 07_Deployment/                     # Module 07: Release Pipelines, K8s & Terraform IaC
├── 08_Operations/                     # Module 08: Operational Runbooks, Prometheus & Incident Response
├── 09_AI_Documentation/               # Module 09: Model Cards, Feature Store & Ethics Audit
├── 10_User_Documentation/             # Module 10: End-User Guides, API Reference & FAQs
└── README.md                          # Master Repository Root README
```

---

# Documentation Module Summary

| Module Folder                                                                                                        | Description & Key Contents                                                                                                                 | Status           |
| -------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ | ---------------- |
| [Requirements](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/Requirements/readme.md)                           | Business Problem Statement, Functional Requirements (FR-001..FR-120), NFRs, Acceptance Criteria, Requirements Traceability Matrix.         | ✅ 100% Complete |
| [System_Design](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/System_Design/readme.md)                         | Component Interaction Diagrams, Database Schemas, API Endpoints, Caching Strategy, UI/UX Wireframes, UML Standards.                        | ✅ 100% Complete |
| [Architecture](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/Architecture/readme.md)                           | Clean Architecture & Modular Monolith Overview, System Topology, Diagram Standards, Architecture Decision Records (ADRs).                  | ✅ 100% Complete |
| [Implementation Structure](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/Implementation%20Structure/README.md) | Specifications for 16 backend micro-modules (Auth, Survey, AI, Report, etc.), Coding Standards, Branching Strategy.                        | ✅ 100% Complete |
| [governance](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/governance/AI%20Governance%20Rules.md)              | Project Constitution, AI Governance Rules, Engineering Principles, Risk Register, Definition of Done (DoD).                                | ✅ 100% Complete |
| [Testing](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/Testing/README.md)                                     | Test Plans, Standards, 920 Test Cases, AI Validation Suites, Disaster Recovery Failover, Performance, Security & UAT.                      | ✅ 100% Complete |
| [07_Deployment](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/07_Deployment/README.md)                         | Blue/Green Deployment Plans, GitHub Actions CI/CD Pipeline Specs, Kubernetes Manifests, Terraform IaC Modules, Rollback Protocols.         | ✅ 100% Complete |
| [08_Operations](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/08_Operations/README.md)                         | 24/7 Operational Standards, Startup/Shutdown Runbooks, DB Maintenance, AI Retraining, Prometheus Monitoring & Incident Response Playbooks. | ✅ 100% Complete |
| [09_AI_Documentation](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/09_AI_Documentation/README.md)             | Model Cards (`RC-XGB-v2`, `REC-MCDA-v1`), Feast Feature Store Spec, TreeSHAP Explainability & Algorithmic Bias/Fairness Audits.            | ✅ 100% Complete |
| [10_User_Documentation](file:///c:/Users/saira/OneDrive/Desktop/MyProps/CSP/10_User_Documentation/README.md)         | Field Officer Mobile User Guide, Government Official Decision Support Guide, Admin Manuals, OpenAPI REST Reference & FAQs.                 | ✅ 100% Complete |

---

# Enterprise Governance & Standards Alignment

All documents across this repository comply with international standards:

- **ISO/IEC 25010**: System and Software Quality Models
- **ISO/IEC 27001 / NIST SP 800-53**: Cybersecurity & Information Protection
- **ISO/IEC 42001 / NIST AI RMF 1.0**: Artificial Intelligence Management System
- **WCAG 2.1 Level AA**: Web Content Accessibility Guidelines
- **IEEE 829 / ISO/IEC 29119**: Software Testing Documentation Standards

---

# Approval

| Role                     | Name              | Date       |
| ------------------------ | ----------------- | ---------- |
| Chief Solution Architect | Marcus Vance      | 2026-07-28 |
| Operations Director      | Helen Brody       | 2026-07-28 |
| Lead Data Scientist      | Dr. Elena Rostova | 2026-07-28 |
| Chief Product Owner      | Gregory Vance     | 2026-07-28 |

---

# Revision History

| Version | Date       | Description                                  | Author                 |
| ------- | ---------- | -------------------------------------------- | ---------------------- |
| 1.0     | 2026-07-28 | Master Repository Completion & Final Signoff | Core Architecture Team |

---

# End of Document

---

# Implementation Monorepo Bootstrap

> **Milestone:** Foundation Recovery
> **Status:** Runtime-verified local MVP foundation
> **Purpose:** Establish the production-ready engineering foundation around the approved documentation repository.  
> **Why it exists:** The project is transitioning from architecture and planning into implementation. The existing documentation remains source-of-truth, and the monorepo now includes the runtime, tooling, CI/CD, configuration, testing, deployment foundation, and identity platform boundary.  
> **Architecture fit:** This implements the approved modular monorepo foundation with identity, survey, survey submission, evidence, deterministic AI/RAG integration boundaries, decision analysis, reporting, eventing, and local Docker Compose runtime verification.

## Canonical Implementation Structure

```text
apps/                 # Next.js user-facing applications
services/             # Spring Boot backend and Python AI service boundaries
packages/             # Shared contracts and governed libraries
data-platform/        # Data ingestion, quality, lineage, lake, catalog, warehouse
ml-platform/          # Feature store, MLOps, prompts, guardrails, evaluation
infra/                # Terraform and infrastructure policy boundaries
deploy/               # Kubernetes, Helm, environment overlays, config templates
docs/                 # Implementation-era canonical docs
tests/                # Cross-system verification suites
tools/                # Future non-runtime engineering tools
scripts/              # Local developer workflow automation
config/               # Non-secret env templates and secrets strategy docs
```

## Implemented Foundation Scope

- Repository structure
- Spring Boot backend build foundation
- Next.js frontend build foundation
- Python AI service dependency foundations
- Shared package boundaries
- Docker and Docker Compose configuration
- Environment templates
- GitHub Actions CI/CD layout
- Pre-commit, linting, formatting, testing configuration
- Documentation and developer workflow structure
- Identity REST API foundation
- PostgreSQL/Flyway identity and audit schemas
- JWT authentication, refresh tokens, BCrypt password hashing, RBAC, audit logging, validation, exception handling, and OpenAPI documentation
- Survey REST API foundation
- PostgreSQL/Flyway survey schema for templates, surveys, versions, sections, questions, options, validation rules, assignments, status history, and tags
- Dynamic question-type registry, survey workflow validation, survey search, survey assignment, questionnaire structure management, and survey audit integration
- Evidence REST API foundation
- PostgreSQL/Flyway evidence schema for evidence, metadata, versions, tags, and evidence audit events
- Local filesystem storage adapter behind a cloud-ready storage interface
- Evidence upload validation, checksum duplicate detection, filename sanitization, metadata lifecycle, search, download authorization, soft delete, restore, version history, and audit integration
- Survey submission persistence and APIs
- Local Ollama/Qwen root-cause analysis through a provider-neutral AI inference service
- Strict structured rural analysis validation and durable `ai.llm_analysis_results` metadata
- Sprint 1 web dashboard workflow for login, survey creation, survey submission, evidence upload, AI/RAG analysis, decision analysis, and report generation
- Reporting MVP with PDF and CSV downloads
- Docker Compose startup for backend, frontend portals, Python services, PostgreSQL, Redis, Redpanda, MinIO, Qdrant, Ollama, and Prometheus

## Explicitly Out of Scope

- Agent workflows
- Production model training, fine-tuning, optimization, and certified model serving
- Production vector-backed RAG beyond the current integration-ready/local boundary
- OCR, image analysis, and embedding generation pipelines
- Browser E2E, load, soak, and cloud deployment certification

## Production Release Candidate RC1

> **Release:** 1.0.0-rc.1
> **Purpose:** Captures the hardening baseline for the Sprint 1 MVP before enterprise production certification.
> **Why it exists:** RC1 separates runnable MVP capability from production release governance, security review, performance evidence, and operational acceptance.
> **Architecture fit:** RC1 preserves the approved modular monorepo and adds release controls, runtime safeguards, validation reports, and operator checklists without adding new business functionality.

RC1 documentation is maintained in `docs/operations`:

- `RC1_RELEASE_CANDIDATE_REPORT.md`
- `RC1_ARCHITECTURE_REVIEW_REPORT.md`
- `RC1_SECURITY_REPORT.md`
- `RC1_PERFORMANCE_REPORT.md`
- `RC1_TESTING_REPORT.md`
- `RC1_AI_READINESS_REPORT.md`
- `RC1_PRODUCTION_READINESS_REPORT.md`
- `RC1_TECHNICAL_DEBT_REPORT.md`
- `RC1_KNOWN_LIMITATIONS.md`
- `RC1_REMAINING_BACKLOG.md`
- `RC1_RELEASE_NOTES.md`
- `RC1_MIGRATION_GUIDE.md`
- `RC1_ROLLBACK_GUIDE.md`
- `RC1_DEPLOYMENT_CHECKLIST.md`
- `RC1_PRODUCTION_CHECKLIST.md`

Local LLM operational documentation:

- `LOCAL_LLM_INTEGRATION_REPORT.md`
- `docs/operations/LOCAL_LLM_SETUP.md`

RC1 does not certify internet-facing production use. It certifies that the MVP is structured, buildable, documented, and ready for controlled release-candidate validation.

## Local Development

Start local infrastructure dependencies:

```powershell
scripts/dev-up.ps1
```

This runs `docker compose up -d --build` and starts the local platform stack.

Enable real local Qwen inference through Ollama:

```powershell
docker compose exec ollama ollama pull qwen2.5:0.5b
Invoke-RestMethod http://localhost:8101/v1/provider/health
```

The configured model can be changed with `LLM_MODEL`, and the provider boundary is selected with `LLM_PROVIDER=ollama`. The backend calls the AI inference service through `AI_INFERENCE_SERVICE_URL`; business modules do not call Ollama directly.

Stop local dependencies:

```powershell
scripts/dev-down.ps1
```

Verify foundation files:

```powershell
scripts/verify-foundation.ps1
```

Run the full operational foundation build:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-all.ps1
```

Core verification commands used during recovery:

```powershell
npm.cmd audit --audit-level=high
npm.cmd run typecheck
npm.cmd run lint --workspaces --if-present
npm.cmd run test
npm.cmd run build:frontends
.\mvnw.cmd -B -pl services/core-backend -am test
python -m pytest tests/foundation
docker compose config --quiet
docker compose up -d
```

Health endpoints:

- Core backend: `http://localhost:8080/actuator/health`
- Web portal: `http://localhost:3000/api/health`
- Admin portal: `http://localhost:3001/api/health`
- AI inference service: `http://localhost:8101/health/ready`
- RAG service: `http://localhost:8102/health/ready`
- Agent orchestrator: `http://localhost:8103/health/ready`
- Reporting service: `http://localhost:8104/health/ready`
- Notification service: `http://localhost:8105/health/ready`
- Qdrant: `http://localhost:6333/readyz`
- Ollama: `http://localhost:11434/api/tags`
- MinIO: `http://localhost:9000/minio/health/live`
- Redpanda: `http://localhost:9644/v1/status/ready`
- Prometheus: `http://localhost:9090/-/ready`

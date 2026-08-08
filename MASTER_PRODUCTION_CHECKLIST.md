# Master Production Checklist

Purpose: Defines the evidence required to declare Production Candidate v1.
Why it exists: The repository contains many completed platform and AI milestones; production readiness requires executable proof, not architecture intent alone.
Architecture fit: This checklist ties the `.ceos` operating system, backend modules, frontend portals, Python AI services, data platform, security controls, CI/CD, and documentation into a single release gate.

Related documents: [MASTER_ARCHITECTURE_REPORT.md](MASTER_ARCHITECTURE_REPORT.md), [MASTER_TECHNICAL_DEBT.md](MASTER_TECHNICAL_DEBT.md), [MASTER_REFACTOR_PLAN.md](MASTER_REFACTOR_PLAN.md), `.ceos/constitutions/release-constitution.md`, `.ceos/playbooks/incident-response-playbook.md`.

Status legend:

- Done: Implemented and validated in the repository.
- Partial: Implemented but missing proof, coverage, or production hardening.
- Not Started: Required for production but not yet implemented.
- Blocked: Cannot complete without external dependency, credential, environment, or board decision.

## Release Gate Summary

| Gate                         | Status  | Required Before PC v1                                                                    |
| ---------------------------- | ------- | ---------------------------------------------------------------------------------------- |
| Repository structure         | Done    | Keep generated artifacts untracked.                                                      |
| Backend build and tests      | Partial | Enforce in CI on push and PR.                                                            |
| Frontend build and tests     | Partial | Enforce in CI on push and PR; add E2E tests for real workflows.                          |
| Python services tests        | Partial | Enforce tests, lint, and type checks across all Python services.                         |
| Docker Compose config        | Done    | Continue validating in CI.                                                               |
| Production secrets           | Partial | Add production fail-fast validation and external secret provider wiring.                 |
| Tenant isolation             | Partial | Enforce consistently across all tenant-scoped modules.                                   |
| API contracts                | Partial | Generate and diff OpenAPI/client contracts.                                              |
| Database migrations          | Partial | Add PostgreSQL Testcontainers migration proof.                                           |
| Observability                | Partial | Add SLO dashboards and alert rules per bounded context.                                  |
| Security scanning            | Partial | Add SBOM, dependency, license, and secret scan evidence.                                 |
| AI production runtime        | Partial | Require signed model artifacts and live serving validation before production AI traffic. |
| Documentation reconciliation | Partial | Add current-state labels and milestone evidence index.                                   |

## Architecture Checklist

| Item                                                  | Status  | Evidence Required                                                               | Owner                |
| ----------------------------------------------------- | ------- | ------------------------------------------------------------------------------- | -------------------- |
| Bounded contexts match approved architecture.         | Done    | Package map and controller inventory.                                           | Architecture Board   |
| No cross-module dependency cycles.                    | Partial | Automated dependency rule check.                                                | Architecture Board   |
| Public APIs are versioned consistently.               | Partial | `/api/v1` contract inventory and alias policy.                                  | API Board            |
| ADRs exist for significant implementation deviations. | Partial | ADR index updated for deferred integrations, aliases, and production hardening. | Architecture Board   |
| Shared contracts are authoritative.                   | Partial | OpenAPI and event schema generation in CI.                                      | Platform Engineering |

## Backend Checklist

| Item                                      | Status      | Evidence Required                                                            | Owner        |
| ----------------------------------------- | ----------- | ---------------------------------------------------------------------------- | ------------ |
| Spring Boot backend builds independently. | Done        | Maven test/package success.                                                  | Backend Lead |
| All controllers use DTO contracts.        | Partial     | Static scan and API tests.                                                   | Backend Lead |
| Stable pagination envelope used.          | Not Started | `PageResponse<T>` adopted across paged endpoints.                            | Backend Lead |
| Global error model is consistent.         | Partial     | Contract tests for validation, auth, not found, conflict, and server errors. | Backend Lead |
| Read endpoints are side-effect free.      | Partial     | Service audit and tests for GET idempotency.                                 | Backend Lead |

## Frontend Checklist

| Item                                | Status      | Evidence Required                                                         | Owner         |
| ----------------------------------- | ----------- | ------------------------------------------------------------------------- | ------------- |
| Web portal builds and typechecks.   | Partial     | CI evidence on push and PR.                                               | Frontend Lead |
| Admin portal builds and typechecks. | Partial     | CI evidence on push and PR.                                               | Frontend Lead |
| Generated API client is used.       | Not Started | Client package generated from OpenAPI.                                    | Frontend Lead |
| E2E tests cover critical workflows. | Not Started | Browser tests for auth, survey, evidence, geospatial, and AI admin flows. | QA Lead       |
| Accessibility baseline exists.      | Not Started | Automated accessibility scan and manual review.                           | Frontend Lead |

## Data Checklist

| Item                                             | Status      | Evidence Required                         | Owner           |
| ------------------------------------------------ | ----------- | ----------------------------------------- | --------------- |
| Flyway migrations apply from clean database.     | Partial     | PostgreSQL Testcontainers migration job.  | Data Lead       |
| Foreign keys and indexes exist for core domains. | Done        | Migration review.                         | Data Lead       |
| High-volume tables have partitioning.            | Not Started | Partition migrations and retention tests. | Data Lead       |
| Structured JSON fields use JSONB where queried.  | Partial     | Column classification and migration plan. | Data Lead       |
| Data retention and archival jobs exist.          | Not Started | Scheduled job implementation and runbook. | Data Governance |
| Data lineage metadata is queryable.              | Partial     | Lineage endpoint/report evidence.         | Data Governance |

## Event Checklist

| Item                                       | Status  | Evidence Required                                                             | Owner                |
| ------------------------------------------ | ------- | ----------------------------------------------------------------------------- | -------------------- |
| Outbox pattern exists.                     | Done    | Schema and service tests.                                                     | Platform Engineering |
| Kafka local topology exists.               | Done    | Docker Compose validation.                                                    | Platform Engineering |
| Event schema compatibility is tested.      | Partial | Versioned schema registry checks.                                             | Platform Engineering |
| Dead-letter replay is operator-safe.       | Partial | Replay tests and authorization review.                                        | Platform Engineering |
| Integration monitor consumers are labeled. | Partial | Runtime health and docs explicitly identify deferred downstream integrations. | Platform Engineering |

## AI Checklist

| Item                                                                                                                                     | Status      | Evidence Required                                                                                         | Owner            |
| ---------------------------------------------------------------------------------------------------------------------------------------- | ----------- | --------------------------------------------------------------------------------------------------------- | ---------------- |
| Dataset, knowledge, training, fine-tuning, evaluation, optimization, learning, serving, governance, release, and research modules exist. | Done        | Module and schema inventory.                                                                              | AI Platform Lead |
| AI endpoints identify runtime classification.                                                                                            | Not Started | Response metadata and docs: control-plane, deterministic-local, deferred-integration, production-runtime. | AI Platform Lead |
| Production model artifacts are signed and registered.                                                                                    | Blocked     | Real model artifacts and registry records.                                                                | MLOps Lead       |
| Prompt injection tests run in CI.                                                                                                        | Partial     | Guardrail test suite.                                                                                     | AI Security Lead |
| Hallucination/citation validation is enforced.                                                                                           | Partial     | Evaluation and serving integration tests.                                                                 | AI Governance    |
| GPU and provider integration is certified.                                                                                               | Blocked     | External runtime environment and benchmark evidence.                                                      | MLOps Lead       |

## Security Checklist

| Item                                            | Status      | Evidence Required                     | Owner             |
| ----------------------------------------------- | ----------- | ------------------------------------- | ----------------- |
| Authentication and RBAC exist.                  | Done        | Identity tests and endpoint policies. | Security Lead     |
| Production weak-secret startup is blocked.      | Not Started | Production profile validation test.   | Security Lead     |
| Endpoint permissions are covered by tests.      | Partial     | Permission matrix test.               | Security Lead     |
| Tenant isolation is enforced.                   | Partial     | Cross-tenant access tests.            | Security Lead     |
| Secret scan is configured and low-noise.        | Partial     | Baseline and allowlist.               | Security Lead     |
| SBOM and dependency scan are release artifacts. | Partial     | CI artifacts.                         | Supply Chain Lead |
| Incident response runbook is current.           | Partial     | Drill report.                         | SRE Lead          |

## Observability Checklist

| Item                           | Status      | Evidence Required                                           | Owner             |
| ------------------------------ | ----------- | ----------------------------------------------------------- | ----------------- |
| Health endpoints exist.        | Done        | Actuator and service health checks.                         | SRE Lead          |
| Structured logging exists.     | Partial     | Log schema test and dashboard.                              | SRE Lead          |
| Metrics are exposed.           | Partial     | Prometheus scrape and dashboard evidence.                   | SRE Lead          |
| Distributed tracing is active. | Partial     | Trace propagation proof across backend and Python services. | SRE Lead          |
| Alerts map to SLOs.            | Not Started | Alert rules and SLO document.                               | SRE Lead          |
| Business KPIs are tracked.     | Not Started | KPI dashboard and data source ownership.                    | Product Analytics |

## DevOps Checklist

| Item                                                     | Status      | Evidence Required                               | Owner                |
| -------------------------------------------------------- | ----------- | ----------------------------------------------- | -------------------- |
| Dockerfiles exist for services.                          | Done        | Docker build validation.                        | Platform Engineering |
| Compose topology validates.                              | Done        | `docker compose config --quiet`.                | Platform Engineering |
| Kubernetes manifests/Helm overlays are production-ready. | Partial     | Environment-specific dry-run and policy checks. | Platform Engineering |
| IaC plans validate.                                      | Partial     | Terraform plan evidence per environment.        | Platform Engineering |
| Release pipeline creates signed artifacts.               | Partial     | Release workflow artifact evidence.             | Release Engineering  |
| Rollback procedure is tested.                            | Not Started | Drill result and automated rollback test.       | Release Engineering  |

## Testing Checklist

| Item                                      | Status      | Evidence Required                                               | Owner              |
| ----------------------------------------- | ----------- | --------------------------------------------------------------- | ------------------ |
| Unit tests exist across backend modules.  | Done        | Maven test report.                                              | QA Lead            |
| Integration tests cover critical modules. | Partial     | PostgreSQL, Kafka, storage, and auth integration tests.         | QA Lead            |
| Contract tests exist.                     | Not Started | OpenAPI and event contract tests.                               | QA Lead            |
| Security tests exist.                     | Partial     | RBAC, tenant, prompt, secret, and dependency tests.             | Security Lead      |
| Performance tests exist.                  | Not Started | Load and soak suite with thresholds.                            | Performance Lead   |
| AI evaluation tests exist.                | Partial     | Evaluation module tests plus live model gate before production. | AI Evaluation Lead |

## Documentation Checklist

| Item                             | Status  | Evidence Required                   | Owner               |
| -------------------------------- | ------- | ----------------------------------- | ------------------- |
| `.ceos` operating system exists. | Done    | Constitution and playbook index.    | TPMO                |
| Architecture docs exist.         | Done    | Architecture and ADR directories.   | Architecture Board  |
| Docs match implementation state. | Partial | Target/current-state labels.        | Documentation Lead  |
| Operations docs exist.           | Partial | Runbooks validated by drills.       | SRE Lead            |
| Release docs exist.              | Partial | Release candidate evidence package. | Release Engineering |

## Production Candidate Exit Criteria

Production Candidate v1 may be declared only when:

1. All P0 debt is closed.
2. All P1 debt is closed or formally risk-accepted with dated remediation.
3. CI required checks pass on `main`.
4. Production configuration fails closed for unsafe defaults.
5. Tenant isolation is verified for all tenant-scoped records.
6. PostgreSQL migrations are validated from a clean database.
7. API and event contracts are generated and compatibility checked.
8. Observability dashboards and alerts cover critical workflows.
9. AI endpoints are clearly classified and production AI routes have signed artifact evidence.
10. Release Board, Security Board, Architecture Board, Data Governance Board, AI Governance Board, and SRE Board approve the evidence package.

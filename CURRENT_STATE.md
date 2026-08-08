# Current Repository State

Purpose: Records the verified post-recovery state of the Rural Intelligence Platform.
Why it exists: The repository has moved through multiple milestone-style implementation passes; this file is the current evidence-based handoff before further feature work.
Architecture fit: Tracks runnable code, tests, schemas, dependency state, and runtime blockers without redesigning the approved architecture.

Assessment date: 2026-08-08
Branch: `main`
Baseline commit before recovery: `b8357cb Prepare RC1 release candidate`
Recovery status: `STABILIZED_RUNTIME_VERIFIED`

## Executive Summary

The repository is now a stable development foundation for the MVP workflow. The recovery pass fixed the high-severity npm audit blocker, added durable survey submission support, connected the web portal to the missing survey submission step, and added a cross-module secured API recovery test for:

`Authentication -> Survey -> Survey Submission -> Evidence Upload -> RAG Retrieval -> Root Cause Decision -> PDF/CSV Report`

All local validation gates now pass, including Docker image builds, full Docker Compose startup, live PostgreSQL migration verification, service health checks, and a live API workflow through the running stack.

Evidence-based implementation percentage after recovery: **68%**

This score reflects the resolved dependency blocker, resolved Docker runtime blocker, verified Compose startup, live PostgreSQL migration proof, one core MVP data-flow gap closed, and one cross-module recovery test added. It does not claim production readiness because browser E2E, load testing, production vector RAG, and production model serving remain incomplete.

## Repository Inventory

| Area                      | Current state                                                                                            |
| ------------------------- | -------------------------------------------------------------------------------------------------------- |
| Spring controllers        | 30                                                                                                       |
| JPA entities              | 191                                                                                                      |
| Spring repositories       | 186                                                                                                      |
| Backend Java test classes | 43                                                                                                       |
| Flyway migrations         | 23                                                                                                       |
| Frontend apps             | `apps/web-portal`, `apps/admin-portal`                                                                   |
| Python services           | `ai-inference-service`, `rag-service`, `agent-orchestrator`, `notification-service`, `reporting-service` |
| Shared Java event package | `packages/java-shared`                                                                                   |

## Implemented During Recovery

1. Dependency security recovery
   - Upgraded both Next.js frontends to `next@16.3.0`.
   - Removed stale package-level Next 14 SWC optional dependency pins.
   - Refreshed `package-lock.json`.
   - Verified `npm.cmd audit --audit-level=high` returns `found 0 vulnerabilities`.

2. Survey submission core flow
   - Added Flyway migration `V23__survey_submission_core_flow.sql`.
   - Added `survey.survey_submissions`.
   - Added `survey.survey_submission_answers`.
   - Added JPA entities and repository for persisted submissions.
   - Added REST APIs to submit, list, and retrieve survey submissions.
   - Added required-question validation and survey-status validation.
   - Added audit entry and canonical `survey.submitted` event topic.

3. MVP dashboard recovery
   - Removed the hardcoded demo password from portal state.
   - Updated the web portal workflow to create section/question metadata, move the survey through review/approval/publish, and submit a response before evidence upload.
   - Evidence upload now includes the survey question reference when available.

4. Recovery testing
   - Extended survey integration tests to verify submission create/list/get.
   - Added `CoreWorkflowRecoveryIntegrationTests` to prove the secured cross-module MVP API flow.

## Verified Gates

| Gate                                               | Result                                       |
| -------------------------------------------------- | -------------------------------------------- |
| `npm.cmd audit --audit-level=high`                 | PASS, 0 vulnerabilities                      |
| `npm.cmd run typecheck`                            | PASS                                         |
| `npm.cmd run lint --workspaces --if-present`       | PASS                                         |
| `npm.cmd run test`                                 | PASS, 2 frontend tests                       |
| `npm.cmd run build:frontends`                      | PASS, web and admin production builds        |
| `.\mvnw.cmd -B -pl services/core-backend -am test` | PASS, 107 backend/shared tests               |
| Python service pytest loop                         | PASS, 10 service tests                       |
| `python -m pytest tests/foundation`                | PASS, 4 foundation tests                     |
| `docker compose config --quiet`                    | PASS                                         |
| `docker version`                                   | PASS, Docker Desktop Linux engine responding |
| `docker compose build ...application services...`  | PASS, backend, portals, and Python images    |
| `docker compose up -d`                             | PASS, full local stack starts                |
| Live runtime core API workflow                     | PASS, report PDF/CSV returned HTTP 200       |

## Current Runtime Status

| Runtime                                      | Status                 | Notes                                                                                   |
| -------------------------------------------- | ---------------------- | --------------------------------------------------------------------------------------- |
| Spring Boot backend                          | WORKING under test     | H2 PostgreSQL-compatibility tests start the Spring context and apply all 23 migrations. |
| Web portal                                   | WORKING build artifact | Production Next.js build succeeds; browser E2E not yet implemented.                     |
| Admin portal                                 | WORKING build artifact | Production Next.js build succeeds.                                                      |
| Python services                              | WORKING under tests    | Five FastAPI service health suites pass.                                                |
| Docker Compose topology                      | WORKING                | Full stack starts locally.                                                              |
| Docker runtime                               | WORKING                | Docker Desktop Linux engine responds and runs containers.                               |
| PostgreSQL runtime                           | VERIFIED               | Live container has Flyway V23 applied successfully.                                     |
| PostGIS runtime                              | NOT VERIFIED           | Current Compose database is PostgreSQL; PostGIS-specific proof remains future work.     |
| Kafka/Redpanda, Redis, Qdrant, MinIO, Ollama | WORKING                | Compose services start and readiness endpoints respond where available.                 |

## Live Runtime Workflow Evidence

The running Compose stack successfully executed:

`Register User -> Create Survey -> Create Section -> Create Question -> Publish Survey -> Submit Survey -> Upload Evidence -> Retrieve Survey -> RAG Query -> Decision Analysis -> Generate Report -> Download PDF -> Download CSV`

Observed result:

| Step                     | Result               |
| ------------------------ | -------------------- |
| Authentication           | PASS                 |
| Survey status            | `PUBLISHED`          |
| Submission status        | `SUBMITTED`          |
| Evidence type            | `GENERIC_ATTACHMENT` |
| RAG citations            | 3                    |
| Decision recommendations | 3                    |
| Report type              | `EXECUTIVE`          |
| PDF download             | HTTP 200             |
| CSV download             | HTTP 200             |

## Remaining P0/P1 Work

1. Add browser E2E coverage for the dashboard path.
2. Add PostgreSQL/Testcontainers migration verification for the 23 Flyway migrations in CI.
3. Add endpoint-permission contract tests for the broad Spring Security mapping.
4. Integrate production Qdrant-backed RAG before expanding AI claims.
5. Validate Kubernetes/Terraform deployment in a real environment.
6. Add load/performance test execution reports.

## Final State

The foundation is stable and reproducible for continued development. The next work should be test hardening and production-readiness proof, not broad new feature expansion.

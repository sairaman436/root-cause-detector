# Bootstrap Validation

## Purpose

Records the current repository bootstrap and recovery validation status.

## Why It Exists

The platform must not proceed from recovery into feature expansion unless the foundation is buildable, testable, runnable, integrated, and documented against actual implementation evidence.

## Architecture Fit

This document supports the approved Engineering Design Specification by turning repository bootstrap and recovery work into explicit quality gates.

## Validation Outcome - 2026-08-08

The repository foundation is runtime-verified for local development. The prior dependency-security and Docker-runtime blockers have been resolved.

Current source-of-truth recovery documents:

- `CURRENT_STATE.md`
- `FOUNDATION_RECOVERY_REPORT.md`

## Checks Passed

- Root formatting: `npm.cmd run format:check`
- Frontend linting: `npm.cmd run lint --workspaces --if-present`
- Frontend type checking: `npm.cmd run typecheck`
- Frontend foundation tests: `npm.cmd run test`
- Frontend production builds: `npm.cmd run build:frontends`
- Frontend dependency security: `npm.cmd audit --audit-level=high`
- Backend test build: `.\mvnw.cmd -B -pl services/core-backend -am test`
- Python service tests across five FastAPI services
- Repository foundation tests: `python -m pytest tests/foundation`
- Docker Compose validation: `docker compose config --quiet`
- Application image builds for backend, portals, and Python services
- Full Docker Compose startup
- Live PostgreSQL Flyway verification through V23
- Live core API workflow through the running stack

## Improvements Applied

- Upgraded both Next.js portals to `16.3.0`.
- Removed stale Next 14 SWC optional dependency pins.
- Added durable survey submission schema, entities, repository, service, controller endpoints, DTOs, and integration tests.
- Added canonical `survey.submitted` event topic.
- Added a cross-module runtime recovery integration test.
- Updated the web dashboard to include the missing survey submission step.
- Fixed Next standalone container binding by launching portal servers with `HOSTNAME=0.0.0.0`.
- Fixed portal Compose health checks to use `127.0.0.1` for container-local checks.

## Runtime Outcome

The local stack starts with:

- Spring Boot backend
- Web portal
- Admin portal
- AI inference service
- RAG service
- Agent orchestrator
- Reporting service
- Notification service
- PostgreSQL
- Redis
- Redpanda
- MinIO
- Qdrant
- Ollama
- Prometheus

The live core workflow completed:

`Register User -> Create Survey -> Publish Survey -> Submit Survey -> Upload Evidence -> Retrieve Survey -> RAG Query -> Decision Analysis -> Generate Report -> Download PDF -> Download CSV`

## Remaining Exceptions

- Browser E2E tests are not implemented yet.
- CI Testcontainers coverage for PostgreSQL migration parity is still missing.
- PostGIS-specific proof remains incomplete.
- Production vector-backed RAG remains incomplete.
- Production model training, fine-tuning, optimization, and certified serving are outside the current implemented runtime.
- Load, soak, and capacity tests remain unexecuted.

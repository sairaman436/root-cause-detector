# Foundation Recovery Report

Purpose: Documents the senior engineering recovery work performed on the Rural Intelligence Platform foundation.
Why it exists: The team needed P0/P1 blockers fixed and a reproducible evidence trail before resuming feature development.
Architecture fit: Records stabilization work only; no architecture redesign or unrelated feature expansion was performed.

Date: 2026-08-08
Branch: `main`
Baseline commit before recovery: `b8357cb Prepare RC1 release candidate`
Recovery outcome: `PASS_RUNTIME_VERIFIED`

## Scope

The recovery pass focused on the approved foundation path:

`User -> Authentication -> Dashboard -> Create Survey -> Submit Survey -> Upload Evidence -> Persist Data -> Retrieve Survey -> RAG Retrieval -> Root Cause Analysis -> Recommendations -> PDF/CSV Report`

No new AI training, autonomous agents, large new product features, or architecture redesign was introduced.

## Blockers Addressed

| Blocker                                    | Severity | Action                                                                                     | Result                                       |
| ------------------------------------------ | -------- | ------------------------------------------------------------------------------------------ | -------------------------------------------- |
| High npm vulnerabilities                   | P0       | Upgraded Next.js to `16.3.0`, removed stale SWC pins, refreshed lockfile                   | Fixed; `npm audit` reports 0 vulnerabilities |
| Missing survey submission persistence/API  | P1       | Added schema, JPA entities, repository, service methods, DTOs, controller endpoints        | Fixed and tested                             |
| MVP dashboard missing submit-survey step   | P1       | Added minimal published-survey creation and response submission flow                       | Fixed and frontend build verified            |
| No single cross-module core workflow proof | P1       | Added `CoreWorkflowRecoveryIntegrationTests`                                               | Fixed; secured API workflow passes           |
| Docker runtime unavailable                 | P0       | Started Docker Desktop, built app images, pulled infrastructure images, started stack      | Fixed; full Compose stack starts             |
| Portal container health checks             | P1       | Bound Next standalone servers to `0.0.0.0` and changed portal health checks to `127.0.0.1` | Fixed; portal containers report healthy      |

## Implementation Summary

### Backend

Added durable survey submission support:

- `survey.survey_submissions`
- `survey.survey_submission_answers`
- `SurveySubmissionEntity`
- `SurveySubmissionAnswerEntity`
- `SurveySubmissionRepository`
- `POST /api/v1/surveys/{surveyId}/submissions`
- `GET /api/v1/surveys/{surveyId}/submissions`
- `GET /api/v1/surveys/{surveyId}/submissions/{submissionId}`

The submission service validates that:

- The survey exists.
- The survey is `PUBLISHED` or `ACTIVE`.
- The survey has at least one question.
- Submitted answers reference questions belonging to the survey.
- Required questions have non-blank answers.

The backend records audit activity and publishes the canonical `survey.submitted` event topic through the existing outbox event mechanism.

### Frontend

Updated the web portal MVP workflow:

- No hardcoded password is prefilled.
- `Create Survey` now creates a minimal survey definition, section, and required question.
- The workflow transitions the survey through `REVIEW`, `APPROVED`, and `PUBLISHED`.
- The survey page now submits a survey response.
- Evidence upload includes the question reference when available.

### Dependency Management

Updated both Next.js frontends:

- `apps/web-portal`
- `apps/admin-portal`

The resolved lockfile now uses Next.js `16.3.0` and matching `@next/swc-*` packages.

### Runtime

Verified Docker runtime:

- Docker Desktop Linux engine responds.
- Application images build successfully.
- Full Compose stack starts.
- Backend is healthy.
- Web portal and admin portal are healthy.
- Python services are healthy.
- PostgreSQL is healthy and has Flyway V23 applied.
- Qdrant, Redpanda, MinIO, Prometheus, Ollama, and Redis start successfully.

## Database Changes

Migration: `services/core-backend/src/main/resources/db/migration/V23__survey_submission_core_flow.sql`

Tables:

- `survey.survey_submissions`
- `survey.survey_submission_answers`

Indexes:

- Survey/submitted-at lookup
- Organization/submitted-at lookup
- User/submitted-at lookup
- Question answer lookup

Constraints:

- Submission belongs to a survey.
- Answer belongs to a submission.
- Answer references a survey question.
- One answer per question per submission.

## API Changes

| Method | Path                                                    | Purpose                        |
| ------ | ------------------------------------------------------- | ------------------------------ |
| `POST` | `/api/v1/surveys/{surveyId}/submissions`                | Submit a survey response       |
| `GET`  | `/api/v1/surveys/{surveyId}/submissions`                | List survey submissions        |
| `GET`  | `/api/v1/surveys/{surveyId}/submissions/{submissionId}` | Retrieve one survey submission |

Security follows existing survey RBAC mappings. Mutation is protected by survey management authority; reads are protected by survey read authority.

## Test Evidence

| Command                                                                                                                                                | Result                         |
| ------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------ |
| `npm.cmd audit --audit-level=high`                                                                                                                     | PASS, 0 vulnerabilities        |
| `npm.cmd run typecheck`                                                                                                                                | PASS                           |
| `npm.cmd run lint --workspaces --if-present`                                                                                                           | PASS                           |
| `npm.cmd run test`                                                                                                                                     | PASS, 2 frontend tests         |
| `npm.cmd run build:frontends`                                                                                                                          | PASS                           |
| `.\mvnw.cmd -B -pl services/core-backend -am -Dtest=CoreWorkflowRecoveryIntegrationTests '-Dsurefire.failIfNoSpecifiedTests=false' test`               | PASS, 1 recovery test          |
| `.\mvnw.cmd -B -pl services/core-backend -am test`                                                                                                     | PASS, 107 backend/shared tests |
| Python service pytest loop                                                                                                                             | PASS, 10 service tests         |
| `python -m pytest tests/foundation`                                                                                                                    | PASS, 4 tests                  |
| `docker compose config --quiet`                                                                                                                        | PASS                           |
| `docker version`                                                                                                                                       | PASS                           |
| `docker compose build ai-inference-service rag-service agent-orchestrator reporting-service notification-service core-backend web-portal admin-portal` | PASS                           |
| `docker compose up -d`                                                                                                                                 | PASS                           |
| Live runtime API workflow                                                                                                                              | PASS                           |

## Live Runtime API Result

The running stack completed the core workflow with these observed outputs:

| Signal                   | Value                |
| ------------------------ | -------------------- |
| Auth                     | `PASS`               |
| Survey status            | `PUBLISHED`          |
| Submission status        | `SUBMITTED`          |
| Evidence type            | `GENERIC_ATTACHMENT` |
| RAG citations            | 3                    |
| Decision recommendations | 3                    |
| Report type              | `EXECUTIVE`          |
| PDF status               | 200                  |
| CSV status               | 200                  |

## Known Remaining Risks

1. Browser E2E tests are still not implemented.
2. PostgreSQL runtime migrations are verified locally through Compose, but CI Testcontainers coverage is still missing.
3. PostGIS-specific migration and spatial query proof remains incomplete.
4. Production vector-backed RAG remains incomplete.
5. Production model serving and AI governance claims remain control-plane/local-fallback level.
6. Load, soak, and capacity tests remain unexecuted.

## Recovery Decision

The repository foundation is stable enough for continued engineering. The highest remaining risks are production-grade E2E, CI database parity, PostGIS proof, performance validation, and production AI runtime maturity.

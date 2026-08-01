# Sprint 1 MVP Release Report

Purpose: Documents the executable Sprint 1 end-to-end MVP implementation.
Why it exists: Engineering, QA, security, and operations need one release record showing what was implemented, how it fits the approved architecture, how to validate it, and what remains after Sprint 1.
Architecture fit: Complements the CEOS release process and links the web portal, core backend, Python AI services, PostgreSQL/Flyway, storage, AI/RAG, decision intelligence, and reporting modules.

## Scope

Sprint 1 implements the first integrated MVP workflow:

1. Login or register the first administrator.
2. Create a survey through the Survey bounded context.
3. Upload evidence through the Evidence bounded context and local storage abstraction.
4. Persist identity, survey, evidence, AI, decision, audit, and report records through PostgreSQL/Flyway schemas.
5. Trigger AI/RAG analysis through backend AI APIs and executable Python service boundaries.
6. Generate root-cause analysis and recommendations through Decision Intelligence.
7. Generate PDF and CSV reports through the new Reports bounded context.
8. View and operate the workflow through the Next.js web dashboard.

## Implemented Components

### Frontend

- `apps/web-portal/src/app/page.tsx` now contains the integrated Sprint 1 dashboard.
- Pages/views included: Dashboard, Login, Survey, Evidence Upload, Reports, AI Assistant, Settings, User Profile.
- The dashboard calls backend APIs directly with JWT bearer tokens.
- Evidence upload uses multipart form data.
- Report downloads use backend PDF and CSV endpoints.

### Backend

- Added `reports` bounded context under `services/core-backend/src/main/java/com/airural/platform/core/reports`.
- Added report generation service, report entity, repository, DTOs, controller, and stable report exception mapping.
- Added V22 Flyway migration for `reports.generated_reports`.
- Added `REPORT_READ`, `REPORT_GENERATE`, and `REPORT_ADMIN` permissions.
- Updated Spring Security to authorize report generation and download.
- Existing identity, survey, evidence, AI, and decision APIs are reused rather than duplicated.

### Python Services

- `ai-inference-service` exposes `/v1/inference` with Ollama-compatible generation and deterministic local fallback.
- `rag-service` exposes `/v1/documents` and `/v1/query` with in-memory indexing, hybrid keyword scoring, and citations.
- `agent-orchestrator` exposes `/v1/orchestrate` for the Sprint 1 survey-to-report workflow plan.
- `reporting-service` exposes `/v1/render` for CSV and text-backed PDF rendering.
- `notification-service` exposes `/v1/notifications` and records local auditable deliveries.

## API Surface

The runtime OpenAPI specification remains available at:

- `/api/v1/openapi`
- `/api/v1/docs`

Sprint 1 primary APIs:

| Capability                | Method | Endpoint                     |
| ------------------------- | ------ | ---------------------------- |
| Login                     | POST   | `/api/v1/auth/login`         |
| Register                  | POST   | `/api/v1/auth/register`      |
| Current profile           | GET    | `/api/v1/users/me`           |
| Create survey             | POST   | `/api/v1/surveys`            |
| Search surveys            | GET    | `/api/v1/surveys`            |
| Upload evidence           | POST   | `/api/v1/evidence`           |
| Search evidence           | GET    | `/api/v1/evidence`           |
| RAG query                 | POST   | `/api/v1/ai/rag/query`       |
| AI chat                   | POST   | `/api/v1/ai/chat`            |
| Decision analysis         | POST   | `/api/v1/decision/analyze`   |
| Recommendation generation | POST   | `/api/v1/decision/recommend` |
| Decision history          | GET    | `/api/v1/decision/history`   |
| Generate report           | POST   | `/api/v1/reports`            |
| List reports              | GET    | `/api/v1/reports`            |
| Download report PDF       | GET    | `/api/v1/reports/{id}/pdf`   |
| Download report CSV       | GET    | `/api/v1/reports/{id}/csv`   |

Python service APIs:

| Service               | Method | Endpoint            |
| --------------------- | ------ | ------------------- |
| AI inference          | POST   | `/v1/inference`     |
| RAG indexing          | POST   | `/v1/documents`     |
| RAG query             | POST   | `/v1/query`         |
| Agent workflow        | POST   | `/v1/orchestrate`   |
| Report rendering      | POST   | `/v1/render`        |
| Notification delivery | POST   | `/v1/notifications` |

## Database Changes

Migration: `V22__enterprise_sprint1_reporting_mvp.sql`

Schema: `reports`

Table: `reports.generated_reports`

Indexes:

- `idx_reports_decision`
- `idx_reports_survey`
- `idx_reports_organization`
- `idx_reports_type_status`

RBAC additions:

- `REPORT_READ`
- `REPORT_GENERATE`
- `REPORT_ADMIN`

## Object Storage

Evidence upload continues to use the approved storage abstraction. Local development writes evidence through the local storage provider configured by `airural.evidence.local-storage-path`. The storage interface remains ready for S3-compatible, Azure Blob, and Google Cloud Storage adapters.

## AI Integration

Sprint 1 uses two AI paths:

- Backend AI/RAG/decision modules call the Python AI/RAG service endpoints when available and persist inference logs, RAG requests, citations, token usage, decisions, root causes, recommendations, confidence, and audit records.
- Python AI service boundaries provide executable `/v1/inference` and `/v1/query` APIs. Ollama is attempted first for inference; deterministic local inference is used when Ollama is not reachable so CI and local demos remain reliable.

Trade-off: Deterministic fallback keeps the MVP executable without GPUs or downloaded models, but production AI release still requires the AI governance, model registry, signed artifact, and benchmark gates already documented in the master production checklist.

## Validation Commands

Run from repository root:

```powershell
npm run format:check
npm run typecheck
npm run test
npm run build:frontends
docker compose config --quiet
$env:JAVA_HOME='C:\Program Files\Java\jdk-24'; .\mvnw.cmd -B -pl services/core-backend -am test
$services = @('ai-inference-service','rag-service','agent-orchestrator','reporting-service','notification-service'); foreach ($svc in $services) { Push-Location "services/$svc"; python -m pytest; Pop-Location }
git diff --check
```

## Architecture Review Summary

Decision: Add a Reports bounded context rather than embedding report generation inside Decision Intelligence.

Rationale:

- Decision Intelligence owns analysis, root causes, recommendations, confidence, and traces.
- Reports own durable presentation artifacts and exports.
- This preserves DDD boundaries and keeps future asynchronous report workers compatible with the backend report record.

Trade-off:

- Adds a schema and module, but avoids coupling report formats to decision logic.

## Security Review Summary

- Report APIs are authenticated and protected by report permissions.
- First registered user remains administrator through the approved identity flow.
- Evidence upload remains RBAC-protected and audited.
- AI endpoints remain protected by existing AI/serving permissions.
- Local AI fallback is explicitly identified in service responses through `provider` and `fallback_used`.

Remaining security work:

- Production profile must still fail closed for local default secrets.
- Tenant isolation hardening remains a production-candidate item.

## Performance Review Summary

- Report records are indexed by decision, survey, organization, type, and status.
- PDF generation is lightweight text rendering and avoids extra heavy dependencies.
- RAG service uses in-memory scoring for Sprint 1 execution.

Remaining performance work:

- Replace in-memory RAG indexing with Qdrant-backed retrieval for production load.
- Add load tests for evidence upload, AI analysis, and report generation.

## QA Review Summary

Coverage added:

- Backend report integration test covers decision analysis, report generation, PDF download, and CSV download.
- Python service tests cover inference, RAG indexing/query, orchestration, report rendering, and notification delivery.
- Existing backend tests cover identity, survey, evidence, AI, decision, geospatial, and AI lifecycle modules.
- Frontend typecheck/build validates the new dashboard.

Known warnings:

- Spring still warns that direct `PageImpl` serialization is not a stable API contract. This remains tracked in `MASTER_TECHNICAL_DEBT.md`.

## Known Issues

1. RAG service uses in-memory retrieval for Sprint 1 and is not a substitute for production Qdrant-backed retrieval.
2. Python inference uses deterministic fallback when Ollama is unavailable.
3. Frontend is a practical MVP dashboard, not a full role-specific enterprise product UI.
4. Direct Spring `Page` serialization remains technical debt.
5. Production tenant isolation and weak-secret fail-fast controls remain production-candidate hardening work.

## Remaining Backlog

1. Generate static OpenAPI JSON in CI and publish it as an artifact.
2. Add Playwright E2E tests for the browser workflow against Docker Compose.
3. Replace in-memory RAG with Qdrant persistence and hybrid vector search.
4. Add S3-compatible evidence storage adapter and signed URL implementation.
5. Add report templates for branded government/NGO formats.
6. Add stable pagination envelope across all paged APIs.
7. Add production secret validation and tenant isolation enforcement.

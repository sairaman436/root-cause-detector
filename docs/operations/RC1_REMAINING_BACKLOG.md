# RC1 Remaining Backlog

Purpose: Defines the work remaining after RC1.
Why it exists: The team needs a direct continuation path from release candidate to production certification.
Architecture fit: Backlog items map to CEOS quality gates and the master debt register.

Related documents: `MASTER_TECHNICAL_DEBT.md`, `MASTER_REFACTOR_PLAN.md`, `docs/operations/RC1_PRODUCTION_CHECKLIST.md`.

## P0

- Enforce required CI checks on protected branches.
- Complete tenant isolation policy and tests.
- Produce dependency scan, SBOM, license, and provenance artifacts.
- Run security review with OWASP and OWASP LLM evidence.

## P1

- Add E2E tests for login, survey creation, evidence upload, AI analysis, RAG retrieval, report generation, and dashboard review.
- Add load and soak tests for backend, database, evidence upload, and AI paths.
- Move distributed traffic controls to gateway or service mesh.
- Add PostgreSQL Testcontainers coverage for migration-sensitive repositories.

## P2

- Generate TypeScript and Python clients from OpenAPI.
- Add production dashboards and alert rules.
- Add cloud environment overlays and disaster-recovery exercises.
- Normalize current-state labels across legacy planning documents.

# RC1 Deployment Checklist

Purpose: Provides an environment checklist for RC1 deployment.
Why it exists: RC1 deployments must be repeatable and auditable.
Architecture fit: Checklist items map to the monorepo service topology and CEOS release governance.

Related documents: `docs/operations/RC1_RELEASE_CANDIDATE_REPORT.md`, `docs/operations/RC1_PRODUCTION_CHECKLIST.md`.

## Pre-Deployment

- `VERSION` matches the release candidate.
- Required validation commands pass.
- Database backup is complete.
- Non-local secrets are configured for production-like environments.
- Object storage, PostgreSQL, Qdrant, and Ollama configuration are known.
- Release notes, migration guide, and rollback guide are reviewed.

## Deployment

- Deploy infrastructure dependencies.
- Apply Flyway migrations through backend startup.
- Start backend.
- Start Python AI services.
- Start frontend portals.
- Validate health and readiness endpoints.

## Post-Deployment

- Execute workflow smoke tests.
- Verify structured logs and audit records.
- Capture deployment evidence, commit hash, and environment metadata.
- Confirm no critical alerts are active.

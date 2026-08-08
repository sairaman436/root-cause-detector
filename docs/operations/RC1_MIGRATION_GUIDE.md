# RC1 Migration Guide

Purpose: Describes how to move an existing Sprint 1 MVP environment to RC1.
Why it exists: Operators need a safe path from MVP validation to release-candidate validation.
Architecture fit: The guide preserves existing service boundaries and database migrations.

Related documents: `docs/operations/RC1_RELEASE_NOTES.md`, `docs/operations/RC1_ROLLBACK_GUIDE.md`, `docs/operations/RC1_DEPLOYMENT_CHECKLIST.md`.

## Before Migration

- Confirm database backups exist.
- Confirm `.env` values are not local-only for production or prod environments.
- Confirm backend, frontend, Python, and Docker Compose validation commands pass.
- Record the current Git commit and container image tags.

## Migration Steps

1. Pull the RC1 commit.
2. Review `VERSION` and `RC1_RELEASE_NOTES.md`.
3. Apply existing Flyway migrations through application startup.
4. Start dependencies with Docker Compose.
5. Start backend, frontend, and Python services.
6. Verify health endpoints, login, survey creation, evidence upload, AI analysis, RAG retrieval, and report generation.

## Post-Migration Checks

- Confirm no service starts with local-only production secrets.
- Confirm rate-limit configuration is appropriate for the environment.
- Confirm logs and audit events are written for user-facing workflows.
- Confirm release evidence is archived with the RC1 commit hash.

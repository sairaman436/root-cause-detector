# RC1 Release Notes

Purpose: Provides operator-facing notes for version `1.0.0-rc.1`.
Why it exists: Release notes make the release candidate auditable and reproducible.
Architecture fit: Notes cover hardening changes only and do not redefine approved architecture.

Related documents: `VERSION`, `docs/operations/RC1_RELEASE_CANDIDATE_REPORT.md`, `docs/operations/RC1_MIGRATION_GUIDE.md`.

## Added

- Repository version marker for `1.0.0-rc.1`.
- Production unsafe-secret validator.
- Configurable backend request rate limiting.
- Stable Spring Data pagination serialization mode.
- RC1 release, security, performance, testing, AI readiness, migration, rollback, deployment, and production-readiness documentation.

## Changed

- Backend application documentation now reflects implemented platform scope.
- Master technical debt wording now distinguishes future integration monitors from completed downstream integrations.
- Monitoring Terraform module wording now describes reserved observability boundaries accurately.

## Compatibility

RC1 is compatible with the current monorepo layout, local Docker Compose workflow, Spring Boot backend, Next.js portals, and Python AI service boundaries.

## Upgrade Notes

Production-like deployments must provide non-local database and JWT secrets. Environments named `production` or `prod` fail startup when unsafe local defaults are detected.

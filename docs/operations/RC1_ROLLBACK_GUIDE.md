# RC1 Rollback Guide

Purpose: Defines rollback actions for RC1.
Why it exists: Release candidates must have a controlled recovery path when validation fails.
Architecture fit: Rollback relies on Git revision control, container image tags, Flyway discipline, and database backups.

Related documents: `docs/operations/RC1_MIGRATION_GUIDE.md`, `docs/operations/RC1_DEPLOYMENT_CHECKLIST.md`.

## Rollback Triggers

- Backend startup fails after validated configuration correction.
- Authentication, survey, evidence, AI, or reporting workflows fail critical acceptance checks.
- Data migration causes unexpected corruption or unacceptable latency.
- Security validation identifies a critical vulnerability.

## Rollback Steps

1. Stop traffic or place the environment in maintenance mode.
2. Restore the previous application image or Git commit.
3. Restore the database backup if schema or data corruption occurred.
4. Restart dependencies and services.
5. Re-run health checks and critical workflow smoke tests.
6. Record incident details in the release audit log.

## Decision Rule

Rollback is preferred over hotfixing when the fault blocks authentication, data integrity, evidence access, AI safety, or report generation.

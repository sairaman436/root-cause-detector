# Purpose: Defines restore validation tests for backups and disaster recovery.

# Why it exists: Ensures backups are restorable and operationally useful.

# Architecture fit: Supports Milestone 11 disaster recovery tests and backup reports.

# Restore Validation Plan

## Procedure

1. Select latest PostgreSQL recovery point.
2. Restore into an isolated environment.
3. Apply Flyway validation.
4. Start backend with restored database.
5. Run health, version, and read-only smoke tests.
6. Record evidence in `operations.backup_reports`.

## Acceptance Criteria

- Restore completes within RTO.
- Recovery point satisfies RPO.
- Migration validation succeeds.
- Platform readiness endpoint is healthy.

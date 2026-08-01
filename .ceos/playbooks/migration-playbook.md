# Migration Playbook

## Purpose

This playbook defines how database and data migrations are planned, implemented, validated, and released.

## Why

Migrations can corrupt data, block releases, or break production services if they are not controlled.

## When

Use this for Flyway migrations, schema changes, data backfills, retention jobs, vector collection migrations, feature store migrations, and metadata catalog changes.

## How

1. Identify owner, schema, impacted services, data classification, and expected growth.
2. Design forward-compatible changes before code depends on them.
3. Add migration with deterministic IDs and repeatable semantics.
4. Validate against test database and migration validation jobs.
5. Define backup, rollback, or roll-forward response.
6. Monitor migration duration, locks, errors, and post-release query performance.

## Tradeoffs

Backward-compatible migrations may require multi-step releases. This reduces downtime and rollback risk.

## Best Practices

- Add columns before requiring them.
- Run backfills as bounded batches.
- Add indexes with production lock impact in mind.
- Keep destructive changes behind retention and approval.
- Record restore validation evidence for high-risk migrations.

## Anti-Patterns

- Combining schema changes and destructive data cleanup in one unreviewed release.
- Relying on ORM auto-DDL in production.
- Adding indexes without understanding query patterns.
- Using production data in local tests without privacy review.
- Running manual SQL outside migration governance.

## Related Documents

See [Database Constitution](../constitutions/database-constitution.md), [Testing Constitution](../constitutions/testing-constitution.md), [Release Playbook](release-playbook.md), and [docs/operations/DISASTER_RECOVERY_GUIDE.md](../../docs/operations/DISASTER_RECOVERY_GUIDE.md).

# Database Constitution

## Purpose

This constitution governs operational databases, analytics stores, event stores, vector stores, object storage, metadata catalogs, migrations, backups, and data retention.

## Why

The platform’s decisions depend on evidence, lineage, policy, survey history, geospatial context, AI traces, and audit records. Data must be trustworthy, recoverable, governed, and evolvable.

## When It Applies

It applies to schema changes, migrations, indexes, partitioning, retention, lineage, backup policies, analytical pipelines, feature stores, vector collections, and data access patterns.

## How To Apply

- Every persistent entity must have ownership, classification, retention, indexes, and growth expectations.
- Every schema change must use Flyway or the approved migration mechanism.
- Every migration must be forward-only unless the Database Review Board approves a rollback strategy.
- Operational systems must not be used as ad hoc analytics warehouses.
- Backups and restore validation are required production controls.

## Tradeoffs

Governed schema evolution requires more upfront design. It prevents data loss, query regressions, unclear ownership, and unbounded retention risk.

## Best Practices

- Design indexes from query patterns and expected growth.
- Keep audit and decision trace data immutable except for approved retention processes.
- Use object storage for large binaries and metadata in operational tables.
- Use vector databases for semantic retrieval, not source-of-truth storage.
- Validate migrations in CI and production release jobs.

## Anti-Patterns

- Adding nullable columns without a data population strategy.
- Storing large files in relational tables.
- Deleting regulated records without retention review.
- Reusing operational tables for reporting-heavy workloads.
- Creating schemas without owner and lifecycle metadata.

## Related Documents

See [Security Constitution](security-constitution.md), [Testing Constitution](testing-constitution.md), [Migration Playbook](../playbooks/migration-playbook.md), [Repository Standards](../standards/repository-standards.md), and [Decision Record Template](../templates/decision-record-template.md).

# Engineering Constitution

## Purpose

This constitution defines how engineering work is performed across the platform.

## Why

The platform will serve governments, NGOs, enterprises, researchers, and decision makers. Engineering failures can become governance failures, privacy failures, or misleading recommendations. A shared engineering constitution prevents local optimizations from weakening the whole system.

## When It Applies

It applies to every repository change, design review, migration, AI artifact, operational procedure, test plan, and release.

## How To Apply

- Start from approved architecture and [Project Memory](../memory/PROJECT_MEMORY.md).
- Keep changes scoped to the requested milestone or approved work item.
- Prefer simple, observable, reversible designs.
- Treat docs, tests, security, and operations as part of implementation, not follow-up work.
- Document meaningful choices with [Decision Records](../templates/decision-record-template.md).

## Tradeoffs

This model slows the first version of a change because teams must document and validate decisions. It reduces long-term rework, unreviewed risk, ownership ambiguity, and production surprises.

## Best Practices

- Make the smallest change that fully satisfies the approved objective.
- Preserve public contracts unless a migration plan and compatibility window are approved.
- Build failure handling before adding scale.
- Add telemetry where operators need decisions, not where it is easiest to emit metrics.
- Keep business behavior, platform foundations, and governance assets clearly separated.

## Anti-Patterns

- Shipping code without a rollback path.
- Treating tests as optional because a change is “configuration only.”
- Reopening approved architecture without a formal decision record.
- Mixing future milestone capabilities into the current scope.
- Using undocumented conventions that only one engineer understands.

## Related Documents

See [Architecture Constitution](architecture-constitution.md), [Testing Constitution](testing-constitution.md), [Release Constitution](release-constitution.md), [Repository Standards](../standards/repository-standards.md), and [Engineering Playbooks](../playbooks/engineering-playbooks.md).

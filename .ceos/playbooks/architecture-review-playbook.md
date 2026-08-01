# Architecture Review Playbook

## Purpose

This playbook defines how architecture changes are proposed, reviewed, approved, and recorded.

## Why

Architecture decisions create long-term constraints. Review ensures they are coherent with approved platform direction.

## When

Use this for new services, new databases, external integrations, cross-domain dependencies, API contract changes, AI architecture changes, infrastructure topology changes, and CEOS policy changes.

## How

1. Prepare an architecture proposal using [Architecture Proposal Template](../templates/architecture-proposal-template.md).
2. Identify impacted domains, data stores, APIs, events, security controls, and operational runbooks.
3. Compare alternatives and tradeoffs.
4. Record accepted decisions using [Decision Record Template](../templates/decision-record-template.md).
5. Obtain approval from boards listed in [Review Boards](../organization/review-boards.md).
6. Update [Project Memory](../memory/PROJECT_MEMORY.md) when the decision becomes durable.

## Tradeoffs

Formal review takes time but prevents incompatible designs and hidden platform debt.

## Best Practices

- Prefer diagrams and explicit dependency lists for complex changes.
- Define failure modes and observability before approval.
- Include migration and rollback strategy.
- Keep architecture decisions close to implementation milestones.
- Make exceptions time-bound.

## Anti-Patterns

- Approving architecture through chat only.
- Deferring security and operations to later phases.
- Treating a vendor selection as architecture without exit strategy.
- Ignoring existing approved decisions.
- Recording decisions without consequences.

## Related Documents

See [Architecture Constitution](../constitutions/architecture-constitution.md), [Security Constitution](../constitutions/security-constitution.md), [Database Constitution](../constitutions/database-constitution.md), and [Review Boards](../organization/review-boards.md).

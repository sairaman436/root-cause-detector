# Architecture Constitution

## Purpose

This constitution governs system structure, module boundaries, integration patterns, and architectural change.

## Why

Decision intelligence platforms fail when architectural boundaries blur. Identity, surveys, evidence, geospatial intelligence, events, AI, agents, decisions, operations, and future analytics must evolve independently while sharing governance, security, observability, and data contracts.

## When It Applies

It applies to new services, modules, APIs, database schemas, event contracts, AI workflows, infrastructure topology, and cross-module dependencies.

## How To Apply

- Preserve the approved modular monorepo and Spring Boot core backend boundaries.
- Use domain-driven decomposition for domain modules and platform capabilities.
- Keep transactional consistency inside a bounded context and communicate across contexts through APIs, events, or explicitly approved shared libraries.
- Record significant architecture choices with [Architecture Proposal Template](../templates/architecture-proposal-template.md) and [Decision Record Template](../templates/decision-record-template.md).
- Require Architecture Review Board approval for new persistent stores, new external dependencies, new runtime services, or boundary changes.

## Tradeoffs

Strict boundaries create some duplication in DTOs and mapping logic. That duplication is acceptable when it preserves domain independence, testability, security review clarity, and deployability.

## Best Practices

- Prefer clear module boundaries over generic shared abstractions.
- Keep shared libraries limited to stable contracts, common primitives, and cross-cutting utilities.
- Make integrations observable and timeout-bound.
- Design for graceful degradation when AI, search, vector storage, cache, or event infrastructure is degraded.
- Model data ownership explicitly.

## Anti-Patterns

- Direct database reads across domains.
- Shared “utility” packages that become hidden business logic.
- Adding an external service without lifecycle ownership and runbooks.
- Synchronous chains across many services for user-critical flows.
- Infrastructure that exists in production but not in versioned manifests.

## Related Documents

See [Database Constitution](database-constitution.md), [DevOps Constitution](devops-constitution.md), [Repository Standards](../standards/repository-standards.md), [Architecture Review Playbook](../playbooks/architecture-review-playbook.md), and [Review Boards](../organization/review-boards.md).

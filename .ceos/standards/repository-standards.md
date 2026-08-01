# Repository Standards

## Purpose

This standard defines repository structure, ownership, dependency rules, naming, configuration, and file placement.

## Why

The monorepo must remain navigable as backend, frontend, AI services, shared packages, infrastructure, operations, and governance assets grow.

## When

Apply this standard for every new file, module, package, service, workflow, and documentation asset.

## How

- Place backend domain code under `services/core-backend/src/main/java/com/airural/platform/core/<domain>`.
- Place frontend applications under `apps`.
- Place Python services under `services/<service-name>`.
- Place reusable contracts and libraries under `packages`.
- Place Kubernetes and deployment manifests under `deploy`.
- Place Terraform under `infra/terraform`.
- Place operational docs under `docs/operations`.
- Place engineering operating system policy under `.ceos`.
- Keep generated build outputs, caches, credentials, and local state out of Git.

## Dependency Rules

- Domain modules may depend on common primitives and shared contracts, not on unrelated domain internals.
- Infrastructure adapters depend inward on application/domain contracts.
- Shared packages must be stable, versionable, and free of domain-specific shortcuts.
- Tests may use test helpers but must not create production dependency cycles.

## Tradeoffs

Strict placement can feel slower for small changes. It makes ownership, review routing, CI paths, and onboarding predictable.

## Best Practices

- Add a README when introducing a new top-level directory or major subsystem.
- Keep file names descriptive and stable.
- Use existing package patterns before creating new abstractions.
- Group production manifests by runtime concern.
- Keep configuration examples explicit and secret-free.

## Anti-Patterns

- Creating parallel structures for the same concern.
- Placing business logic in shared utility packages.
- Hiding runtime behavior in scripts without documentation.
- Committing generated artifacts or local caches.
- Creating files without clear ownership.

## Related Documents

See [Architecture Constitution](../constitutions/architecture-constitution.md), [Coding Standards](coding-standards.md), [Workflow Standards](workflow-standards.md), and [Project Memory](../memory/PROJECT_MEMORY.md).

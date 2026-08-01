# Testing Constitution

## Purpose

This constitution defines verification requirements for application code, infrastructure, data, AI, security, performance, and operations.

## Why

The platform cannot rely on compile checks alone. Tests must exercise the actual failure paths and delivery paths that matter in production.

## When It Applies

It applies to every change, including documentation-driven workflows, CI/CD updates, configuration, migrations, Kubernetes manifests, Terraform, prompts, model policies, and operational runbooks.

## How To Apply

- Match test depth to risk and blast radius.
- Run unit, integration, API, migration, security, and operational checks where relevant.
- Validate the specific bug, release path, or risk being addressed.
- Record test evidence in final engineering summaries.
- Keep test names and fixtures stable so failures are actionable.

## Tradeoffs

Comprehensive testing increases build time. The tradeoff is managed through focused suites, fast checks for local work, and deeper CI gates for production-risk changes.

## Best Practices

- Use deterministic test doubles for external AI services.
- Validate database migrations against test databases.
- Render Kubernetes manifests and validate Compose configs.
- Test security boundaries through integration tests, not only unit tests.
- Maintain performance, chaos, and DR test templates for production readiness.

## Anti-Patterns

- Calling work complete after syntax checks only.
- Skipping tests for infrastructure or documentation that controls production behavior.
- Mocking the behavior under test.
- Ignoring flaky tests instead of isolating root causes.
- Adding tests that assert implementation details without user or operator value.

## Related Documents

See [Engineering Constitution](engineering-constitution.md), [Release Constitution](release-constitution.md), [Testing Strategy](../standards/testing-standards.md), [Release Playbook](../playbooks/release-playbook.md), and [Engineering Review Template](../templates/engineering-plan-template.md).

# Testing Standards

## Purpose

This standard defines required test types, evidence, naming, and validation expectations.

## Why

Tests are the mechanism by which architecture, security, reliability, and product promises become enforceable.

## When

Apply this standard to every implementation, infrastructure, AI, documentation, or release-governance change that affects behavior or operations.

## How

- Use unit tests for deterministic domain and application logic.
- Use integration tests for security, persistence, APIs, migrations, and cross-module flows.
- Use contract tests for shared APIs and event contracts.
- Use manifest validation for Kubernetes and Compose changes.
- Use foundation tests for repository policy assets.
- Use load, stress, chaos, and DR templates for production readiness evidence.

## Tradeoffs

More tests increase maintenance. The standard manages this by requiring tests that protect behavior, not tests that freeze implementation details.

## Best Practices

- Reproduce the actual failure or delivery path.
- Keep tests deterministic and isolated.
- Validate negative paths and authorization failures.
- Capture exact commands and outcomes in final summaries.
- Use CI to run broad gates and local commands for focused validation.

## Anti-Patterns

- Treating a successful compile as a complete validation.
- Skipping migration tests because SQL “looks simple.”
- Writing brittle tests around private methods.
- Ignoring failed local checks because CI might differ.
- Leaving validation status ambiguous.

## Related Documents

See [Testing Constitution](../constitutions/testing-constitution.md), [Release Constitution](../constitutions/release-constitution.md), [Engineering Playbooks](../playbooks/engineering-playbooks.md), and [docs/engineering/TESTING_STRATEGY.md](../../docs/engineering/TESTING_STRATEGY.md).

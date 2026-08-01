# Coding Standards

## Purpose

This standard defines implementation quality for future application code.

## Why

Production code must be understandable, testable, secure, observable, and consistent across teams.

## When

Apply this standard to Java, TypeScript, Python, SQL migrations, configuration code, scripts, and infrastructure code.

## How

- Follow existing language and framework conventions in the repository.
- Keep classes and functions focused on one responsibility.
- Express boundaries through packages, DTOs, services, repositories, and adapters.
- Prefer typed contracts over unstructured maps unless the domain explicitly requires flexible metadata.
- Handle failures explicitly with domain-specific exceptions and stable error responses.
- Emit structured logs and metrics where operators need visibility.
- Add comments only when they explain non-obvious decisions or constraints.

## Tradeoffs

Clear code may require more files than a quick script. The payoff is lower change risk and easier review.

## Best Practices

- Use Clean Architecture dependency direction.
- Keep validation close to boundaries.
- Preserve backward compatibility for public APIs.
- Keep migrations forward-only and deterministic.
- Write tests alongside new behavior.

## Anti-Patterns

- Overly generic services with unclear domain ownership.
- Silent exception swallowing.
- Magic strings that should be enums or constants.
- Security checks duplicated inconsistently across controllers.
- Tests that only assert mocks were called.

## Related Documents

See [Engineering Constitution](../constitutions/engineering-constitution.md), [Testing Standards](testing-standards.md), [Security Constitution](../constitutions/security-constitution.md), and [docs/engineering/CODING_STANDARDS.md](../../docs/engineering/CODING_STANDARDS.md).

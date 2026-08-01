# Documentation Constitution

## Purpose

This constitution governs how engineering documentation is created, reviewed, maintained, and retired.

## Why

Enterprise engineering depends on durable shared context. Documentation must help future engineers understand decisions without relying on conversation history or individual memory.

## When It Applies

It applies to architecture docs, standards, runbooks, APIs, schemas, prompts, model cards, data cards, release notes, incident reports, and CEOS changes.

## How To Apply

- Every document must state purpose, why it exists, when it applies, how to use it, tradeoffs, best practices, anti-patterns, and related documents where applicable.
- Use templates from [Templates](../templates/README.md).
- Keep documents close to the system they govern.
- Update documentation in the same change as the behavior or process it describes.
- Retire obsolete documentation explicitly rather than leaving contradictions.

## Tradeoffs

High-quality documentation adds writing work to engineering tasks. It reduces onboarding cost, review friction, compliance risk, and repeated architectural debate.

## Best Practices

- Prefer direct, operational language over broad claims.
- Link to source files, runbooks, and decision records.
- Record rationale, not just decisions.
- Keep ownership clear.
- Use examples when they prevent misinterpretation.

## Anti-Patterns

- Empty documents with headings but no policy.
- Documentation that says what but not why.
- Copying vendor documentation instead of platform-specific guidance.
- Leaving outdated docs after architecture changes.
- Treating docs as separate from engineering quality.

## Related Documents

See [Engineering Constitution](engineering-constitution.md), [Repository Standards](../standards/repository-standards.md), [Documentation Standards](../standards/documentation-standards.md), [Decision Record Template](../templates/decision-record-template.md), and [Project Memory](../memory/PROJECT_MEMORY.md).

# CEOS Governance

## Purpose

This document defines how CEOS itself is changed, reviewed, enforced, and audited.

## Why

An engineering operating system can become stale or political if it has no governance. CEOS must evolve deliberately while remaining stable enough to guide future milestones.

## When

Use this governance process for any change to `.ceos`, including constitutions, standards, playbooks, templates, organization rules, and project memory.

## How

- Classify the change as editorial, policy, structural, or emergency.
- Editorial changes require Documentation Engineering review.
- Policy changes require Engineering Council approval and any impacted review board.
- Structural changes require Architecture Review Board and Engineering Council approval.
- Emergency changes may be applied with Engineering Council approval but must receive retrospective board review within five business days.
- Every policy change must update [MANIFEST](MANIFEST.md) when it changes document inventory or authority.

## Enforcement

CEOS is enforced through pull request review, release review, architecture review, security review, MLOps review, and production readiness gates. A change that violates CEOS must either be revised or approved as a documented exception.

## Tradeoffs

Governance makes policy updates slower. This prevents silent weakening of engineering standards and ensures teams can rely on CEOS as a stable internal contract.

## Best Practices

- Keep CEOS changes small and reviewable.
- Link changes to incidents, milestone needs, or explicit decision records.
- Preserve backward compatibility of templates where possible.
- Update related documents in the same change.
- Record durable CEOS decisions in [Project Memory](memory/PROJECT_MEMORY.md).

## Anti-Patterns

- Changing policy to justify an already implemented shortcut.
- Creating conflicting standards in other directories.
- Adding template fields that nobody reviews.
- Letting project memory become a changelog instead of durable context.
- Using emergency governance for routine work.

## Related Documents

See [Engineering Constitution](constitutions/engineering-constitution.md), [Documentation Constitution](constitutions/documentation-constitution.md), [Review Boards](organization/review-boards.md), [Decision Record Template](templates/decision-record-template.md), and [MANIFEST](MANIFEST.md).

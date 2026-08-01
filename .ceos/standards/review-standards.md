# Review Standards

## Purpose

This standard defines how reviews are conducted for code, architecture, security, AI, data, infrastructure, and releases.

## Why

Reviews are quality gates and knowledge-sharing mechanisms. They must catch risks before they become production incidents.

## When

Apply this standard to pull requests, architecture proposals, decision records, AI artifacts, migrations, infrastructure changes, security-sensitive changes, and release approvals.

## How

- Start reviews with correctness, security, data integrity, and operational risk.
- Require domain owner review for module changes.
- Require board review for changes listed in [Review Boards](../organization/review-boards.md).
- Review tests and rollback plans as first-class deliverables.
- Document unresolved risks and accepted tradeoffs.

## Tradeoffs

Review gates can slow delivery. They reduce rework and prevent local decisions from damaging platform-level reliability.

## Best Practices

- Provide file and line-specific feedback where possible.
- Separate blocking issues from suggestions.
- Ask for evidence, not reassurance.
- Prefer small reviewable changes.
- Confirm the change matches the approved milestone scope.

## Anti-Patterns

- Rubber-stamping large changes.
- Focusing on formatting while ignoring correctness.
- Approving untested security-sensitive changes.
- Requiring personal preference changes without policy basis.
- Allowing unresolved architectural disagreement to move into implementation.

## Related Documents

See [Organization Structure](../organization/organization-structure.md), [Review Boards](../organization/review-boards.md), [Engineering Plan Template](../templates/engineering-plan-template.md), and [Security Review Template](../templates/security-review-template.md).

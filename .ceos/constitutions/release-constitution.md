# Release Constitution

## Purpose

This constitution governs how changes move from repository to production.

## Why

Production releases must be deliberate, reversible, traceable, and validated. The platform’s users depend on stable access, reliable decisions, and accountable changes.

## When It Applies

It applies to every merge, image publication, database migration, deployment, feature enablement, model promotion, prompt promotion, infrastructure change, and emergency fix.

## How To Apply

- Require green CI gates before merge unless an emergency exception is approved.
- Require release notes and rollback instructions for production-impacting changes.
- Validate migrations, health endpoints, smoke tests, and observability before traffic promotion.
- Use canary, shadow, or phased rollout for high-risk changes.
- Capture release evidence in the release review process.

## Tradeoffs

Release governance reduces spontaneous production changes. It increases auditability, reliability, and confidence in operations.

## Best Practices

- Keep releases small enough to reason about.
- Separate deployment from feature exposure when risk warrants.
- Use immutable image references and signed provenance.
- Verify rollback before it is needed.
- Monitor post-release metrics against agreed thresholds.

## Anti-Patterns

- Releasing unreviewed changes directly to production.
- Combining unrelated changes into one release.
- Running migrations without validation and backup confirmation.
- Declaring success before post-deployment checks.
- Promoting AI artifacts outside model and prompt governance.

## Related Documents

See [DevOps Constitution](devops-constitution.md), [Testing Constitution](testing-constitution.md), [Release Playbook](../playbooks/release-playbook.md), [Release Review Template](../templates/release-review-template.md), and [Enterprise Readiness Checklist](../../docs/operations/ENTERPRISE_READINESS_CHECKLIST.md).

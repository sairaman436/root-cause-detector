# Release Playbook

## Purpose

This playbook defines how changes are prepared, approved, deployed, validated, and rolled back.

## Why

Release discipline protects production availability and institutional trust.

## When

Use this for application releases, infrastructure releases, migration releases, model and prompt promotions, emergency fixes, and production configuration changes.

## How

1. Confirm scope and linked approvals.
2. Run required CI gates and local validation.
3. Confirm migration and rollback strategy.
4. Build and publish signed artifacts.
5. Deploy to the target environment.
6. Run readiness, liveness, smoke, and business-critical checks.
7. Monitor dashboards and alerts through the observation window.
8. Record release evidence using [Release Review Template](../templates/release-review-template.md).

## Tradeoffs

Structured releases reduce speed for trivial changes. They also make production safer and post-incident analysis clearer.

## Best Practices

- Keep release units small.
- Prefer canary or phased rollout for risky changes.
- Use immutable image tags.
- Separate deployment from feature activation where possible.
- Verify rollback before declaring release complete.

## Anti-Patterns

- Deploying with failing or skipped gates.
- Combining infrastructure, schema, and AI model changes without staged rollout.
- Losing track of which image is in production.
- Declaring success before monitoring.
- Failing to document emergency changes after the incident.

## Related Documents

See [Release Constitution](../constitutions/release-constitution.md), [DevOps Constitution](../constitutions/devops-constitution.md), [Production Readiness Playbook](production-readiness-playbook.md), and [docs/operations/DEPLOYMENT_GUIDE.md](../../docs/operations/DEPLOYMENT_GUIDE.md).

# Production Readiness Playbook

## Purpose

This playbook defines the readiness review required before production enablement.

## Why

Production readiness requires more than passing tests. Teams must prove operability, security, resilience, and governance.

## When

Use this before launching new services, major modules, high-risk features, AI capabilities, infrastructure changes, and enterprise customer deployments.

## How

1. Confirm ownership, architecture, and support model.
2. Validate security, privacy, and compliance controls.
3. Confirm dashboards, metrics, logs, traces, and alerts.
4. Confirm runbooks and escalation paths.
5. Run performance, disaster recovery, and rollback checks appropriate to risk.
6. Confirm release and rollback procedures.
7. Complete [Release Review Template](../templates/release-review-template.md) and link evidence.

## Tradeoffs

Readiness gates delay launch if operational evidence is missing. This prevents fragile launches and emergency retrofits.

## Best Practices

- Define SLOs and alert thresholds before launch.
- Test restore paths before relying on backups.
- Validate degraded dependency behavior.
- Confirm user-facing and operator-facing documentation.
- Run post-launch review after the observation window.

## Anti-Patterns

- Launching without ownership.
- Adding alerts with no responder.
- Assuming cloud-managed services remove DR responsibility.
- Treating security review as paperwork.
- Skipping rollback rehearsal.

## Related Documents

See [DevOps Constitution](../constitutions/devops-constitution.md), [Release Constitution](../constitutions/release-constitution.md), [Security Constitution](../constitutions/security-constitution.md), and [Enterprise Readiness Checklist](../../docs/operations/ENTERPRISE_READINESS_CHECKLIST.md).

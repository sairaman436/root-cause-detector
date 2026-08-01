# Incident Response Playbook

## Purpose

This playbook defines how production incidents are detected, triaged, mitigated, communicated, and reviewed.

## Why

Incidents are inevitable in complex platforms. The response must protect users, data, trust, and evidence.

## When

Use this for availability incidents, data incidents, security incidents, AI safety incidents, migration failures, dependency outages, and operational degradation.

## How

1. Declare incident severity and incident commander.
2. Stabilize user impact before root-cause analysis.
3. Preserve evidence, logs, traces, decision records, and timelines.
4. Communicate status through approved channels.
5. Mitigate through rollback, feature disablement, traffic shift, or human review routing.
6. Complete an incident review using [Incident Review Template](../templates/incident-review-template.md).
7. Convert learnings into tests, runbooks, alerts, or CEOS updates.

## Tradeoffs

Incident process can feel heavy during urgency. It keeps response coordinated and prevents evidence loss.

## Best Practices

- Assign one incident commander.
- Separate investigation from mitigation.
- Use runbooks before improvising.
- Keep user and stakeholder updates factual.
- Track follow-up owners and deadlines.

## Anti-Patterns

- Multiple people changing production independently.
- Deleting evidence during mitigation.
- Waiting for perfect root cause before reducing impact.
- Blaming individuals instead of improving systems.
- Closing incidents without preventive actions.

## Related Documents

See [Security Constitution](../constitutions/security-constitution.md), [DevOps Constitution](../constitutions/devops-constitution.md), [docs/operations/RUNBOOKS.md](../../docs/operations/RUNBOOKS.md), and [Security Review Template](../templates/security-review-template.md).

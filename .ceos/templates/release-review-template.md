# Release Review Template

## Purpose

Use this template for production-impacting releases.

## Required Content

### Release Summary

Describe changes, commit range, artifacts, and deployment target.

### Risk Assessment

Identify user, security, data, AI, operational, and rollback risks.

### Validation Evidence

List CI checks, local checks, migration validation, manifest rendering, smoke tests, and performance checks.

### Deployment Plan

Describe sequence, owner, timing, and monitoring window.

### Rollback Plan

Define exact rollback trigger, steps, owner, and validation.

### Post-Release Monitoring

List dashboards, alerts, SLOs, and business metrics.

### Approval

List required boards and final decision.

## Anti-Patterns

- Releasing without artifact identity.
- Missing rollback trigger.
- Treating deployment as complete before monitoring.
- Omitting data and AI artifact changes.

## Related Documents

See [Release Constitution](../constitutions/release-constitution.md), [Release Playbook](../playbooks/release-playbook.md), [Production Readiness Playbook](../playbooks/production-readiness-playbook.md), and [docs/operations/DEPLOYMENT_GUIDE.md](../../docs/operations/DEPLOYMENT_GUIDE.md).

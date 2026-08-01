# Security Review Template

## Purpose

Use this template for security-sensitive changes.

## Required Content

### Scope

Identify assets, users, services, data, prompts, models, and infrastructure affected.

### Threat Model

List likely adversaries, abuse cases, trust boundaries, and attack paths.

### Controls

Document authentication, authorization, encryption, audit, rate limits, validation, and monitoring.

### Secrets

Identify secret sources, rotation policy, and exposure prevention.

### Privacy

Document data classification, minimization, retention, and access boundaries.

### AI Security

Document prompt injection, tool misuse, retrieval poisoning, model artifact integrity, and unsafe output controls.

### Residual Risk

State accepted risks, owner, review date, and compensating controls.

## Anti-Patterns

- Treating internal services as trusted by default.
- Approving broad permissions without expiration.
- Logging sensitive content for debugging convenience.
- Ignoring AI-specific abuse paths.

## Related Documents

See [Security Constitution](../constitutions/security-constitution.md), [AI Constitution](../constitutions/ai-constitution.md), [Incident Response Playbook](../playbooks/incident-response-playbook.md), and [Review Boards](../organization/review-boards.md).

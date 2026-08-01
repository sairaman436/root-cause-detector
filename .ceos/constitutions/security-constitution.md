# Security Constitution

## Purpose

This constitution governs authentication, authorization, secrets, encryption, audit logging, AI security, supply chain security, incident response, and privacy.

## Why

The platform handles sensitive users, organizations, evidence, geospatial context, policy interpretation, and AI-generated decisions. Security is required for trust, compliance, and institutional adoption.

## When It Applies

It applies to code, APIs, infrastructure, data, prompts, models, build pipelines, dependencies, secrets, incident procedures, and operational access.

## How To Apply

- Use existing IAM, RBAC, and audit logging for protected capabilities.
- Apply least privilege to users, services, workloads, CI jobs, and cloud resources.
- Keep secrets outside Git and source them through approved secret managers.
- Encrypt sensitive data in transit and at rest.
- Run secret scanning, dependency scanning, container scanning, SBOM generation, and provenance controls through CI.
- Treat prompt injection, tool misuse, model artifact tampering, and retrieval poisoning as security threats.

## Tradeoffs

Security controls can increase implementation effort and slow release approval. That is acceptable because remediation after exposure or unsafe recommendations is more expensive and reputationally damaging.

## Best Practices

- Require explicit permission names for new protected endpoints.
- Audit every consequential administrative, AI, evidence, and decision action.
- Prefer short-lived credentials and automatic rotation.
- Fail closed when authorization or policy checks cannot be completed.
- Write incident runbooks before production enablement.

## Anti-Patterns

- Committing real secrets, credentials, tokens, or private keys.
- Using broad administrator permissions for service accounts.
- Logging sensitive evidence or prompt content unnecessarily.
- Treating internal APIs as trusted without authentication.
- Ignoring AI-specific attack paths.

## Related Documents

See [AI Constitution](ai-constitution.md), [DevOps Constitution](devops-constitution.md), [Security Review Template](../templates/security-review-template.md), [Incident Playbook](../playbooks/incident-response-playbook.md), and [Review Boards](../organization/review-boards.md).

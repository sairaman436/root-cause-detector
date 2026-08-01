# Organization Structure

## Purpose

This document defines the virtual engineering organization responsible for the platform.

## Why

Enterprise engineering needs explicit ownership. Without it, modules grow without accountable maintainers and production issues fall between teams.

## Teams

- Engineering Council: owns CEOS, engineering principles, milestone scope integrity, and cross-team arbitration.
- Architecture Group: owns system architecture, domain boundaries, APIs, dependency rules, and architecture records.
- Backend Platform Team: owns Spring Boot core backend, domain modules, APIs, persistence adapters, and shared Java contracts.
- Frontend Platform Team: owns user and admin portals, frontend standards, accessibility, and client contracts.
- AI Engineering Team: owns AI gateway, RAG, prompts, agents, decision intelligence, guardrails, and evaluation integration.
- Data Engineering Team: owns operational data design, eventing, analytics readiness, feature store contracts, lineage, and retention.
- Security Engineering Team: owns IAM, RBAC, ABAC, secrets, encryption, supply chain, threat models, and incident response.
- DevOps and SRE Team: owns CI/CD, containers, Kubernetes, Terraform, observability, reliability, DR, and production readiness.
- MLOps Team: owns model registry, prompt registry, evaluation, drift, promotion, rollback, and AI artifact governance.
- QA Engineering Team: owns test strategy, quality gates, regression strategy, and evidence collection.
- Documentation Engineering Team: owns documentation standards, templates, runbooks, and durable project memory.

## How Work Is Routed

Work is routed by the highest-risk affected area. A backend change with security impact requires Backend and Security review. An AI prompt change with production behavior impact requires AI Engineering, MLOps, and AI Governance review.

## Tradeoffs

Explicit ownership adds coordination overhead. It prevents unowned systems, inconsistent reviews, and hidden operational risk.

## Best Practices

- Assign one directly responsible team for every module.
- Use review boards for cross-cutting decisions.
- Escalate early when ownership is ambiguous.
- Keep approvals evidence-based.
- Record durable decisions in [Project Memory](../memory/PROJECT_MEMORY.md).

## Anti-Patterns

- Shared ownership without a final accountable owner.
- Bypassing boards because a change is urgent.
- Treating documentation ownership as optional.
- Allowing teams to define conflicting standards.
- Keeping decisions only in meetings.

## Related Documents

See [Review Boards](review-boards.md), [Review Standards](../standards/review-standards.md), [Engineering Playbooks](../playbooks/engineering-playbooks.md), and [Decision Record Template](../templates/decision-record-template.md).

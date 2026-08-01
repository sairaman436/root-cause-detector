# AI-9 Enterprise AI Governance Platform

## Purpose

The Enterprise AI Governance Platform governs every AI artifact produced or consumed by the Rural Intelligence Platform. It covers datasets, models, prompts, agents, knowledge sources, inference flows, continuous learning artifacts, human feedback, deployments, and policies.

## Why It Exists

Production AI systems require independent controls for explainability, auditability, security, compliance, risk management, privacy, responsible AI review, and release approval. This module prevents production AI artifacts from bypassing governance while avoiding new model training, deployment, or inference behavior.

## Architecture Fit

AI-9 adds a governance bounded context under `com.airural.platform.core.governance`. It follows the approved backend architecture:

- REST adapter: `GovernanceController`
- Application service: `GovernanceService`
- Domain persistence: governance JPA entities
- Infrastructure adapters: Spring Data repositories
- Database namespace: `governance`
- Security boundary: `GOVERNANCE_READ`, `GOVERNANCE_MANAGE`, `GOVERNANCE_APPROVE`, compliance review, AI governance review, and AI admin authorities

## Implemented Capabilities

- Governance policy registry with configurable rules.
- Compliance control matrix for ISO 42001, NIST AI RMF, OWASP LLM Top 10, GDPR-ready privacy concepts, and related control evidence.
- Risk register for model, prompt, dataset, knowledge, security, operational, compliance, and bias risks.
- Risk assessment records for governed artifact decisions.
- Prompt registry and prompt approval records.
- Agent registry and agent policy records.
- Immutable governance audit ledger with hash chaining.
- Policy violation storage for governance observability.
- Governance report persistence for board and release evidence.

## REST APIs

- `GET /api/v1/governance/policies`
- `POST /api/v1/governance/policies`
- `GET /api/v1/governance/audit`
- `GET /api/v1/governance/compliance`
- `GET /api/v1/governance/risks`
- `POST /api/v1/governance/approve`
- `POST /api/v1/governance/reject`

The same endpoints are also exposed under `/governance` for internal platform adapters that already use short AI route aliases.

## Database Schema

Flyway migration `V19__enterprise_ai_governance_platform.sql` creates:

- `governance.governance_policies`
- `governance.governance_rules`
- `governance.compliance_controls`
- `governance.risk_register`
- `governance.risk_assessments`
- `governance.prompt_registry`
- `governance.prompt_approvals`
- `governance.agent_registry`
- `governance.agent_policies`
- `governance.audit_records`
- `governance.policy_violations`
- `governance.governance_reports`

The schema includes unique constraints, foreign keys, status checks, risk-score checks, and indexes for policy domain/status, rule priority, compliance framework/status, risk type/status/severity, artifact risk lookup, prompt status, agent status, audit lookup, and violation remediation.

## Security Model

Read operations require governance read, audit, compliance, governance review, or AI admin authority. Management operations require governance management or AI governance authority. Approval and rejection operations require governance approval, compliance review, AI governance review, release review, or AI admin authority.

Audit records are append-only through the exposed API surface. Each audit entry stores an event hash and previous hash to support tamper detection and external audit review.

## Explainability Contract

Governance approval records require:

- Artifact type
- Artifact reference
- Rationale
- Approval chain
- Residual risk score
- Policy compliance status
- Tamper-evident event hash

This satisfies the AI-9 requirement that AI outputs and artifacts expose model, dataset, prompt, agent, knowledge, confidence, evidence, decision trace, and policy compliance status through governed metadata and audit trails.

## Out Of Scope

AI-9 does not train models, deploy models, change production inference, collect datasets, or add new AI capabilities. It governs artifacts produced by previous and future AI milestones.

## Release Review

The implemented module supports governance board, security board, compliance board, architecture board, external audit, and release board review through durable policy, risk, compliance, and audit evidence.

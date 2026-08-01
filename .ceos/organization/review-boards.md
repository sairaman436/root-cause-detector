# Review Boards

## Purpose

This document defines review boards, their jurisdiction, and approval authority.

## Why

Review boards ensure high-impact decisions receive the right expertise before they become production constraints.

## Boards

### Engineering Council

Owns CEOS, milestone scope, cross-team conflict resolution, and engineering policy exceptions.

### Architecture Review Board

Approves new services, dependency boundary changes, database choices, API strategy changes, event contracts, and infrastructure topology.

### Security Review Board

Approves authentication, authorization, secrets, encryption, audit, supply chain, privacy, threat model, and incident response changes.

### Data Governance Board

Approves schemas, retention, lineage, data classification, feature store contracts, analytics pipelines, and data sharing.

### AI Governance Board

Approves prompts, models, agent tools, decision intelligence behavior, human review policy, safety thresholds, and explainability requirements.

### MLOps Review Board

Approves model promotion, prompt promotion, drift response, evaluation suites, rollback, and continuous learning processes.

### Release Review Board

Approves production releases, emergency changes, rollback plans, migration risk, and readiness evidence.

### Documentation Review Board

Approves CEOS documentation changes, templates, runbooks, and durable project memory updates.

## How To Use

Identify impacted boards during planning. If multiple boards apply, the highest-risk board coordinates final approval. Exceptions require [Decision Record Template](../templates/decision-record-template.md) and an expiration or review date.

## Tradeoffs

Boards increase review latency. They reduce irreversible mistakes and create institutional accountability.

## Best Practices

- Bring evidence, not opinions.
- Record decisions and dissent.
- Keep emergency approval paths explicit.
- Review outcomes after production incidents.
- Maintain independence between proposer and approver for high-risk changes.

## Anti-Patterns

- Treating board review as ceremonial.
- Approving without alternatives and tradeoffs.
- Hiding risks to accelerate approval.
- Making permanent exceptions.
- Skipping documentation after verbal approval.

## Related Documents

See [Organization Structure](organization-structure.md), [Architecture Review Playbook](../playbooks/architecture-review-playbook.md), [Release Playbook](../playbooks/release-playbook.md), and [Review Standards](../standards/review-standards.md).

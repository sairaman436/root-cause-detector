# CEOS Manifest

## Purpose

This manifest lists every required operating-system document and the governance role it serves.

## Why

The engineering operating system must be auditable. A manifest lets reviewers verify that the handbook is complete, discoverable, and internally connected.

## Document Inventory

| Area          | Document                                                             | Governance Role                                                           |
| ------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| Root          | [README](README.md)                                                  | Defines CEOS authority, scope, directory map, and change governance.      |
| Constitutions | [Engineering](constitutions/engineering-constitution.md)             | Defines how engineering work is performed.                                |
| Constitutions | [Architecture](constitutions/architecture-constitution.md)           | Governs system structure and architectural change.                        |
| Constitutions | [AI](constitutions/ai-constitution.md)                               | Governs LLM, RAG, agent, and decision intelligence behavior.              |
| Constitutions | [Database](constitutions/database-constitution.md)                   | Governs persistence, migration, lineage, retention, and recovery.         |
| Constitutions | [Security](constitutions/security-constitution.md)                   | Governs IAM, privacy, audit, AI security, and supply chain controls.      |
| Constitutions | [Testing](constitutions/testing-constitution.md)                     | Governs verification and evidence.                                        |
| Constitutions | [DevOps](constitutions/devops-constitution.md)                       | Governs CI/CD, infrastructure, observability, HA, and DR.                 |
| Constitutions | [MLOps](constitutions/mlops-constitution.md)                         | Governs model, prompt, feature, drift, evaluation, and rollback controls. |
| Constitutions | [Documentation](constitutions/documentation-constitution.md)         | Governs documentation quality and lifecycle.                              |
| Constitutions | [Release](constitutions/release-constitution.md)                     | Governs production change and release evidence.                           |
| Standards     | [Repository](standards/repository-standards.md)                      | Defines placement, ownership, and dependency rules.                       |
| Standards     | [Coding](standards/coding-standards.md)                              | Defines production code quality.                                          |
| Standards     | [Testing](standards/testing-standards.md)                            | Defines required test types and evidence.                                 |
| Standards     | [Documentation](standards/documentation-standards.md)                | Defines documentation quality rules.                                      |
| Standards     | [Prompt](standards/prompt-standards.md)                              | Defines prompt governance.                                                |
| Standards     | [AI](standards/ai-standards.md)                                      | Defines AI implementation standards.                                      |
| Standards     | [Review](standards/review-standards.md)                              | Defines review expectations.                                              |
| Standards     | [Workflow](standards/workflow-standards.md)                          | Defines milestone execution and shipping workflow.                        |
| Playbooks     | [Engineering](playbooks/engineering-playbooks.md)                    | Defines implementation execution loop.                                    |
| Playbooks     | [Architecture Review](playbooks/architecture-review-playbook.md)     | Defines architecture approval process.                                    |
| Playbooks     | [Migration](playbooks/migration-playbook.md)                         | Defines migration planning and validation.                                |
| Playbooks     | [Release](playbooks/release-playbook.md)                             | Defines release and rollback procedure.                                   |
| Playbooks     | [Incident Response](playbooks/incident-response-playbook.md)         | Defines incident handling.                                                |
| Playbooks     | [MLOps](playbooks/mlops-playbook.md)                                 | Defines AI artifact lifecycle.                                            |
| Playbooks     | [Production Readiness](playbooks/production-readiness-playbook.md)   | Defines production readiness review.                                      |
| Organization  | [Organization Structure](organization/organization-structure.md)     | Defines teams and ownership.                                              |
| Organization  | [Review Boards](organization/review-boards.md)                       | Defines approval authorities.                                             |
| Templates     | [Decision Record](templates/decision-record-template.md)             | Defines decision record requirements.                                     |
| Templates     | [Architecture Proposal](templates/architecture-proposal-template.md) | Defines architecture proposal requirements.                               |
| Templates     | [Engineering Plan](templates/engineering-plan-template.md)           | Defines implementation planning requirements.                             |
| Templates     | [Security Review](templates/security-review-template.md)             | Defines security review requirements.                                     |
| Templates     | [AI Review](templates/ai-review-template.md)                         | Defines AI review requirements.                                           |
| Templates     | [Release Review](templates/release-review-template.md)               | Defines release review requirements.                                      |
| Templates     | [Incident Review](templates/incident-review-template.md)             | Defines incident review requirements.                                     |
| Memory        | [Project Memory](memory/PROJECT_MEMORY.md)                           | Preserves durable project context and approved milestone history.         |

## Completion Criteria

CEOS is complete when every future engineering change can answer:

- Which constitution applies?
- Which standard applies?
- Which playbook governs execution?
- Which board approves exceptions?
- Which template records the decision?
- Which memory entry preserves durable context?

## Related Documents

See [README](README.md), [Review Boards](organization/review-boards.md), [Documentation Constitution](constitutions/documentation-constitution.md), and [Project Memory](memory/PROJECT_MEMORY.md).

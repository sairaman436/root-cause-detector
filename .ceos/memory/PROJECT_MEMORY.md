# Project Memory

## Purpose

Project Memory preserves durable context that future engineering work must respect.

## Why

The project has moved through architecture, bootstrap, identity, survey, evidence, geospatial, eventing, AI, agents, decision intelligence, and production hardening milestones. Future work must not rediscover or contradict these foundations.

## Permanent Invariants

- The repository is a production-oriented monorepo.
- The core backend is a Spring Boot modular backend using JPA, Flyway, JWT/RBAC, OpenAPI, structured errors, and H2-backed integration tests.
- Frontend applications live under `apps`.
- Python AI and worker services live under `services`.
- Shared packages live under `packages`.
- Infrastructure and deployment are versioned under `infra` and `deploy`.
- Production hardening includes CI/CD, security scanning, SBOM, Kubernetes, Terraform, observability, MLOps policies, DR documentation, and operational APIs.
- Milestone work must preserve approved previous modules unless explicitly required by dependency or defect.
- The user expects milestone work to be committed and pushed to `main` when complete.

## Approved Capability History

- Repository Bootstrap established monorepo foundations, Docker, Compose, CI, config, logging, scripts, and docs.
- Identity and Access Management established users, organizations, roles, permissions, JWT, refresh tokens, RBAC, audit, Flyway, and tests.
- Survey Management established surveys, templates, dynamic questions, workflow, assignments, validation, search, Flyway, and tests.
- Evidence and Asset Management established evidence metadata, storage abstraction, validation, authorization, audit, search, Flyway, and tests.
- Event Streaming established Kafka-oriented event contracts, outbox, consumers, retry, dead-letter, analytics ingestion, and observability.
- Geospatial Intelligence established administrative hierarchy, locations, boundaries, spatial services, infrastructure assets, households, and geospatial APIs.
- AI Foundation established AI gateway, model and prompt registry foundation, embeddings, RAG, vector search, safety, usage, and inference logs.
- Multi-Agent Intelligence established agent registry, tools, memory, orchestration, traces, task planning, feedback, and audit.
- Decision Intelligence established evidence fusion, rule evaluation, hypothesis and root cause generation, recommendations, confidence, decision traces, decision memory, and decision APIs.
- Production Hardening established operations APIs, production profile, operations schema, Kubernetes, Terraform, observability, DevSecOps workflows, MLOps policies, DR/runbooks, and readiness documentation.

## Lessons

- Distinguish design-only requests from implementation requests before editing code.
- Do not implement future milestone capabilities unless explicitly requested.
- Do not call a milestone complete until relevant build, test, format, and deployment-shape validation has been attempted.
- Report unavailable local tooling clearly, such as Terraform CLI or Docker daemon limitations.
- Preserve clean git status and push after milestone completion when requested.

## How To Update

Project Memory updates require Documentation Review Board approval when they change durable project interpretation. Updates that record completed milestones must link release evidence and commit identifiers.

## Related Documents

See [Engineering Constitution](../constitutions/engineering-constitution.md), [Workflow Standards](../standards/workflow-standards.md), [Release Playbook](../playbooks/release-playbook.md), and [Review Boards](../organization/review-boards.md).

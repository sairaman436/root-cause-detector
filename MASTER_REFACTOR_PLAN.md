# Master Refactor Plan

Purpose: Converts the enterprise audit findings into an ordered remediation plan for Production Candidate v1.
Why it exists: The repository now has many completed milestones; refactoring must be sequenced so teams improve production readiness without redesigning approved architecture or disrupting feature ownership.
Architecture fit: This plan preserves the modular monolith core, Next.js portal boundaries, Python AI service boundaries, shared packages, event backbone, and governance documentation while hardening them for enterprise delivery.

Related documents: [MASTER_TECHNICAL_DEBT.md](MASTER_TECHNICAL_DEBT.md), [MASTER_ARCHITECTURE_REPORT.md](MASTER_ARCHITECTURE_REPORT.md), [MASTER_PRODUCTION_CHECKLIST.md](MASTER_PRODUCTION_CHECKLIST.md), `.ceos/playbooks/review-playbook.md`, `.ceos/standards/repository-standards.md`.

## Refactor Principles

1. No feature expansion during production-candidate hardening.
2. Preserve approved bounded contexts and service ownership.
3. Prefer contract stabilization over cosmetic restructuring.
4. Make production safety fail closed, especially for secrets, tenant scope, and AI claims.
5. Convert undocumented behavior into explicit contracts before optimizing it.
6. Require test evidence for each refactor before merging.

## Phase PC1: CI, Branch Protection, and Build Evidence

Objectives:

- Ensure every push and pull request validates all production-critical stacks.
- Make validation evidence repeatable and visible.

Scope:

- Update CI triggers for backend, frontend, Python services, infra validation, and security scans.
- Add required check naming conventions.
- Add coverage report publishing and artifact retention.
- Add `docker compose config`, backend tests, frontend typecheck/test/build, Python tests, and migration validation to a single production-candidate gate.

Dependencies:

- Existing GitHub Actions workflows.
- Existing Maven, npm, Python, Docker Compose configurations.

Success criteria:

- `main` cannot accept changes unless backend, frontend, Python, Compose, formatting, and security checks pass.
- CI logs publish test summaries and coverage artifacts.
- Local commands match CI commands.

Risks:

- CI runtime increases.
- Optional local services such as Ollama and Qdrant may need mocked or profile-controlled checks.

Trade-offs:

- Stronger gates slow down merges but prevent unvalidated milestone claims.

## Phase PC2: API Contract Stabilization

Objectives:

- Freeze public API shape before feature expansion.
- Remove ambiguity between canonical and convenience routes.

Scope:

- Standardize `/api/v1` as the public prefix.
- Classify unversioned aliases as internal, deprecated, or removed.
- Replace direct Spring `Page` serialization with a stable page envelope.
- Add OpenAPI generation and contract diff checks.
- Add endpoint-permission contract tests.

Dependencies:

- Existing controllers and OpenAPI annotations.
- Shared TypeScript and API contract packages.

Success criteria:

- Every public endpoint appears in OpenAPI.
- Every endpoint has an owner, permission, request DTO, response DTO, and error model.
- API compatibility failures block CI.

Risks:

- Existing tests that assume Spring `Page` JSON will need updates.
- Removing aliases can break local scripts if any exist.

Trade-offs:

- A short-term migration cost buys client stability and generated SDK reliability.

## Phase PC3: Security and Tenant Isolation Hardening

Objectives:

- Make enterprise multi-tenant behavior explicit and enforceable.
- Prevent unsafe production startup.

Scope:

- Add production startup validation for JWT secrets, database credentials, object storage credentials, CORS origins, and actuator exposure.
- Define tenant ownership for each bounded context.
- Add tenant or global-scope markers to AI, research, release, governance, optimization, serving, and learning tables.
- Add service-layer tenant filters and authorization tests.
- Add secret scanning allowlist and evidence retention.

Dependencies:

- Identity and RBAC modules.
- Existing security configuration.
- Data migrations.

Success criteria:

- Production profile fails to start with local defaults.
- Tenant-scoped records cannot be listed or modified across tenant boundaries.
- Security tests cover every role group and sensitive endpoint family.

Risks:

- Data migrations may require backfill defaults for existing local data.

Trade-offs:

- More tenant constraints increase schema complexity but are required for government, NGO, and enterprise deployment.

## Phase PC4: Data Platform Productionization

Objectives:

- Prepare storage and analytics foundations for scale.
- Reduce query and retention risk.

Scope:

- Convert queryable JSON `TEXT` columns to PostgreSQL `JSONB`.
- Introduce partition strategy for audit, event, inference, feedback, metric, and outbox tables.
- Add retention and archival runbooks.
- Add PostgreSQL Testcontainers for migration and repository tests.
- Introduce PostGIS-backed spatial columns and indexes where high-volume geospatial queries require them.

Dependencies:

- Flyway migration discipline.
- Existing data architecture documents.

Success criteria:

- High-growth tables have partition, retention, and archive ownership.
- PostgreSQL migration tests run in CI.
- Geospatial radius and boundary queries have database-backed spatial indexes.

Risks:

- Vendor-specific tests are slower than H2.
- JSONB and PostGIS increase PostgreSQL dependency.

Trade-offs:

- Portability decreases, but production correctness and scale improve.

## Phase PC5: AI Runtime Classification and Safety Enforcement

Objectives:

- Separate target AI architecture from current runnable implementation.
- Prevent simulated AI behavior from being released as validated model output.

Scope:

- Label AI endpoints as control-plane, deterministic-local, integration-placeholder, or production-runtime.
- Add runtime response metadata indicating model source and validation status.
- Add prompt injection middleware and guardrail enforcement tests.
- Add model artifact integration gates before any production inference route is promoted.
- Add AI evaluation and red-team evidence as release artifacts.

Dependencies:

- AI, serving, governance, evaluation, optimization, and release modules.

Success criteria:

- No production AI route can serve without a registered, signed, certified artifact.
- AI safety checks run in CI and release workflows.
- Audit logs capture prompt, policy, model routing, citation, and output validation decisions without storing disallowed sensitive content.

Risks:

- Real model integration will require external runtimes, GPUs, or provider credentials.

Trade-offs:

- Strict gates slow AI experimentation but protect enterprise deployment credibility.

## Phase PC6: Observability and SRE Readiness

Objectives:

- Make platform operation measurable before production candidate certification.

Scope:

- Define SLIs and SLOs per bounded context.
- Add Micrometer metrics for request latency, error rate, domain operations, event lag, upload throughput, geospatial query time, and AI-serving latency.
- Add dashboards and alert rules.
- Add runbooks for incident response, rollback, database recovery, and degraded AI mode.

Dependencies:

- Existing actuator, Prometheus, logging, and operations module.

Success criteria:

- Every critical user flow has logs, metrics, traces, and alert ownership.
- Production checklist maps each SLO to dashboard evidence.

Risks:

- Too many low-signal metrics can create alert fatigue.

Trade-offs:

- SRE instrumentation adds implementation overhead but is mandatory for enterprise reliability.

## Phase PC7: Documentation Reconciliation

Objectives:

- Ensure docs match implemented capability.

Scope:

- Add implementation-state labels to architecture, AI, security, MLOps, and release docs.
- Rename or alias `Architecture/templetes` to `Architecture/templates`.
- Add ADRs for implementation deviations and temporary placeholders.
- Create a document index that maps each milestone to code, schema, tests, and validation evidence.

Dependencies:

- Existing `.ceos`, `Architecture`, `docs`, `Requirements`, `System_Design`, `Testing`, and `governance` documents.

Success criteria:

- Operators can distinguish target state from implemented state without reading source code.
- Every public module has current docs, API references, and runbooks.

Risks:

- Documentation churn can mask code changes if not reviewed separately.

Trade-offs:

- Documentation reconciliation is not feature work, but it prevents false production claims.

## Execution Order

1. PC1 and PC2 must complete before any new business milestone.
2. PC3 must complete before external pilot deployments.
3. PC4 must complete before high-volume data ingestion.
4. PC5 must complete before AI production inference.
5. PC6 must complete before Production Candidate v1 certification.
6. PC7 runs continuously, with a full closeout before release tagging.

## Review Boards

Required approvals:

- Architecture Board for API, module, and schema changes.
- Security Board for secrets, tenant isolation, RBAC, and AI guardrails.
- Data Governance Board for retention, lineage, and archival changes.
- SRE Board for observability, scaling, and incident response readiness.
- AI Governance Board for AI runtime classification and model release gates.

## Stop Conditions

Pause the refactor program if:

- A change requires redesigning an approved bounded context.
- A migration could destroy or orphan existing data.
- A security fix requires rotating credentials outside repository control.
- A production-facing endpoint cannot be assigned an owner and permission.

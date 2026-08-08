# RC1 Release Candidate Report

Purpose: Defines Release Candidate 1 for the Sprint 1 MVP.
Why it exists: The platform now has an end-to-end MVP path and needs a controlled release-candidate baseline before production approval.
Architecture fit: RC1 preserves the approved modular monorepo, Spring Boot backend, Next.js portals, Python AI services, PostgreSQL, object storage, and local AI integration boundaries.

Related documents: `MASTER_ARCHITECTURE_REPORT.md`, `MASTER_TECHNICAL_DEBT.md`, `docs/operations/RC1_SECURITY_REPORT.md`, `docs/operations/RC1_TESTING_REPORT.md`, `docs/operations/RC1_PRODUCTION_READINESS_REPORT.md`.

## Release Identity

- Version: `1.0.0-rc.1`
- Classification: release candidate
- Scope: quality, reliability, security, observability, developer experience, and release readiness
- Excluded: new domain features, new AI capabilities, redesigns, and unsupported production certifications

## RC1 Changes

- Added repository-level `VERSION` for semantic release tracking.
- Added production secret validation for unsafe production defaults.
- Added bounded request rate limiting for backend APIs.
- Added stable Spring Data pagination serialization configuration.
- Added RC1 operational, security, performance, testing, migration, rollback, and deployment reports.
- Removed misleading future-integration wording from current production-readiness documents.

## Board Review

| Board              | Decision             | Rationale                                                                                                                                      |
| ------------------ | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Architecture Board | Conditional approval | Module boundaries remain intact; direct feature expansion is deferred.                                                                         |
| Security Board     | Conditional approval | Production weak-secret startup prevention and rate limiting are now present; tenant isolation remains open debt.                               |
| Performance Board  | Conditional approval | No blocking local performance regression found; formal load tests remain required.                                                             |
| AI Board           | Conditional approval | AI behavior is MVP-integrated with deterministic fallbacks and local provider boundaries; model production certification remains out of scope. |
| QA Board           | Conditional approval | Build and test suites are required for RC1 acceptance; E2E coverage remains a follow-up.                                                       |

## Release Decision

RC1 is acceptable for controlled validation and internal pilot environments after the validation commands in `RC1_TESTING_REPORT.md` pass. It is not approved for unrestricted public production traffic until open P0/P1 items in `MASTER_TECHNICAL_DEBT.md` are resolved or formally accepted.

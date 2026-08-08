# RC1 Production Checklist

Purpose: Defines the remaining gates before RC1 can become a production release.
Why it exists: Release candidate status is not the same as production certification.
Architecture fit: The checklist converts CEOS review standards into concrete release gates.

Related documents: `MASTER_PRODUCTION_CHECKLIST.md`, `docs/operations/RC1_SECURITY_REPORT.md`, `docs/operations/RC1_PERFORMANCE_REPORT.md`.

## Required Before Production

- No critical or high unaccepted security vulnerabilities.
- Required CI checks enforced on protected branches.
- Tenant isolation and RBAC/ABAC tests complete.
- Dependency, SBOM, license, and provenance reports archived.
- Load, soak, failover, and disaster-recovery tests complete.
- E2E user workflow test suite complete.
- AI safety, hallucination, citation, and prompt-injection tests complete.
- Observability dashboards and alert routes active.
- Backup and restore drill completed.
- Production runbooks approved by SRE and Security Boards.

## Certification Decision

RC1 can be promoted only when every required gate passes or an accountable executive risk acceptance is recorded.

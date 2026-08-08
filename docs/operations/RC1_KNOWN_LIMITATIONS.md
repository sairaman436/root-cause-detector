# RC1 Known Limitations

Purpose: Lists release-candidate limitations that operators and engineers must understand before deployment.
Why it exists: RC1 must avoid overstating production readiness.
Architecture fit: Limitations are tied to approved architecture areas and the master debt register.

Related documents: `MASTER_TECHNICAL_DEBT.md`, `docs/operations/RC1_PRODUCTION_READINESS_REPORT.md`.

## Limitations

- RC1 is not certified for unrestricted internet-facing production.
- Rate limiting is in-process and must be replaced or complemented by gateway-level controls for multi-replica deployments.
- Tenant isolation is incomplete across later control-plane modules.
- AI behavior requires local provider availability and has not completed enterprise red-team validation.
- Formal load, soak, and capacity tests are not yet archived.
- E2E browser workflow coverage is still open.
- Production cloud deployment and disaster recovery have not been independently certified.
- Some older architecture documents describe target-state capability and should be read with current-state RC1 reports.

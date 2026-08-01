# DevOps Constitution

## Purpose

This constitution governs CI/CD, containers, Kubernetes, infrastructure as code, observability, operations, scaling, reliability, and disaster recovery.

## Why

Enterprise users require predictable deployments, traceable artifacts, repeatable infrastructure, observable systems, and tested recovery paths.

## When It Applies

It applies to workflows, build scripts, Dockerfiles, Compose files, Kubernetes manifests, Terraform modules, monitoring, logging, tracing, alerting, runbooks, and release operations.

## How To Apply

- Use CI/CD gates for formatting, tests, security scans, SBOMs, images, and infrastructure validation.
- Keep deployment manifests versioned and environment-specific.
- Make every service expose health, readiness, liveness, metrics, and structured logs.
- Define resource requests, limits, autoscaling, and network boundaries for production workloads.
- Treat disaster recovery as a tested production feature.

## Tradeoffs

Operational rigor increases repository size and review scope. It prevents undocumented infrastructure, manual drift, and unrepeatable deployments.

## Best Practices

- Prefer declarative infrastructure and GitOps-compatible manifests.
- Keep local, staging, and production configuration contracts aligned.
- Use immutable image tags and provenance for releases.
- Define alerts with runbook links.
- Validate production-like topology before enabling traffic.

## Anti-Patterns

- Manually changing production resources outside IaC.
- Relying on one replica for critical services.
- Deploying without rollback and smoke tests.
- Adding metrics without ownership or thresholds.
- Treating backups as successful without restore validation.

## Related Documents

See [Release Constitution](release-constitution.md), [Security Constitution](security-constitution.md), [Production Readiness Playbook](../playbooks/production-readiness-playbook.md), [Release Playbook](../playbooks/release-playbook.md), and [Operations Manual](../../docs/operations/OPERATIONS_MANUAL.md).

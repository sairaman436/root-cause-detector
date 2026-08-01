# Purpose: Defines the production deployment procedure for the AI Rural platform.

# Why it exists: Gives release engineers a repeatable path from CI artifacts to Kubernetes production.

# Architecture fit: Supports Milestone 11 CI/CD, Kubernetes, Terraform, and global deployment deliverables.

# Production Deployment Guide

## Release Inputs

- Signed container images in GHCR.
- SBOM artifact from `Production Hardening`.
- Terraform plan for `infra/terraform/environments/production`.
- Kubernetes render from `deploy/kubernetes/overlays/production`.
- Vault-backed secret material for database, JWT, object storage, TLS, and Grafana.

## Deployment Flow

1. Run repository quality, backend, frontend, AI services, security, and production hardening workflows.
2. Apply Terraform after plan review and security approval.
3. Sync Kubernetes manifests through GitOps or `kubectl apply -k deploy/kubernetes/overlays/production`.
4. Run the `migration-validation` Job.
5. Verify `/actuator/health/readiness`, `/actuator/prometheus`, `/api/v1/platform/version`, and `/api/v1/platform/deployment-status`.
6. Promote traffic through ingress after smoke tests pass.

## Rollback

- Repoint production overlay image tags to the previous signed image digest.
- Reapply the overlay.
- Validate readiness, smoke tests, and decision confidence dashboards.

# Purpose: Defines administrator responsibilities for operating the production platform.

# Why it exists: Separates operational administration from application feature ownership.

# Architecture fit: Supports Milestone 11 administration, governance, and enterprise readiness.

# Administrator Guide

## Responsibilities

- Manage environment configuration through ConfigMaps and Vault-backed secrets.
- Validate service health and deployment status after releases.
- Review operational dashboards and backup reports.
- Coordinate incident response with SRE, security, MLOps, and data owners.

## Access Controls

- `PLATFORM_ADMIN` can manage operational evidence and production readiness data.
- `PLATFORM_READ` can inspect deployment status and operations dashboards.
- Secrets are never managed directly in Git and must be rotated through the approved secret manager.

## Routine Checks

- Daily: dashboard health, alert queue, backup completion.
- Weekly: restore validation, dependency scan results, drift reports.
- Monthly: access review, incident drill, cost review, license compliance.

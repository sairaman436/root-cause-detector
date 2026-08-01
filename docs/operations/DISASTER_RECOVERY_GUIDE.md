# Purpose: Defines disaster recovery objectives, procedures, and validation evidence.

# Why it exists: Makes backup and restore expectations explicit before production launch.

# Architecture fit: Supports Milestone 11 high availability, backup, PITR, cross-region backup, and restore validation.

# Disaster Recovery Guide

## Objectives

- PostgreSQL RPO: 15 minutes with managed PITR.
- PostgreSQL RTO: 60 minutes for regional database restore.
- Object storage RPO: 60 minutes with versioning and replication.
- Kubernetes workload RTO: 30 minutes when infrastructure remains available.

## Backup Strategy

- Managed PostgreSQL automated backups retain at least 35 days.
- Object storage retains audit and model artifacts for at least 365 days.
- Kubernetes manifests, Terraform state, SBOMs, and deployment evidence are source-controlled or archived.

## Restore Validation

1. Restore database snapshot to isolated environment.
2. Apply migrations and run `migration-validation`.
3. Start services against restored dependencies.
4. Run smoke tests and decision-history read checks.
5. Store evidence in `operations.backup_reports`.

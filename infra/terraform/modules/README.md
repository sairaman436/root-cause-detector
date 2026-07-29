# Terraform Modules

## Purpose

Holds reusable infrastructure modules for production, staging, and local-like cloud environments.

## Why It Exists

Shared modules enforce consistent network, Kubernetes, database, storage, observability, IAM, and secret-management patterns across deployments.

## Architecture Fit

Modules are the lowest-level infrastructure building blocks. Environment stacks under `infra/terraform/environments` compose these modules and provide environment-specific inputs.

## Implementation Notes

Do not place environment-specific credentials, state, or ad hoc resources here. Module interfaces must be versioned, documented, and reviewed as shared platform contracts.

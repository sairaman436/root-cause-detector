# Terraform Environments

## Purpose

Contains environment composition roots for infrastructure provisioning.

## Why It Exists

Production, staging, development, and disaster recovery deployments need separate state, approvals, policies, and blast-radius controls while sharing the same reusable modules.

## Architecture Fit

Each environment stack must compose approved Terraform modules, reference external secret stores, and align with the deployment topology defined in the Engineering Design Specification.

## Implementation Notes

Terraform state files, plans, credentials, and provider caches are intentionally excluded from version control. Environment directories should contain only reviewed configuration, variable schemas, policy bindings, and documentation.

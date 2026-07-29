# Monorepo Structure

## Purpose

Defines the canonical implementation repository structure for the AI Rural Root Cause Discovery Platform.

## Why It Exists

Milestone 1 requires a production-ready foundation before business modules, APIs, authentication, AI workflows, or database tables are implemented.

## Architecture Fit

This document maps the approved Monorepo Blueprint into concrete repository directories.

## Canonical Boundaries

- `apps/`: user-facing applications.
- `services/`: deployable backend and AI services.
- `packages/`: shared contracts and governed libraries.
- `data-platform/`: ingestion, lake, quality, catalog, warehouse.
- `ml-platform/`: feature store, model lifecycle, evaluation, prompts, guardrails.
- `infra/`: Terraform and cloud security baselines.
- `deploy/`: Kubernetes deployment configuration.
- `docs/`: implementation-era documentation.
- `tests/`: cross-system verification suites.
- `scripts/`: developer workflow automation.
- `config/`: non-secret environment templates and secret-reference documentation.


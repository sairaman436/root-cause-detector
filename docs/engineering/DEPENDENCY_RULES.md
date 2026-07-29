# Dependency Rules

## Purpose

Defines allowed and forbidden dependencies across the monorepo.

## Why It Exists

The platform must avoid hidden coupling between frontend apps, backend modules, AI services, shared packages, and infrastructure.

## Architecture Fit

This document implements the dependency rules from the approved Monorepo Blueprint.

## Allowed

- Apps may depend on API contracts and generated clients.
- Services may depend on shared contracts and stable infrastructure packages.
- AI services may depend on Python SDK contracts, model registry clients, and approved data/feature interfaces.
- Infrastructure may reference deployable artifact names, not application internals.

## Forbidden

- Frontend apps must not access databases directly.
- Services must not import another service's private source tree.
- Shared packages must not own cross-domain business logic.
- AI services must not mutate operational state except through approved backend contracts.
- Production code must not depend on test fixtures.

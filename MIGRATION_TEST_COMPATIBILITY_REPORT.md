# Migration Test Compatibility Report

## Purpose

This report records the test-environment correction for PostgreSQL-specific Flyway migrations.

## Root Cause

The `test` Spring profile selected H2 while leaving Flyway enabled. Integration tests using `@ActiveProfiles("test")` therefore executed the production migration chain against H2. Migration V25 uses PostgreSQL-specific SQL, including `gen_random_uuid()`, which H2 cannot parse.

## Fix

- Disabled Flyway only in the H2 test profile.
- Enabled Hibernate `create-drop` for the isolated H2 schema.
- Reused the existing `h2-schemas.sql` through the Hikari connection initialization hook.
- Added a test-only multi-line seed script for the identity, AI model, agent, tool, and authorization reference data required by existing integration contracts.
- Pointed H2 AI integration tests at an unavailable local RAG port so they deterministically exercise the existing PostgreSQL-independent vector fallback, without depending on a live service.

Production PostgreSQL configuration and all production Flyway migrations remain unchanged.

## Verification

- Backend Maven test module: **133 passed, 0 failures, 0 errors, 0 skipped**.
- Targeted migration-affected integrations: passed for decision, recovery, AI, agents, eventing, evidence, geospatial, and survey workflows.
- AI/RAG and dataset contract Python suite: **27 passed**, 1 existing deprecation warning.
- Production Docker stack: core-backend and PostgreSQL healthy.
- Backend actuator health: HTTP 200.
- Flyway production log: 32 migrations validated; schema version 31; schema up to date.
- PostgreSQL migration history: V25 through V31 recorded successful.

## Production Impact

None. The changes are under `src/test/resources` only and affect only the H2 test profile and test fixtures.

## Remaining Blockers

None for the H2/Flyway migration compatibility issue. The Python suite retains its pre-existing Starlette/httpx deprecation warning.

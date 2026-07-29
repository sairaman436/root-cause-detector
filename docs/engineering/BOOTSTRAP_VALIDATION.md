# Bootstrap Validation

## Purpose

Records the current repository bootstrap validation status.

## Why It Exists

Milestone 2 should not begin until the engineering foundation has known checks, known risks, and repeatable remediation steps.

## Architecture Fit

This document supports the approved Engineering Design Specification by turning repository bootstrap into an explicit quality gate.

## Current Known Risk

The stable Next.js dependency line currently carries an npm audit advisory through its internal PostCSS dependency. The repository keeps Next.js because it is the approved frontend framework. This must be revisited before any production UI release, and CI keeps `npm audit` visible as a security signal.

Next.js also emits a known npm-workspaces lockfile patch warning for SWC optional dependencies. The web build script sets `NEXT_IGNORE_INCORRECT_LOCKFILE=true` because the lockfile already contains the required SWC package entries and the warning is not a product build failure.

## Validation Outcome - 2026-07-29

Milestone 1 is structurally valid for Milestone 2 development with two explicit exceptions: this working directory is not yet a Git repository, and the approved Next.js frontend dependency line has an upstream npm audit advisory.

## Checks Passed

- Root formatting: `npm run format:check`
- Frontend linting: `npm run lint`
- Frontend type checking: `npm run typecheck`
- Frontend foundation tests: `npm run test`
- Frontend production build: `npm run build:web`
- Python linting: `python -m ruff check .`
- Python formatting: `python -m ruff format --check .`
- Python foundation tests: `python -m pytest tests\foundation`
- Java reactor build: `.\mvnw.cmd -B -DskipTests=false verify`
- Docker Compose validation: `docker compose config --quiet`
- Repository foundation file check: `powershell -ExecutionPolicy Bypass -File scripts\verify-foundation.ps1`

## Improvements Applied

- Added purpose-bearing README contracts for data lake zones so raw, validated, curated, feature, analytics, RAG, archive, and quarantine storage boundaries are durable.
- Added purpose-bearing README contracts for MLOps boundaries so feature store, prompt registry, model registry, evaluation, guardrails, training, drift, data cards, and model cards are durable.
- Added purpose-bearing README contracts for Terraform modules, Terraform environments, deployment environments, deployment secret templates, ADRs, contract tests, integration tests, and end-to-end tests.
- Verified that forced upgrade to Next.js 16.2.12 does not currently remove audit risk because it retains the PostCSS advisory and introduces a Sharp advisory; the repository remains pinned to the previously verified Next.js 14.2.35 line.

## Remaining Exceptions

- `npm audit --audit-level=moderate` fails with high-severity advisories from Next.js and its internal PostCSS dependency. Treat this as a release-blocking item before any externally exposed production frontend deployment.
- `git status --short` fails because `C:\Users\saira\OneDrive\Desktop\MyProps\CSP` is not initialized as a Git repository. Version-control initialization, first commit, branch protection, and remote CI proof are still required.
- Java builds pass with a local JDK 24 targeting Java 21. Developer machines and CI should standardize on Java 21 for consistency.
- Maven reports empty Java artifacts by design because Milestone 1 intentionally contains no backend or shared Java implementation.

## Operational Foundation Outcome - 2026-07-29

The repository now has runnable service shells for the Spring Boot backend, both Next.js portals, and all Python service boundaries. Each runtime service exposes a health endpoint and participates in Docker Compose service networking.

## Additional Checks Passed

- Full local foundation script: `powershell -ExecutionPolicy Bypass -File scripts\build-all.ps1`
- Core backend executable Spring Boot jar packaging
- Web portal health route test and production build
- Admin portal health route test and production build
- AI inference service install and health test
- RAG service install and health test
- Agent orchestrator install and health test
- Reporting service install and health test
- Notification service install and health test
- Docker Compose service topology validation

## Additional Exception

- Docker image build and full Compose runtime startup could not be executed because Docker Desktop's Linux engine was not running: `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`.

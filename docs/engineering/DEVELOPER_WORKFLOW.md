# Developer Workflow

## Purpose

Defines the local development workflow for the production monorepo.

## Why It Exists

Developers need a repeatable process for making changes across Java, TypeScript, Python, infrastructure, and documentation modules.

## Architecture Fit

This workflow operationalizes the approved engineering foundation and prepares the team for Milestone 2 implementation.

## Standard Flow

1. Read the relevant ADR and module README.
2. Create a scoped feature branch.
3. Make changes inside the owning module.
4. Run local checks with `scripts/verify-foundation.ps1` and stack-specific tooling.
5. Update contracts, tests, and docs when behavior changes.
6. Open a pull request with affected modules and rollback notes.
7. Wait for CODEOWNERS and CI approval.

## Local Infrastructure

Use `scripts/dev-up.ps1` to start local dependencies and `scripts/dev-down.ps1` to stop them.

## Operational Foundation Workflow

Run the full foundation build before starting feature work:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-all.ps1
```

The script verifies the Spring Boot backend, both Next.js portals, all Python service shells, repository formatting, linting, tests, production frontend builds, and Docker Compose configuration.

Start the local platform foundation:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev-up.ps1
```

Stop the local platform foundation:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev-down.ps1
```

## Local Service Ports

- Core backend: `http://localhost:8080/actuator/health`
- Web portal: `http://localhost:3000/api/health`
- Admin portal: `http://localhost:3001/api/health`
- AI inference service: `http://localhost:8101/health`
- RAG service: `http://localhost:8102/health`
- Agent orchestrator: `http://localhost:8103/health`
- Reporting service: `http://localhost:8104/health`
- Notification service: `http://localhost:8105/health`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka-compatible broker: `localhost:19092`
- MinIO API: `http://localhost:9000`
- MinIO console: `http://localhost:9001`
- Prometheus: `http://localhost:9090`

## Configuration Rules

Local non-secret defaults are defined in `config/env/local.example.env`. Production keys and secret references are defined in `config/env/production.example.env`.

Real secrets must not be written to repository files. Runtime secrets must come from the approved external secret manager or local untracked environment files.

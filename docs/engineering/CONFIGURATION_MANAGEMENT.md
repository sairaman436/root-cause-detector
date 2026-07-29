# Configuration Management

## Purpose

Defines how runtime configuration is represented across local, staging, and production environments.

## Why It Exists

The platform must avoid hardcoded environment values and must separate non-secret configuration from secret material.

## Architecture Fit

This document implements the approved configuration management strategy for services, Docker Compose, Kubernetes, and CI/CD.

## Rules

- Store non-secret examples under `config/env`.
- Store only secret references or templates under `config/secrets`.
- Use runtime environment variables for service configuration.
- Use managed secret stores for real secrets.
- Keep production values out of the repository.

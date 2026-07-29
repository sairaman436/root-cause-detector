# Secrets Strategy

## Purpose

Defines repository-level handling for credentials, keys, tokens, and secret references.

## Why It Exists

The platform will handle sensitive evidence, AI outputs, reports, and public-sector workflows. Secret hygiene must exist before implementation begins.

## Architecture Fit

This document supports the approved Security & Governance Architecture.

## Rules

- Never commit real secrets.
- Use managed secret stores or Kubernetes secret integrations.
- Use separate secrets per environment.
- Rotate secrets on schedule and after suspected exposure.
- Prefer workload identity over long-lived cloud credentials.
- Treat AI provider keys and model registry credentials as high-sensitivity secrets.

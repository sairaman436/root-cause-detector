# Testing Strategy

## Purpose

Defines the Milestone 1 testing foundation and future test-suite locations.

## Why It Exists

The development team needs a shared test layout before implementing business workflows.

## Architecture Fit

This document supports the approved testing strategy across backend, frontend, AI services, data platform, infrastructure, security, RAG, and agents.

## Test Locations

- Unit tests live inside each owning app, service, or package.
- Cross-service contract tests live in `tests/contract`.
- Integration tests live in `tests/integration`.
- End-to-end tests live in `tests/e2e`.
- AI, RAG, and agent evaluations live in their dedicated `tests/*-evaluation` directories.

## Milestone 1 Scope

Only test framework configuration is established. Business test cases begin in later milestones.

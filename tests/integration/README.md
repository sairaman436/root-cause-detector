# Integration Tests

## Purpose

Contains tests that verify behavior across service, database, messaging, storage, and infrastructure boundaries.

## Why It Exists

Unit tests cannot prove that platform components are wired correctly. Integration tests provide confidence in local and CI environments before production hardening.

## Architecture Fit

Integration tests should run against controlled dependencies from Docker Compose or ephemeral CI services and must avoid production resources.

## Implementation Notes

Milestone 1 does not add business integration tests. Future tests should be deterministic, isolated, and observable through CI logs and artifacts.

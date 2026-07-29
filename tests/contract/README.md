# Contract Tests

## Purpose

Contains tests that verify published API, event, SDK, and shared-package contracts.

## Why It Exists

The platform is service-oriented and event-driven. Contract tests prevent one service from silently breaking consumers as APIs and events evolve.

## Architecture Fit

Contract tests validate the boundaries defined in `packages/api-contracts`, `packages/event-contracts`, generated clients, and service-to-service integration agreements.

## Implementation Notes

Milestone 1 includes the directory contract only. Add tests here when Milestone 2 introduces real service contracts.

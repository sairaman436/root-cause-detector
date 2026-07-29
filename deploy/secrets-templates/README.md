# Secrets Templates

## Purpose

Documents required runtime secrets without storing secret values.

## Why It Exists

Engineering teams need a clear contract for required secret names, owners, rotation cadence, and consuming services before Milestone 2 implementation begins.

## Architecture Fit

Templates in this directory map application configuration to the approved secrets-management architecture. They support local onboarding, CI validation, and production readiness reviews.

## Implementation Notes

Only schemas, examples with non-sensitive dummy values, and ownership notes belong here. Real secrets must live in the approved external secret manager.

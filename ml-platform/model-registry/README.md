# Model Registry

## Purpose

Defines repository-side contracts for model registration, versioning, approval, deployment eligibility, and rollback.

## Why It Exists

The model registry is the governance control point between experimentation and production model serving.

## Architecture Fit

This boundary connects training, evaluation, security approval, canary deployment, monitoring, and retraining workflows.

## Implementation Notes

Milestone 1 includes only registry conventions. Model artifacts and registry state must live in the approved runtime registry service.

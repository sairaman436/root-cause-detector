# Training Pipelines

## Purpose

Defines the repository boundary for governed model training pipeline specifications.

## Why It Exists

Training pipelines must be reproducible, observable, policy-compliant, and connected to datasets, features, evaluations, and approvals.

## Architecture Fit

This boundary implements the MLOps lifecycle between data preparation, experiment tracking, model registry, and deployment strategy.

## Implementation Notes

Milestone 1 does not include pipeline code. Future pipeline definitions should reference approved runtime orchestrators, storage locations, credentials, and quality gates.

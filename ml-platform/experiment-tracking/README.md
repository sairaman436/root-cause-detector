# Experiment Tracking

## Purpose

Defines the repository boundary for experiment metadata, tracking conventions, and reproducibility contracts.

## Why It Exists

Experiment tracking preserves lineage from dataset versions, feature versions, prompt versions, code versions, parameters, metrics, and artifacts to model approval decisions.

## Architecture Fit

This boundary supports the approved MLOps lifecycle from research through governed release.

## Implementation Notes

Runtime experiment artifacts belong in the configured experiment tracking system, not in this repository.

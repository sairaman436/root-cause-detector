# Feature Store

## Purpose

Defines contracts for offline and online feature groups used by prediction, analytics, and monitoring.

## Why It Exists

Feature store governance prevents training-serving skew and gives teams reusable, versioned, discoverable feature definitions.

## Architecture Fit

This boundary connects operational data, lake zones, training pipelines, model serving, drift monitoring, and lineage.

## Implementation Notes

Milestone 1 includes only the feature-store boundary. Future work should add feature definitions, owners, freshness guarantees, and access policies.

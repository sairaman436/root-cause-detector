# Data Lake Zones

## Purpose

Defines the physical data lake zone boundaries approved for the platform.

## Why It Exists

The enterprise data architecture separates ingestion, validation, analytics, AI retrieval, and archival concerns. Keeping these zones explicit prevents implementation teams from mixing raw source data with governed analytical or AI-ready datasets.

## Architecture Fit

The data platform uses these zones as the storage contract for batch pipelines, streaming sinks, feature generation, analytics warehouse loading, RAG corpus preparation, retention jobs, and disaster recovery.

## Zone Ownership

- `raw`: Immutable source-aligned landing area owned by Data Platform Engineering.
- `validated`: Quality-checked data owned by Data Platform Engineering and domain data stewards.
- `curated`: Domain-modeled data products owned by domain teams with data governance oversight.
- `feature`: Offline feature datasets owned by MLOps and Data Platform Engineering.
- `analytics`: Warehouse-ready extracts owned by Analytics Engineering.
- `rag`: Retrieval-ready document and chunk payloads owned by AI Platform Engineering.
- `archive`: Retention-controlled cold storage owned by Platform Operations.
- `quarantine`: Rejected or policy-blocked records owned by Data Governance and Security.

## Implementation Notes

No application data is stored in this repository. Runtime deployments must map each zone to governed object storage buckets or prefixes with lifecycle rules, encryption, lineage capture, and access controls.

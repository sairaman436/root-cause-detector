# DATASET v0.3 QUALITY REPORT

Materialized only from persisted v0.3-compatible APPROVE or validated CORRECT decisions.
Pending, rejected, legacy, and synthetic records were excluded. No training was performed.

- Approved: `31`
- Corrected: `0`
- Rejected: `0`
- Pending: `0`
- Total examples: `28`
- Root-cause count: `10`
- Recommendation count: `9`
- RAG count: `9`
- Train: `23`
- Validation: `2`
- Test: `3`
- Maximum output words: `588` / `1024`
- Approved records excluded by contract: `3`
- Exclusion reasons: `{"contract:output:exceeds_sequence_length": 3}`
- Canonical schema: `passed`
- Citation/source-ID contract: `passed`
- Evidence context: `passed`
- PII: `passed`
- Duplicates: `0`
- Scenario leakage: `0`
- Split leakage: `0`
- Synthetic examples: `0`

## Task Distribution

- `root-cause-analysis`: `10`
- `recommendation-generation`: `9`
- `rag-grounded-responses`: `9`

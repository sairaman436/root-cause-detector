# DATASET QUALITY REPORT

Dataset version: `dataset-v0.1`

## Scope

This report covers deterministic materialization of approved or validated human-corrected records. No training or fine-tuning is performed.

## Counts

- Source export available: `True`
- Accepted examples: `6`
- Rejected examples: `0`
- Train: `4`
- Validation: `1`
- Test: `1`
- Split leakage count: `0`

- Synthetic examples: `0`
- Development only: `False`

## Task Counts

- `root-cause-analysis`: `4`
- `recommendation-generation`: `1`
- `rag-grounded-responses`: `1`

## Validation Failures

- None

## Artifacts

- `root-cause-analysis.jsonl`
- `recommendation-generation.jsonl`
- `rag-grounded-responses.jsonl`
- `manifest.json`

Approved or validated corrected governed records were materialized from the authenticated candidate export. No training or fine-tuning was performed.

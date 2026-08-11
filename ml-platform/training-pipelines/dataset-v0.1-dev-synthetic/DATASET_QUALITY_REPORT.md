# DATASET QUALITY REPORT

Dataset version: `dataset-v0.1-dev-synthetic`

## Scope

This report covers deterministic materialization of approved or validated human-corrected records. No training or fine-tuning is performed.

## Counts

- Source export available: `True`
- Accepted examples: `9`
- Rejected examples: `0`
- Train: `3`
- Validation: `3`
- Test: `3`
- Split leakage count: `0`

- Synthetic examples: `9`
- Development only: `True`

## Task Counts

- `root-cause-analysis`: `3`
- `recommendation-generation`: `3`
- `rag-grounded-responses`: `3`

## Validation Failures

- None

## Artifacts

- `root-cause-analysis.jsonl`
- `recommendation-generation.jsonl`
- `rag-grounded-responses.jsonl`
- `manifest.json`

This is a synthetic development-only fixture. It must not be treated as real field data or promoted to a production training dataset.

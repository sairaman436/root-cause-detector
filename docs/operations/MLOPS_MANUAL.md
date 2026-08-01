# Purpose: Defines production MLOps lifecycle operations.

# Why it exists: Makes model, prompt, embedding, drift, and evaluation controls executable.

# Architecture fit: Supports Milestone 11 MLOps, AI governance, and continuous learning.

# MLOps Manual

## Lifecycle

1. Register dataset and feature lineage with a data card.
2. Train or update model artifact in an isolated pipeline.
3. Run evaluation suites for quality, safety, groundedness, and bias.
4. Register model card and approval evidence.
5. Promote through validated, approved, canary, and production stages.
6. Monitor drift, confidence, override rate, and acceptance rate.

## Rollback

- Roll back to the registry `rollbackVersion`.
- Freeze prompt and model promotion.
- Route consequential workflows to human review.
- Create a retraining or evaluation task before re-enabling promotion.

# MLOps Constitution

## Purpose

This constitution governs datasets, features, experiments, model registry, prompt registry, model approval, deployment, drift, rollback, monitoring, and AI compliance.

## Why

AI decision intelligence requires evidence that models and prompts are fit for use, traceable, monitored, and reversible.

## When It Applies

It applies to training pipelines, evaluation pipelines, feature stores, model artifacts, prompt artifacts, embedding versions, retrieval policies, drift monitoring, human review, and continuous learning.

## How To Apply

- Register every production model, prompt, embedding strategy, and evaluation report.
- Require data cards and model cards before promotion.
- Use canary or shadow deployment for consequential model changes.
- Monitor drift, hallucination, confidence calibration, citation faithfulness, and override rates.
- Roll back automatically or route to human review when safety thresholds are breached.

## Tradeoffs

MLOps governance can delay experimentation. It protects production decision quality while preserving controlled innovation through experimental environments.

## Best Practices

- Separate research experiments from production registry artifacts.
- Version datasets, feature definitions, embeddings, prompts, and models together.
- Keep evaluation suites representative of rural governance and policy decision use cases.
- Track cost, latency, accuracy, safety, and explainability together.
- Treat feedback as labeled evidence requiring review before learning.

## Anti-Patterns

- Promoting a model without rollback metadata.
- Using online feedback directly for retraining.
- Comparing models without fixed evaluation datasets.
- Ignoring low-confidence or high-override decisions.
- Treating prompt changes as harmless text edits.

## Related Documents

See [AI Constitution](ai-constitution.md), [AI Standards](../standards/ai-standards.md), [Prompt Standards](../standards/prompt-standards.md), [MLOps Playbook](../playbooks/mlops-playbook.md), and [AI Review Template](../templates/ai-review-template.md).

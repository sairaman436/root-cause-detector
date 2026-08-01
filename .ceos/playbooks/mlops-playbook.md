# MLOps Playbook

## Purpose

This playbook defines how AI artifacts move from experimentation to governed production use.

## Why

Models, prompts, embeddings, and retrieval policies can change recommendations and institutional decisions. They require production-grade lifecycle management.

## When

Use this for new models, model updates, prompt updates, embedding changes, RAG changes, evaluation changes, drift responses, and AI rollback.

## How

1. Register dataset and lineage through a data card.
2. Define evaluation suite and acceptance thresholds.
3. Register model or prompt metadata.
4. Run quality, safety, groundedness, bias, latency, and cost evaluations.
5. Obtain MLOps and AI Governance approvals.
6. Deploy through shadow or canary when consequential.
7. Monitor drift, confidence, hallucination, citations, overrides, and user feedback.
8. Roll back or route to human review when thresholds are breached.

## Tradeoffs

Governance slows raw experimentation but keeps production AI accountable and reversible.

## Best Practices

- Keep experiment tracking separate from production registry.
- Use fixed evaluation datasets for comparisons.
- Store all artifact versions in decision traces.
- Treat prompt changes as releases.
- Review feedback before using it for learning.

## Anti-Patterns

- Promoting a model because a demo looked good.
- Updating prompts without versioning.
- Ignoring cost and latency.
- Training on ungoverned sensitive data.
- Failing to test prompt injection against retrieved content.

## Related Documents

See [MLOps Constitution](../constitutions/mlops-constitution.md), [AI Constitution](../constitutions/ai-constitution.md), [Prompt Standards](../standards/prompt-standards.md), [AI Review Template](../templates/ai-review-template.md), and [docs/operations/MLOPS_MANUAL.md](../../docs/operations/MLOPS_MANUAL.md).

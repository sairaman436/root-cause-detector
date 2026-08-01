# AI Constitution

## Purpose

This constitution governs all AI, LLM, RAG, embedding, agent, recommendation, and decision intelligence behavior.

## Why

The platform exists to produce explainable, evidence-backed, confidence-scored decisions. AI capabilities must increase decision quality without hiding uncertainty, bypassing policy, leaking private data, or producing unsupported recommendations.

## When It Applies

It applies to prompts, model configuration, embeddings, RAG retrieval, agents, model registry changes, decision intelligence workflows, evaluations, feedback loops, guardrails, and human review policies.

## How To Apply

- Separate prediction, reasoning, and knowledge retrieval in design and telemetry.
- Treat prompts, models, embeddings, policies, and tools as versioned production artifacts.
- Require citations and decision traces for consequential outputs.
- Use deterministic fallbacks for tests and CI when external AI providers are not required.
- Route high-impact or low-confidence recommendations to human review.
- Evaluate AI changes using [MLOps Constitution](mlops-constitution.md) and [AI Standards](../standards/ai-standards.md).

## Tradeoffs

Governed AI systems may respond more slowly and decline unsupported outputs more often. That is acceptable because trust, traceability, and safety are primary product qualities.

## Best Practices

- Use retrieval only from approved knowledge sources with metadata lineage.
- Store prompt version, model version, retrieval sources, confidence, and guardrail decisions.
- Make hallucination, citation faithfulness, and policy compliance measurable.
- Prefer tool-scoped agents over broad autonomous agents.
- Capture user feedback as governance data, not automatic retraining input.

## Anti-Patterns

- Presenting generated reasoning as fact without evidence.
- Allowing prompts to contain hidden policy changes.
- Using agent output without provenance.
- Treating model accuracy as the only quality metric.
- Using production user data for training without approved data governance.

## Related Documents

See [MLOps Constitution](mlops-constitution.md), [Security Constitution](security-constitution.md), [Prompt Standards](../standards/prompt-standards.md), [AI Standards](../standards/ai-standards.md), and [MLOps Playbook](../playbooks/mlops-playbook.md).

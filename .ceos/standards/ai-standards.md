# AI Standards

## Purpose

This standard defines implementation requirements for AI services, RAG, embeddings, agents, recommendations, and decision intelligence.

## Why

AI must be trustworthy, explainable, governed, measurable, and resilient under imperfect evidence.

## When

Apply this standard to AI gateway changes, model registry changes, prompt registry changes, embedding changes, RAG retrieval, agent orchestration, decision intelligence, and evaluation workflows.

## How

- Separate prediction, reasoning, retrieval, and recommendation components.
- Version models, prompts, embeddings, chunking strategy, rules, tools, and evaluation datasets.
- Capture citations, confidence, safety checks, and decision traces.
- Use guardrails for prompt injection, unsafe output, sensitive data, and unsupported claims.
- Use human review for consequential, low-confidence, or policy-conflicted outputs.

## Tradeoffs

Explainable AI may require additional storage, latency, and review workflows. These costs are justified by auditability and institutional trust.

## Best Practices

- Keep retrieval metadata rich enough for citations and lineage.
- Use hybrid search and re-ranking where evidence quality requires it.
- Measure hallucination, groundedness, confidence calibration, latency, token cost, and override rate.
- Keep agents tool-scoped and failure-aware.
- Use deterministic CI fallbacks when external models are unavailable.

## Anti-Patterns

- Storing generated outputs without trace metadata.
- Making autonomous decisions without approval policy.
- Combining all AI logic into one opaque “assistant.”
- Ignoring contradictory evidence.
- Using model output as policy authority.

## Related Documents

See [AI Constitution](../constitutions/ai-constitution.md), [Prompt Standards](prompt-standards.md), [MLOps Playbook](../playbooks/mlops-playbook.md), and [ml-platform/evaluation/evaluation-framework.yaml](../../ml-platform/evaluation/evaluation-framework.yaml).

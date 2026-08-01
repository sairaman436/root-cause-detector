# AI Review Template

## Purpose

Use this template for AI, prompt, model, embedding, RAG, agent, and decision intelligence changes.

## Required Content

### Intended Use

Describe the user workflow and decision context.

### Artifact Versions

List model, prompt, embedding, retrieval, rule, tool, and evaluation versions.

### Inputs And Outputs

Describe accepted inputs, output contract, citations, confidence, and refusal behavior.

### Safety And Guardrails

Document prompt injection protection, sensitive data handling, unsupported claim handling, and escalation rules.

### Evaluation

List datasets, metrics, thresholds, results, and regression risks.

### Human Review

Define when humans must approve, override, or reject outputs.

### Monitoring

Define drift, hallucination, citation, confidence, override, latency, and cost monitoring.

## Anti-Patterns

- Reviewing AI output subjectively without evaluation data.
- Omitting prompt and model version.
- Ignoring low-confidence behavior.
- Allowing retrieval content to override policy.

## Related Documents

See [AI Constitution](../constitutions/ai-constitution.md), [MLOps Constitution](../constitutions/mlops-constitution.md), [Prompt Standards](../standards/prompt-standards.md), and [MLOps Playbook](../playbooks/mlops-playbook.md).

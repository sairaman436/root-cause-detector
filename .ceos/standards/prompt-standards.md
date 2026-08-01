# Prompt Standards

## Purpose

This standard governs system prompts, task prompts, RAG prompts, agent instructions, and prompt registry artifacts.

## Why

Prompts can change system behavior as much as code. They must be versioned, reviewed, evaluated, and safe.

## When

Apply this standard whenever creating or modifying prompts for AI services, agents, RAG, decision intelligence, evaluation, or administrative assistants.

## How

- Assign every production prompt a stable key and semantic version.
- State intended use, inputs, outputs, constraints, safety rules, citation requirements, and fallback behavior.
- Record model compatibility and evaluation suite.
- Require prompt injection tests for prompts that consume user or retrieved content.
- Store prompt version in traces for generated decisions.

## Tradeoffs

Prompt governance can slow iteration. It prevents silent behavior drift and improves incident investigation.

## Best Practices

- Keep policy instructions separate from user content.
- Explicitly require citations when using retrieval.
- Include refusal and escalation criteria.
- Limit prompt length and data exposure.
- Evaluate prompts with adversarial and domain-specific cases.

## Anti-Patterns

- Editing prompts directly in runtime configuration without review.
- Embedding secrets or internal credentials in prompts.
- Allowing retrieved content to override system policy.
- Using prompts that cannot be traced to a release.
- Treating prompt changes as documentation-only.

## Related Documents

See [AI Constitution](../constitutions/ai-constitution.md), [MLOps Constitution](../constitutions/mlops-constitution.md), [AI Standards](ai-standards.md), and [AI Review Template](../templates/ai-review-template.md).

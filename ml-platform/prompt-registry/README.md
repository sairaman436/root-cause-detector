# Prompt Registry

## Purpose

Defines conventions for prompt versioning, approval, evaluation, rollout, and rollback.

## Why It Exists

Prompts are production AI assets. They require the same traceability, testing, and governance controls as model and policy artifacts.

## Architecture Fit

This boundary supports RAG, LLM orchestration, agents, guardrails, human review, and hallucination detection.

## Implementation Notes

No production prompts are introduced in Milestone 1. Future prompt assets must include purpose, owner, input schema, output schema, safety constraints, evaluation evidence, and approval status.

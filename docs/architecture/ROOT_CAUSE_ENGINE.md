# Root Cause Engine

## Purpose

Defines the implemented root-cause intelligence engine that turns rural problem data, survey responses, uploaded evidence, structured data, retrieved knowledge, and local Qwen/RAG context into a fact-separated decision-support analysis.

## Pipeline

The engine normalizes a problem representation, extracts observed facts, retrieves trusted knowledge through the existing RAG/Qwen path, scores evidence, identifies contributing factors, generates multiple candidate root causes, compares alternative hypotheses, builds a causal graph, records uncertainty, and requires human review when confidence is limited.

## Boundaries

The engine does not fine-tune models, train predictors, create autonomous agents, or claim scientific causality. It uses cautious language such as "may contribute to" and "evidence suggests" unless stronger evidence is available.

## API

- `POST /api/v1/analysis/root-cause`
- `GET /api/v1/analysis/root-cause/{id}`
- `GET /api/v1/analysis/root-cause/{id}/evidence`
- `GET /api/v1/analysis/root-cause/{id}/causal-graph`
- `POST /api/v1/analysis/root-cause/{id}/review`
- `POST /api/v1/analysis/root-cause/{id}/regenerate`

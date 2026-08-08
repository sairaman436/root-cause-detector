# RAG Evaluation

## Purpose

Defines deterministic quality checks for retrieval relevance, citation accuracy, groundedness, and insufficient-evidence handling.

## Current Evaluation Set

The CI-safe test set covers trusted water-policy ingestion, metadata-filtered agriculture retrieval, untrusted-source rejection, citation validation, and refusal on unrelated queries.

## Metrics

The current automated gates measure ingestion success, chunk count, retrieval result count, top citation identity, citation validation status, support status, and insufficient-evidence refusal. Runtime observability tracks retrieval latency, inference latency, result counts, citation counts, and support outcomes.

## Groundedness Policy

Answers are generated only after evidence passes the configured score threshold. The answer must cite retrieved evidence and must state insufficient evidence instead of inventing unsupported claims.

## Expansion Path

The evaluation framework can add curated government documents, multilingual questions, PDF/DOCX fixtures, Qdrant-only integration tests, and live Qwen equivalence checks. These expansions should not weaken the fail-closed behavior.

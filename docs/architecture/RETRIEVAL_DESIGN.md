# Retrieval Design

## Purpose

Defines how the platform retrieves ranked evidence for RAG and evidence-only search.

## Hybrid Retrieval

Retrieval combines deterministic vector similarity, keyword overlap, and metadata filter boosts. Filters support domain, language, source, document type, publisher, document version, and future geography fields. Qdrant is used for vector candidate discovery when available; a local deterministic score keeps tests and offline development reproducible.

## Reranking

The current reranker is a weighted hybrid scorer. It favors vector relevance while retaining keyword precision and metadata intent. The reranking abstraction allows a cross-encoder or local rerank model to replace the scorer later without changing API contracts.

## Evidence Threshold

The RAG service refuses to answer when no citation exceeds `RAG_MIN_EVIDENCE_SCORE`. This prevents low-confidence retrieval from being converted into unsupported LLM output.

## Observability

The service records query IDs, retrieval latency, result counts, support status, citation counts, and validation status. Query logs are redacted and truncated to avoid storing sensitive content.

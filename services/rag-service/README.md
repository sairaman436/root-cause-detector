# RAG Service

## Purpose

Provides trusted document ingestion, parsing, cleaning, structure-aware chunking, deterministic embeddings, Qdrant indexing, hybrid retrieval, citation validation, and grounded answer generation.

## Why It Exists

The platform must not rely on hardcoded knowledge or unsupported LLM claims. This service converts approved rural knowledge sources into citation-grade evidence and refuses to answer when retrieval confidence is insufficient.

## Architecture Fit

RAG remains separate from prediction and root-cause reasoning. The Spring Boot backend is the secured API boundary, Qdrant is the vector store, and the AI inference service is the local Qwen/Ollama gateway used after evidence retrieval.

## Local Workflow

1. Start the stack with `docker compose up --build`.
2. Upload trusted documents through `POST /api/v1/knowledge/documents`.
3. Search evidence through `POST /api/v1/knowledge/search`.
4. Generate grounded answers through `POST /api/v1/ai/rag/query`.
5. Inspect validated citations through `GET /api/v1/ai/rag/citations`.

Documents must include title, source, publisher, domain, language, document type, and `approved_source=true`. Unsupported or corrupted files are rejected before indexing.

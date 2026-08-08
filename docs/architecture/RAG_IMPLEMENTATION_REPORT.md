# RAG Implementation Report

## Purpose

Records the implemented Knowledge Retrieval and RAG milestone.

## Implemented Components

- Trusted ingestion for TXT, Markdown, PDF, and DOCX.
- Required provenance metadata and SHA-256 duplicate detection.
- Structure-aware chunking with page, section, document, source, version, language, and domain metadata.
- Deterministic embedding provider with model, version, and dimension metadata.
- Qdrant collection creation, point upsert, vector search, metadata filtering, and local fallback for CI.
- Hybrid retrieval using vector similarity, keyword scoring, metadata filters, and weighted reranking.
- Citation validation against indexed chunk IDs.
- Evidence threshold refusal for insufficient retrieval confidence.
- RAG prompt boundary that treats retrieved content as data and calls the AI inference service in Docker.
- Backend proxy APIs for document ingestion, search, reindexing, RAG query, and citations.
- PostgreSQL migration for knowledge documents, versions, chunks, collections, embeddings, retrieval queries, retrieval results, citation records, and knowledge audit.

## API Surface

- `POST /api/v1/knowledge/documents`
- `GET /api/v1/knowledge/documents`
- `GET /api/v1/knowledge/documents/{id}`
- `POST /api/v1/knowledge/search`
- `POST /api/v1/knowledge/reindex`
- `POST /api/v1/ai/rag/query`
- `GET /api/v1/ai/rag/citations`

## Validation

`python -m pytest services/rag-service/tests` passed with coverage of health, trusted ingestion, untrusted rejection, hybrid retrieval, citation validation, metadata filtering, and insufficient-evidence refusal.

## Known Constraints

The default embedding provider is deterministic for reproducibility and can be replaced by a local embedding model through the existing provider boundary. The RAG service currently keeps operational state in a Docker volume-backed JSON store while PostgreSQL owns the enterprise metadata schema.

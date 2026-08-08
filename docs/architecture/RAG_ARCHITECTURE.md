# RAG Architecture

## Purpose

Defines the production Retrieval-Augmented Generation layer connecting trusted rural knowledge to the local Qwen/Ollama inference path.

## Why It Exists

The platform needs answers grounded in approved evidence, not model memory. RAG separates knowledge retrieval from model reasoning so every generated answer can be inspected, cited, refused, and audited.

## Architecture Fit

The Spring Boot backend remains the authenticated API boundary. `rag-service` owns ingestion, chunking, embeddings, retrieval, reranking, and citation validation. Qdrant stores vectors. `ai-inference-service` serves the local model. PostgreSQL stores governance, lineage, retrieval, citation, and audit metadata.

## Pipeline

Documents enter through `/api/v1/knowledge/documents`, are parsed by MIME type, cleaned, checked for prompt-injection patterns, chunked by document structure, embedded with a deterministic provider, and indexed into Qdrant. Queries pass safety validation in the backend, retrieval in `rag-service`, grounded answer generation through the AI inference service, then citation validation before a response is returned.

## Boundaries

RAG retrieves knowledge. It does not predict outcomes, train models, create agents, or treat document content as executable instructions. Retrieved content is always data.

## Trade-offs

The initial embedding provider is deterministic and local for reproducibility and CI stability. This is less semantically strong than a dedicated embedding model, but the abstraction allows a local embedding model to replace it without changing the API or citation contract.

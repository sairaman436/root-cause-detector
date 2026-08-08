# Knowledge Pipeline

## Purpose

Documents how trusted knowledge moves from ingestion to citation-ready retrieval.

## Flow

`Document -> Ingestion -> Parsing -> Cleaning -> Metadata Extraction -> Chunking -> Embedding -> Qdrant -> Hybrid Retrieval -> Reranking -> Evidence Context -> Qwen -> Structured Answer -> Citation Validation -> User`

## Supported Inputs

The service accepts TXT, Markdown, PDF, and DOCX. Each document must include title, source, publisher, language, domain, document type, version, and an approved-source flag. Checksums provide duplicate detection and version traceability.

## Processing Rules

Parsing is deterministic. Cleaning normalizes whitespace and records prompt-injection indicators. Chunking preserves headings, page markers, paragraphs, and section names before splitting oversized sections. Each chunk keeps document ID, chunk ID, source, page, section, version, domain, language, checksum, embedding model, and embedding version.

## Failure Handling

Unsupported MIME types return `415`. Oversized files return `413`. Untrusted sources return `403`. Corrupted or unparseable documents return `422`. Duplicate checksums return `409`.

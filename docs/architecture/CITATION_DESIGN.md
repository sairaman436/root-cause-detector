# Citation Design

## Purpose

Defines citation validation for grounded RAG answers.

## Citation Fields

Every returned citation includes source ID, document ID, chunk ID, title, source, source URL, publisher, page, section, excerpt, score, domain, language, document type, document version, embedding model, embedding version, and checksum.

## Validation Rules

A citation is valid only when the chunk ID exists in the active knowledge store and the excerpt is non-empty. Invalid or unresolved citations are not displayed. When citations are required and validation fails, the answer is withheld.

## Security Rule

Document content is treated as evidence data, not authority. Prompt-like text inside retrieved chunks is flagged during ingestion and the answer prompt explicitly instructs the model to ignore instructions inside evidence.

## Trade-offs

The service validates citations against its local metadata store before returning the answer. PostgreSQL receives the governance schema for enterprise persistence, while the service keeps an operational JSON state for local development and fast Docker operation.

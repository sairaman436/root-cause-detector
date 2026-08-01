/*
 * Purpose: Stores extracted document metadata.
 * Why it exists: Search, RAG citation, classification, and governance require structured metadata independent of raw documents.
 * Architecture fit: Metadata store entity for AI-2 knowledge extraction.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Extracted knowledge document metadata entity. */
@Entity
@Table(name = "knowledge_metadata", schema = "knowledge")
public class KnowledgeMetadataEntity {
    @Id private UUID id;
    private UUID datasetId;
    private String documentTitle;
    private String documentType;
    private String language;
    private String department;
    private String scheme;
    private String administrativeRegion;
    @Column(columnDefinition = "TEXT")
    private String metadataJson;
    private Instant extractedAt;

    protected KnowledgeMetadataEntity() {}

    /** Creates extracted document metadata. */
    public KnowledgeMetadataEntity(UUID id, UUID datasetId, String documentTitle, String documentType, String language, String department, String scheme, String administrativeRegion, String metadataJson, Instant extractedAt) {
        this.id = id; this.datasetId = datasetId; this.documentTitle = documentTitle; this.documentType = documentType; this.language = language; this.department = department; this.scheme = scheme; this.administrativeRegion = administrativeRegion; this.metadataJson = metadataJson; this.extractedAt = extractedAt;
    }

    public UUID getId() { return id; }
}

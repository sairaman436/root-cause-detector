/*
 * Purpose: Stores extracted named entities from knowledge documents.
 * Why it exists: Knowledge enrichment needs departments, schemes, locations, people, organizations, dates, and topics with confidence.
 * Architecture fit: Enrichment entity for AI-2 knowledge graph preparation.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Extracted knowledge entity record. */
@Entity
@Table(name = "knowledge_entities", schema = "knowledge")
public class KnowledgeEntityRecordEntity {
    @Id private UUID id;
    private UUID metadataId;
    private String entityType;
    private String entityValue;
    private BigDecimal confidence;

    protected KnowledgeEntityRecordEntity() {}

    /** Creates an extracted entity record. */
    public KnowledgeEntityRecordEntity(UUID id, UUID metadataId, String entityType, String entityValue, BigDecimal confidence) {
        this.id = id; this.metadataId = metadataId; this.entityType = entityType; this.entityValue = entityValue; this.confidence = confidence;
    }
}

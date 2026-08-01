/*
 * Purpose: Stores multi-label topic classifications for knowledge documents.
 * Why it exists: Retrieval, coverage analysis, and dataset slicing require governed classification labels.
 * Architecture fit: Classification entity for the AI-2 knowledge enrichment pipeline.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Knowledge classification label entity. */
@Entity
@Table(name = "knowledge_classifications", schema = "knowledge")
public class KnowledgeClassificationEntity {
    @Id private UUID id;
    private UUID metadataId;
    private String label;
    private BigDecimal confidence;
    private String classifierVersion;

    protected KnowledgeClassificationEntity() {}

    /** Creates a classification label. */
    public KnowledgeClassificationEntity(UUID id, UUID metadataId, String label, BigDecimal confidence, String classifierVersion) {
        this.id = id; this.metadataId = metadataId; this.label = label; this.confidence = confidence; this.classifierVersion = classifierVersion;
    }
}

/*
 * Purpose: Stores explicit research hypotheses.
 * Why it exists: Scientific discovery requires hypotheses to be traceable from proposal through experiment and finding.
 * Architecture fit: Research-1 hypothesis tracker entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research hypothesis entity. */
@Entity
@Table(name = "research_hypotheses", schema = "research_lab")
public class ResearchHypothesisEntity {
    @Id private UUID id;
    private UUID projectId;
    private String statement;
    private String rationale;
    private String status;
    private Double confidence;
    private Instant createdAt;

    protected ResearchHypothesisEntity() {}

    /** Creates a research hypothesis. */
    public ResearchHypothesisEntity(UUID id, UUID projectId, String statement, String rationale, String status, Double confidence, Instant createdAt) {
        this.id = id; this.projectId = projectId; this.statement = statement; this.rationale = rationale; this.status = status; this.confidence = confidence; this.createdAt = createdAt;
    }
}

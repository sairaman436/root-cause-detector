/*
 * Purpose: Stores crawler connector configuration and incremental cursor state.
 * Why it exists: Continuous acquisition requires scheduled source-specific crawlers and resumable incremental updates.
 * Architecture fit: Crawler registry entity for the AI-2 connector framework.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Knowledge crawler registry entity. */
@Entity
@Table(name = "knowledge_crawlers", schema = "knowledge")
public class KnowledgeCrawlerEntity {
    @Id private UUID id;
    private UUID sourceId;
    private String connectorType;
    private String scheduleCron;
    private String incrementalCursor;
    private String status;
    private Instant lastRunAt;

    protected KnowledgeCrawlerEntity() {}

    /** Creates a crawler configuration. */
    public KnowledgeCrawlerEntity(UUID id, UUID sourceId, String connectorType, String scheduleCron, String incrementalCursor, String status, Instant lastRunAt) {
        this.id = id; this.sourceId = sourceId; this.connectorType = connectorType; this.scheduleCron = scheduleCron; this.incrementalCursor = incrementalCursor; this.status = status; this.lastRunAt = lastRunAt;
    }
}

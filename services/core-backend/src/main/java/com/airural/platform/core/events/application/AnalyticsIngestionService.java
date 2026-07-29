/*
 * Purpose: Transforms platform events into analytical records.
 * Why it exists: Future feature store, warehouse, AI training, and reporting pipelines need normalized event-derived records.
 * Architecture fit: Data pipeline application service for Milestone 5 without implementing analytics products.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.AnalyticsEventRecordEntity;
import com.airural.platform.core.events.infrastructure.AnalyticsEventRecordRepository;
import com.airural.platform.shared.events.PlatformEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ingests platform events into analytical records. */
@Service
public class AnalyticsIngestionService {
    private final AnalyticsEventRecordRepository repository;
    private final ObjectMapper objectMapper;

    public AnalyticsIngestionService(AnalyticsEventRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /** Creates an idempotent analytical record for a platform event. */
    @Transactional
    public void ingest(PlatformEvent event) {
        if (repository.existsByEventId(event.eventId())) {
            return;
        }
        try {
            repository.save(new AnalyticsEventRecordEntity(
                    event.eventId(),
                    event.eventType(),
                    event.topic(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.organizationId(),
                    event.actorUserId(),
                    event.schemaVersion(),
                    event.occurredAt(),
                    objectMapper.writeValueAsString(event.payload())));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize analytical event payload", ex);
        }
    }
}

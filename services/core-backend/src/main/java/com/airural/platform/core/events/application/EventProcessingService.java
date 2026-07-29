/*
 * Purpose: Records consumer processing and placeholder data-pipeline stages.
 * Why it exists: Milestone 5 needs consumers for audit, analytics, notification, AI pipeline, and search index without implementing future domains.
 * Architecture fit: Consumer application service for event-driven ingestion and observability.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.*;
import com.airural.platform.core.events.infrastructure.EventProcessingLogRepository;
import com.airural.platform.shared.events.PlatformEvent;
import io.micrometer.core.instrument.*;
import java.time.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Processes platform events for current and future consumers. */
@Service
public class EventProcessingService {
    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);
    private final EventProcessingLogRepository processingLogRepository;
    private final AnalyticsIngestionService analyticsIngestionService;
    private final Counter processedCounter;
    private final Counter skippedCounter;
    private final Timer processingTimer;

    public EventProcessingService(EventProcessingLogRepository processingLogRepository, AnalyticsIngestionService analyticsIngestionService, MeterRegistry meterRegistry) {
        this.processingLogRepository = processingLogRepository;
        this.analyticsIngestionService = analyticsIngestionService;
        this.processedCounter = Counter.builder("platform.events.consumer.processed").register(meterRegistry);
        this.skippedCounter = Counter.builder("platform.events.consumer.skipped").register(meterRegistry);
        this.processingTimer = Timer.builder("platform.events.consumer.latency").register(meterRegistry);
    }

    /** Records idempotent processing for a consumer. */
    @Transactional
    public void process(String consumerName, PlatformEvent event, String topic, Integer partition, Long offset) {
        if (processingLogRepository.existsByEventIdAndConsumerName(event.eventId(), consumerName)) {
            skippedCounter.increment();
            return;
        }
        long latencyMs = Math.max(0, Duration.between(event.occurredAt(), Instant.now()).toMillis());
        processingLogRepository.save(new EventProcessingLogEntity(
                event.eventId(), consumerName, topic, partition, offset, EventProcessingStatus.PROCESSED, 1, latencyMs, null));
        if ("analytics-ingestion".equals(consumerName)) {
            analyticsIngestionService.ingest(event);
        }
        processingTimer.record(Duration.ofMillis(latencyMs));
        processedCounter.increment();
        log.info("Consumer {} processed event {} type {} from topic {}", consumerName, event.eventId(), event.eventType(), topic);
    }
}

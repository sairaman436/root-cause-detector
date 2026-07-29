/*
 * Purpose: Publishes due outbox records to Kafka and records retry state.
 * Why it exists: Kafka delivery must be decoupled from domain transactions and resilient to broker outages.
 * Architecture fit: Event publisher worker for the transactional outbox pattern.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.*;
import com.airural.platform.core.events.infrastructure.*;
import com.airural.platform.shared.events.PlatformEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Scheduled publisher that drains the transactional outbox. */
@Service
@ConditionalOnProperty(prefix = "airural.events.kafka", name = "enabled", havingValue = "true")
public class KafkaOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaOutboxPublisher.class);
    private final OutboxEventRepository outboxRepository;
    private final DeadLetterEventRepository deadLetterRepository;
    private final EventRetryRepository retryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final int maxAttempts;
    private final Counter publishedCounter;
    private final Counter failedCounter;
    private final Timer latencyTimer;

    public KafkaOutboxPublisher(
            OutboxEventRepository outboxRepository,
            DeadLetterEventRepository deadLetterRepository,
            EventRetryRepository retryRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            @Value("${airural.events.outbox.batch-size}") int batchSize,
            @Value("${airural.events.outbox.max-attempts}") int maxAttempts) {
        this.outboxRepository = outboxRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.retryRepository = retryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.publishedCounter = Counter.builder("platform.events.outbox.published").register(meterRegistry);
        this.failedCounter = Counter.builder("platform.events.outbox.failed").register(meterRegistry);
        this.latencyTimer = Timer.builder("platform.events.outbox.latency").register(meterRegistry);
    }

    /** Publishes due outbox records on a fixed schedule. */
    @Scheduled(fixedDelayString = "${airural.events.outbox.poll-ms}")
    @Transactional
    public void publishDueEvents() {
        List<OutboxEventEntity> dueEvents = outboxRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), Instant.now(), PageRequest.of(0, batchSize));
        dueEvents.forEach(this::publishOne);
    }

    private void publishOne(OutboxEventEntity outbox) {
        try {
            Map<String, Object> payload = objectMapper.readValue(outbox.payloadJson(), new TypeReference<>() {
            });
            PlatformEvent event = new PlatformEvent(
                    outbox.id(),
                    outbox.eventType(),
                    outbox.schemaVersion(),
                    outbox.topic(),
                    outbox.aggregateType(),
                    outbox.aggregateId(),
                    outbox.organizationId(),
                    outbox.actorUserId(),
                    outbox.correlationId(),
                    outbox.occurredAt(),
                    payload);
            kafkaTemplate.send(outbox.topic(), outbox.aggregateId().toString(), event).get();
            outbox.markPublished();
            retryRepository.save(new EventRetryEntity(outbox.id(), outbox.attempts() + 1, "PUBLISHED", null, outbox.nextAttemptAt(), Instant.now()));
            publishedCounter.increment();
            latencyTimer.record(java.time.Duration.between(outbox.occurredAt(), Instant.now()));
            log.info("Published outbox event {} to topic {}", outbox.id(), outbox.topic());
        } catch (Exception ex) {
            outbox.markFailure(ex.getMessage(), maxAttempts);
            retryRepository.save(new EventRetryEntity(outbox.id(), outbox.attempts(), outbox.status().name(), ex.getMessage(), outbox.nextAttemptAt(), Instant.now()));
            if (outbox.status() == OutboxStatus.DEAD_LETTERED) {
                deadLetterRepository.save(new DeadLetterEventEntity(outbox, ex.getMessage()));
            }
            failedCounter.increment();
            log.warn("Failed to publish outbox event {} to topic {}", outbox.id(), outbox.topic(), ex);
        }
    }
}

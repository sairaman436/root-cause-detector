/*
 * Purpose: Declares Kafka consumers for audit, notification, analytics, AI, search index, and workflow paths.
 * Why it exists: Milestone 7 establishes the event-driven backbone consumers that future services will extend.
 * Architecture fit: Consumer adapter layer with current processing logs and future-domain placeholders.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.shared.events.PlatformEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka listener collection for platform event consumers. */
@Component
@ConditionalOnProperty(prefix = "airural.events.kafka", name = "consumers-enabled", havingValue = "true")
public class PlatformEventConsumers {
    private final EventProcessingService processingService;

    public PlatformEventConsumers(EventProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(id = "audit-consumer", topics = {"survey.created", "survey.updated", "survey.completed", "evidence.uploaded", "evidence.validated", "knowledge.document.uploaded", "knowledge.document.updated", "geo.household.created", "geo.infrastructure.updated", "notification.created", "audit.created", "user.created", "user.updated", "user.logged-in"}, containerFactory = "kafkaListenerContainerFactory")
    public void audit(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("audit", record.value(), record.topic(), record.partition(), record.offset());
    }

    @KafkaListener(id = "analytics-consumer", topics = {"survey.created", "survey.updated", "survey.completed", "evidence.uploaded", "evidence.validated", "geo.household.created", "geo.infrastructure.updated", "user.created", "user.updated", "user.logged-in"}, containerFactory = "kafkaListenerContainerFactory")
    public void analytics(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("analytics-ingestion", record.value(), record.topic(), record.partition(), record.offset());
    }

    @KafkaListener(id = "notification-consumer", topics = {"notification.created", "survey.completed", "evidence.validated", "user.created", "user.logged-in"}, containerFactory = "kafkaListenerContainerFactory")
    public void notification(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("notification", record.value(), record.topic(), record.partition(), record.offset());
    }

    @KafkaListener(id = "ai-pipeline-placeholder-consumer", topics = {"survey.completed", "evidence.validated", "knowledge.document.uploaded", "geo.household.created"}, containerFactory = "kafkaListenerContainerFactory")
    public void aiPipeline(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("ai-pipeline-placeholder", record.value(), record.topic(), record.partition(), record.offset());
    }

    @KafkaListener(id = "search-index-placeholder-consumer", topics = {"knowledge.document.uploaded", "knowledge.document.updated", "survey.updated", "evidence.uploaded", "geo.infrastructure.updated"}, containerFactory = "kafkaListenerContainerFactory")
    public void searchIndex(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("search-index-placeholder", record.value(), record.topic(), record.partition(), record.offset());
    }

    @KafkaListener(id = "workflow-placeholder-consumer", topics = {"survey.completed", "notification.created", "audit.created"}, containerFactory = "kafkaListenerContainerFactory")
    public void workflow(ConsumerRecord<String, PlatformEvent> record) {
        processingService.process("workflow-placeholder", record.value(), record.topic(), record.partition(), record.offset());
    }
}

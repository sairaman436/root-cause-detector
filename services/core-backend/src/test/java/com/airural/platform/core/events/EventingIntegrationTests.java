/*
 * Purpose: Verifies outbox persistence and consumer processing logs.
 * Why it exists: Milestone 5 requires integration coverage for outbox and consumer idempotency.
 * Architecture fit: Persistence-backed eventing tests without requiring a live Kafka broker.
 */
package com.airural.platform.core.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airural.platform.core.events.application.*;
import com.airural.platform.core.events.domain.OutboxStatus;
import com.airural.platform.core.events.infrastructure.*;
import com.airural.platform.shared.events.*;
import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for eventing persistence. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_eventing_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class EventingIntegrationTests {
    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private EventProcessingService processingService;

    @Autowired
    private EventProcessingLogRepository processingLogRepository;

    @Autowired
    private AnalyticsEventRecordRepository analyticsEventRecordRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private EventSubscriptionRepository subscriptionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void outboxServicePersistsVersionedEvent() {
        UUID surveyId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        UUID eventId = outboxService.enqueue(
                EventTopic.SURVEY_CREATED,
                "SURVEY",
                surveyId,
                organizationId,
                actorId,
                new EventPayloads.SurveyPayload(surveyId, organizationId, "Water Survey", "DRAFT", 1, Instant.now()));

        var outbox = outboxRepository.findById(eventId).orElseThrow();
        assertThat(outbox.status()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outbox.topic()).isEqualTo(EventTopic.SURVEY_CREATED.topicName());
        assertThat(outbox.schemaVersion()).isEqualTo(PlatformEvent.CURRENT_SCHEMA_VERSION);
        assertThat(outbox.payloadJson()).contains("Water Survey");
        assertThat(eventLogRepository.existsByEventId(eventId)).isTrue();
    }

    @Test
    void processingServiceIsIdempotentPerConsumerAndEvent() {
        UUID eventId = UUID.randomUUID();
        PlatformEvent event = new PlatformEvent(
                eventId,
                EventTopic.EVIDENCE_UPLOADED.name(),
                PlatformEvent.CURRENT_SCHEMA_VERSION,
                EventTopic.EVIDENCE_UPLOADED.topicName(),
                "EVIDENCE",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId.toString(),
                Instant.now(),
                Map.of("evidenceType", "IMAGE"));

        processingService.process("analytics-ingestion", event, event.topic(), 0, 10L);
        processingService.process("analytics-ingestion", event, event.topic(), 0, 10L);

        assertThat(processingLogRepository.findAll()).hasSize(1);
        assertThat(processingLogRepository.existsByEventIdAndConsumerName(eventId, "analytics-ingestion")).isTrue();
        assertThat(analyticsEventRecordRepository.findAll()).hasSize(1);
    }

    @Test
    void eventOperationsApisExposeLogsOutboxSubscriptionsAndReplay() throws Exception {
        String token = registerAdmin();
        UUID aggregateId = UUID.randomUUID();
        UUID eventId = outboxService.enqueue(
                EventTopic.NOTIFICATION_CREATED,
                "NOTIFICATION",
                aggregateId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new EventPayloads.NotificationPayload(aggregateId, UUID.randomUUID(), "IN_APP", "PENDING", Instant.now()));

        JsonNode events = json(getJson("/api/v1/events", token));
        assertThat(events.at("/data/content").size()).isGreaterThanOrEqualTo(1);

        JsonNode event = json(getJson("/api/v1/events/" + eventId, token));
        assertThat(event.at("/data/eventId").asText()).isEqualTo(eventId.toString());

        JsonNode outbox = json(getJson("/api/v1/events/outbox", token));
        assertThat(outbox.at("/data/content").size()).isGreaterThanOrEqualTo(1);

        JsonNode subscriptions = json(getJson("/api/v1/events/subscriptions", token));
        assertThat(subscriptions.at("/data/content").size()).isEqualTo((int) subscriptionRepository.count());

        JsonNode replay = json(postJson("/api/v1/events/replay", token, "{\"eventId\":\"" + eventId + "\"}"));
        assertThat(replay.at("/data/status").asText()).isEqualTo("PENDING");

        mockMvc.perform(get("/api/v1/events/dead-letter").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/events/audit").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String registerAdmin() throws Exception {
        return json(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"event.admin",
                                  "email":"event.admin@example.gov",
                                  "fullName":"Event Admin",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).at("/data/accessToken").asText();
    }

    private String getJson(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String postJson(String path, String token, String payload) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}

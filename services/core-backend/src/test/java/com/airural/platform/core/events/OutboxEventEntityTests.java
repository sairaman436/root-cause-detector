/*
 * Purpose: Verifies outbox retry and dead-letter state transitions.
 * Why it exists: Milestone 5 requires failure recovery behavior independent of Kafka availability.
 * Architecture fit: Unit coverage for the eventing domain model.
 */
package com.airural.platform.core.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.events.domain.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for outbox event retry state. */
class OutboxEventEntityTests {
    @Test
    void failuresRetryThenDeadLetterAtMaxAttempts() {
        OutboxEventEntity event = new OutboxEventEntity(
                UUID.randomUUID(),
                "SURVEY_CREATED",
                1,
                "survey.created",
                "SURVEY",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "correlation",
                "core-backend",
                "trace",
                "{}",
                "{}",
                Instant.now());

        event.markFailure("broker unavailable", 2);
        assertThat(event.status()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.attempts()).isEqualTo(1);

        event.markRetryPending();
        assertThat(event.status()).isEqualTo(OutboxStatus.PENDING);

        event.markFailure("broker unavailable", 2);
        assertThat(event.status()).isEqualTo(OutboxStatus.DEAD_LETTERED);
        assertThat(event.attempts()).isEqualTo(2);
    }
}

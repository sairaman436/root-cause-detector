/*
 * Purpose: Provides persistence access to event retry attempts.
 * Why it exists: Retry history must be observable for production event operations.
 * Architecture fit: Infrastructure repository for retry tracking.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.EventRetryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for event retries. */
public interface EventRetryRepository extends JpaRepository<EventRetryEntity, UUID> {
}

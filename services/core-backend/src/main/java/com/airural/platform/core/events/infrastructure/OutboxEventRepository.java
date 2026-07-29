/*
 * Purpose: Provides persistence access to transactional outbox events.
 * Why it exists: Publisher workers need ordered batches of due events and retry state.
 * Architecture fit: Infrastructure repository for the eventing module.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for outbox events. */
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(Collection<OutboxStatus> statuses, Instant now, Pageable pageable);

    long countByStatus(OutboxStatus status);
}

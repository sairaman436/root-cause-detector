/*
 * Purpose: Provides persistence access to dead-letter events.
 * Why it exists: Operators need dead-letter search and replay workflows.
 * Architecture fit: Infrastructure repository for event recovery.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.DeadLetterEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for dead-letter events. */
public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, UUID> {
}

/*
 * Purpose: Provides persistence access to event subscription records.
 * Why it exists: Event APIs need visibility into current and future consumer subscriptions.
 * Architecture fit: Infrastructure repository for event consumer architecture.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.EventSubscriptionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for event subscriptions. */
public interface EventSubscriptionRepository extends JpaRepository<EventSubscriptionEntity, UUID> {
}

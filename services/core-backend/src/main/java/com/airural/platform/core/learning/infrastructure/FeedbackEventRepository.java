/* Purpose: Persists feedback events. Why it exists: Human and system feedback must remain auditable. Architecture fit: JPA evidence adapter for learning records. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.FeedbackEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for feedback events. */
public interface FeedbackEventRepository extends JpaRepository<FeedbackEventEntity, UUID> {}

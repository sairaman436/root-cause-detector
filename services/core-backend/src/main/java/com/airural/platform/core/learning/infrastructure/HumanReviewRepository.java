/* Purpose: Persists human reviews. Why it exists: Learning records need reviewer decisions and escalation evidence. Architecture fit: JPA adapter for governance workflow. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.HumanReviewEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for human reviews. */
public interface HumanReviewRepository extends JpaRepository<HumanReviewEntity, UUID> {}

/* Purpose: Persists routing decisions. Why it exists: Model selection must be explainable and auditable. Architecture fit: JPA adapter for model router decisions. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.RoutingDecisionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for routing decisions. */
public interface RoutingDecisionRepository extends JpaRepository<RoutingDecisionEntity, UUID> {}

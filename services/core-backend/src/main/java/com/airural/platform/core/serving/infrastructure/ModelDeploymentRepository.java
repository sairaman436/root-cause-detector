/* Purpose: Persists model deployments. Why it exists: Routing requires quality-gated deployment registry records. Architecture fit: JPA adapter for serving model deployments. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.ModelDeploymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for model deployments. */
public interface ModelDeploymentRepository extends JpaRepository<ModelDeploymentEntity, UUID> {
    Optional<ModelDeploymentEntity> findFirstByAssistantTypeAndTrafficStatusOrderByCreatedAtDesc(String assistantType, String trafficStatus);
}

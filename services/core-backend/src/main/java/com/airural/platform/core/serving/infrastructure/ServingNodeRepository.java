/* Purpose: Persists serving nodes. Why it exists: Router and health APIs need runtime node inventory. Architecture fit: JPA adapter for serving infrastructure. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.ServingNodeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for serving nodes. */
public interface ServingNodeRepository extends JpaRepository<ServingNodeEntity, UUID> {}

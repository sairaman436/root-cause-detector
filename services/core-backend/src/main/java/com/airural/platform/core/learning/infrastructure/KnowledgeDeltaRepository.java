/* Purpose: Persists knowledge deltas. Why it exists: Policy, scheme, guideline, and research updates must trigger future refresh work. Architecture fit: JPA adapter for knowledge evolution. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.KnowledgeDeltaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge deltas. */
public interface KnowledgeDeltaRepository extends JpaRepository<KnowledgeDeltaEntity, UUID> {}

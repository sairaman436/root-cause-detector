/* Purpose: Persists corrections. Why it exists: Expert edits and better explanations are training-candidate evidence. Architecture fit: JPA adapter for correction records. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.CorrectionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for corrections. */
public interface CorrectionRepository extends JpaRepository<CorrectionEntity, UUID> {}

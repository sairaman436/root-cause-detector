/*
 * Purpose: Persists AI safety test results.
 * Why it exists: Safety gates and audit exports require durable safety findings.
 * Architecture fit: Infrastructure adapter for AI-5 safety framework.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.SafetyTestEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for safety tests. */
public interface SafetyTestRepository extends JpaRepository<SafetyTestEntity, UUID> {
}

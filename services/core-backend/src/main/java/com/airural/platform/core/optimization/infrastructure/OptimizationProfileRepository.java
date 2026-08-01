/*
 * Purpose: Persists optimization profile registry records.
 * Why it exists: Profiles version export, quantization, runtime, and precision policy.
 * Architecture fit: JPA repository for AI-6 reference data.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.OptimizationProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for optimization profiles. */
public interface OptimizationProfileRepository extends JpaRepository<OptimizationProfileEntity, UUID> {
    Optional<OptimizationProfileEntity> findByProfileKey(String profileKey);
}

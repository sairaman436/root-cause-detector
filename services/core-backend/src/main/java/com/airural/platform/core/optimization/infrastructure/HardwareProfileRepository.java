/*
 * Purpose: Persists hardware profile records.
 * Why it exists: Compatibility checks need stable hardware requirement metadata.
 * Architecture fit: JPA reference repository for optimization compatibility.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.HardwareProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for hardware profiles. */
public interface HardwareProfileRepository extends JpaRepository<HardwareProfileEntity, UUID> {
    Optional<HardwareProfileEntity> findByHardwareKey(String hardwareKey);
}

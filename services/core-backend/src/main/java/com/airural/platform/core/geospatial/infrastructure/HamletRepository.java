/*
 * Purpose: Provides persistence access for hamlet administrative units.
 * Why it exists: Household mapping requires stable locality parents.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.HamletEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for hamlets. */
public interface HamletRepository extends JpaRepository<HamletEntity, UUID> {
    boolean existsByWardIdAndCode(UUID wardId, String code);
    Optional<HamletEntity> findByIdAndIsActiveTrue(UUID id);
    List<HamletEntity> findByWardIdAndIsActiveTrueOrderByNameAsc(UUID wardId);
}

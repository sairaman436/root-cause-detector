/*
 * Purpose: Provides persistence access for ward administrative units.
 * Why it exists: Household hierarchy resolution needs ward lookups under villages.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.WardEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for wards. */
public interface WardRepository extends JpaRepository<WardEntity, UUID> {
    boolean existsByVillageIdAndCode(UUID villageId, String code);
    Optional<WardEntity> findByIdAndIsActiveTrue(UUID id);
    List<WardEntity> findByVillageIdAndIsActiveTrueOrderByNameAsc(UUID villageId);
}

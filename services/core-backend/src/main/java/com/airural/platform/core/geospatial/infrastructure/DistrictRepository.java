/*
 * Purpose: Provides persistence access for district administrative units.
 * Why it exists: Geospatial services need governed child lookup below states.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.DistrictEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for districts. */
public interface DistrictRepository extends JpaRepository<DistrictEntity, UUID> {
    boolean existsByStateIdAndCode(UUID stateId, String code);
    Optional<DistrictEntity> findByIdAndIsActiveTrue(UUID id);
    List<DistrictEntity> findByStateIdAndIsActiveTrueOrderByNameAsc(UUID stateId);
}

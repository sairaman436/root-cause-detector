/*
 * Purpose: Provides persistence access for mandal/sub-district administrative units.
 * Why it exists: Geospatial services need governed child lookup below districts.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.MandalEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for mandals. */
public interface MandalRepository extends JpaRepository<MandalEntity, UUID> {
    boolean existsByDistrictIdAndCode(UUID districtId, String code);
    Optional<MandalEntity> findByIdAndIsActiveTrue(UUID id);
    List<MandalEntity> findByDistrictIdAndIsActiveTrueOrderByNameAsc(UUID districtId);
}

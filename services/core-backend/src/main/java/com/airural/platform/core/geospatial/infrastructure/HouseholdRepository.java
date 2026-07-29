/*
 * Purpose: Provides persistence access for mapped households.
 * Why it exists: Household mapping APIs need idempotent lookup, search, and spatial filtering.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.HouseholdEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Spring Data repository for households. */
public interface HouseholdRepository extends JpaRepository<HouseholdEntity, UUID>, JpaSpecificationExecutor<HouseholdEntity> {
    boolean existsByHamletIdAndHouseholdCode(UUID hamletId, String householdCode);
    Optional<HouseholdEntity> findByIdAndIsActiveTrue(UUID id);
    List<HouseholdEntity> findByHamletIdAndIsActiveTrueOrderByHouseholdCodeAsc(UUID hamletId);
}

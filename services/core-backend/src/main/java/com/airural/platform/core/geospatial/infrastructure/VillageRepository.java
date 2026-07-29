/*
 * Purpose: Provides persistence access for village administrative units.
 * Why it exists: Village lookup, spatial search, and hierarchy operations need specification-backed queries.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.VillageEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Spring Data repository for villages. */
public interface VillageRepository extends JpaRepository<VillageEntity, UUID>, JpaSpecificationExecutor<VillageEntity> {
    boolean existsByGramPanchayatIdAndCode(UUID gramPanchayatId, String code);
    Optional<VillageEntity> findByIdAndIsActiveTrue(UUID id);
    List<VillageEntity> findByGramPanchayatIdAndIsActiveTrueOrderByNameAsc(UUID gramPanchayatId);
    List<VillageEntity> findByIsActiveTrueOrderByNameAsc();
}

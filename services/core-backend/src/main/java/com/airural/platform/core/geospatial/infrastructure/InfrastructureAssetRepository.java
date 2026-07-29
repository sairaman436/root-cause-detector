/*
 * Purpose: Provides persistence access for mapped infrastructure assets.
 * Why it exists: Infrastructure APIs need search, proximity filtering, and uniqueness enforcement.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Spring Data repository for infrastructure assets. */
public interface InfrastructureAssetRepository extends JpaRepository<InfrastructureAssetEntity, UUID>, JpaSpecificationExecutor<InfrastructureAssetEntity> {
    boolean existsByAssetTypeAndCode(InfrastructureAssetType assetType, String code);
    Optional<InfrastructureAssetEntity> findByIdAndIsActiveTrue(UUID id);
    List<InfrastructureAssetEntity> findByVillageIdAndIsActiveTrueOrderByNameAsc(UUID villageId);
}

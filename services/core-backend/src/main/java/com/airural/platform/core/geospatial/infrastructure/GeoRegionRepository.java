/*
 * Purpose: Provides persistence access for geography regions.
 * Why it exists: Regions group spatial areas for future enterprise views without implementing analytics logic.
 * Architecture fit: Infrastructure repository for geography regions.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.GeoRegionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for GeoRegion records. */
public interface GeoRegionRepository extends JpaRepository<GeoRegionEntity, UUID> {
}

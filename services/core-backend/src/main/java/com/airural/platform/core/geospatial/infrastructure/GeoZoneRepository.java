/*
 * Purpose: Provides persistence access for geography zones.
 * Why it exists: Zones are named spatial containers for future heatmaps and field planning.
 * Architecture fit: Infrastructure repository for geography zones.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.GeoZoneEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for GeoZone records. */
public interface GeoZoneRepository extends JpaRepository<GeoZoneEntity, UUID> {
}

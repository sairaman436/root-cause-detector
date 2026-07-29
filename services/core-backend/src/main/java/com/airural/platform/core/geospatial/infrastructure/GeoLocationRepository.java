/*
 * Purpose: Provides persistence access for normalized geography location records.
 * Why it exists: GeoLocation records store common GPS, GeoJSON, WKT, and bounding-box metadata.
 * Architecture fit: Infrastructure repository for geography primitives.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.GeoLocationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for GeoLocation records. */
public interface GeoLocationRepository extends JpaRepository<GeoLocationEntity, UUID> {
}

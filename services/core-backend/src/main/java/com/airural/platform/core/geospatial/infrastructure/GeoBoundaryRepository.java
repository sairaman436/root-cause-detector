/*
 * Purpose: Provides persistence access for GeoJSON boundaries.
 * Why it exists: Boundary APIs need uniqueness by administrative entity and bounding-box search support.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Spring Data repository for boundaries. */
public interface GeoBoundaryRepository extends JpaRepository<GeoBoundaryEntity, UUID>, JpaSpecificationExecutor<GeoBoundaryEntity> {
    boolean existsByEntityTypeAndEntityIdAndIsActiveTrue(AdministrativeLevel entityType, UUID entityId);
    Optional<GeoBoundaryEntity> findByEntityTypeAndEntityIdAndIsActiveTrue(AdministrativeLevel entityType, UUID entityId);
}

/*
 * Purpose: Provides persistence access for geospatial audit events.
 * Why it exists: Geography actions must be queryable for compliance and operational review.
 * Architecture fit: Infrastructure repository for Milestone 6 geography audit records.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.GeoAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for geospatial audit events. */
public interface GeoAuditRepository extends JpaRepository<GeoAuditEntity, UUID> {
}

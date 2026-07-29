/*
 * Purpose: Defines auditable geospatial actions.
 * Why it exists: Geography changes need module-local compliance records in addition to the shared platform audit log.
 * Architecture fit: Domain vocabulary for GeoAudit persistence.
 */
package com.airural.platform.core.geospatial.domain;

/** Auditable actions in the Geography module. */
public enum GeoAuditAction {
    CREATED,
    UPDATED,
    SOFT_DELETED,
    RESTORED,
    SEARCHED,
    BOUNDARY_CREATED,
    INFRASTRUCTURE_CREATED
}

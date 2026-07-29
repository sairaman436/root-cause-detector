/*
 * Purpose: Persists module-local geospatial audit records.
 * Why it exists: Geography changes require searchable audit history scoped to entity type and entity ID.
 * Architecture fit: Compliance aggregate for the Geospatial module, complementing the shared platform audit log.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for geospatial audit events. */
@Entity
@Table(name = "geo_audit", schema = "geospatial")
public class GeoAuditEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private GeoAuditAction action;

    private UUID actorUserId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    protected GeoAuditEntity() {
    }

    public GeoAuditEntity(String entityType, UUID entityId, GeoAuditAction action, UUID actorUserId, String details) {
        this.id = UUID.randomUUID();
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.actorUserId = actorUserId;
        this.details = details;
        this.createdAt = Instant.now();
    }
}

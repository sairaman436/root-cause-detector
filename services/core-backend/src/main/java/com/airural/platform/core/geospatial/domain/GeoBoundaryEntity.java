/*
 * Purpose: Persists GeoJSON boundaries and bounding boxes for administrative units.
 * Why it exists: Boundary storage enables spatial filtering today and prepares the platform for advanced spatial services later.
 * Architecture fit: Boundary aggregate owned by the Geospatial module.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for GeoJSON boundaries. */
@Entity
@Table(name = "geo_boundaries", schema = "geospatial")
public class GeoBoundaryEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdministrativeLevel entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String geojson;

    @Column(nullable = false)
    private BigDecimal minLatitude;

    @Column(nullable = false)
    private BigDecimal minLongitude;

    @Column(nullable = false)
    private BigDecimal maxLatitude;

    @Column(nullable = false)
    private BigDecimal maxLongitude;

    private BigDecimal areaSqKm;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected GeoBoundaryEntity() {
    }

    public GeoBoundaryEntity(
            AdministrativeLevel entityType,
            UUID entityId,
            String geojson,
            BigDecimal minLatitude,
            BigDecimal minLongitude,
            BigDecimal maxLatitude,
            BigDecimal maxLongitude,
            BigDecimal areaSqKm) {
        this.id = UUID.randomUUID();
        this.entityType = entityType;
        this.entityId = entityId;
        this.geojson = geojson;
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
        this.areaSqKm = areaSqKm;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public AdministrativeLevel entityType() { return entityType; }
    public UUID entityId() { return entityId; }
    public String geojson() { return geojson; }
    public BigDecimal minLatitude() { return minLatitude; }
    public BigDecimal minLongitude() { return minLongitude; }
    public BigDecimal maxLatitude() { return maxLatitude; }
    public BigDecimal maxLongitude() { return maxLongitude; }
    public BigDecimal areaSqKm() { return areaSqKm; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }
}

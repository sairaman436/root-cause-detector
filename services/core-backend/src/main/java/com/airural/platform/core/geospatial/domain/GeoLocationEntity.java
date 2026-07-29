/*
 * Purpose: Persists normalized geospatial point or geometry metadata for any geography-owned entity.
 * Why it exists: Administrative units, households, infrastructure, zones, and regions need a common location record that supports GeoJSON and WKT.
 * Architecture fit: Cross-cutting geography aggregate that prepares the platform for PostGIS-backed spatial services.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for generic geography locations. */
@Entity
@Table(name = "geo_locations", schema = "geospatial")
public class GeoLocationEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdministrativeLevel entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CoordinateSystem coordinateSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private GeoShapeType shapeType;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal elevationMeters;

    @Column(columnDefinition = "TEXT")
    private String geojson;

    @Column(columnDefinition = "TEXT")
    private String wkt;

    private BigDecimal minLatitude;
    private BigDecimal minLongitude;
    private BigDecimal maxLatitude;
    private BigDecimal maxLongitude;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected GeoLocationEntity() {
    }
}

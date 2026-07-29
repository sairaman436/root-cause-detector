/*
 * Purpose: Persists operational geography zones.
 * Why it exists: Field planning and future heatmaps need governed named zones independent of administrative borders.
 * Architecture fit: Geography aggregate for zone-level spatial metadata.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for geography zones. */
@Entity
@Table(name = "geo_zones", schema = "geospatial")
public class GeoZoneEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CoordinateSystem coordinateSystem;

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

    protected GeoZoneEntity() {
    }
}

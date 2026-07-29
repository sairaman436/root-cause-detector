/*
 * Purpose: Persists named geography regions that can group multiple zones or administrative units.
 * Why it exists: Enterprise deployments need region-level spatial containers for future analytics and reporting.
 * Architecture fit: Geography aggregate for region metadata without implementing analytics features.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for geography regions. */
@Entity
@Table(name = "geo_regions", schema = "geospatial")
public class GeoRegionEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected GeoRegionEntity() {
    }
}

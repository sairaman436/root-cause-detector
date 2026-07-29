/*
 * Purpose: Persists village-level administrative records with centroid and boundary search metadata.
 * Why it exists: Villages are the primary rural spatial unit for survey, evidence, analytics, and future AI workflows.
 * Architecture fit: Geospatial aggregate for village lookup, radius search, bounding-box filtering, and hierarchy traversal.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for villages. */
@Entity
@Table(name = "villages", schema = "geospatial")
public class VillageEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID gramPanchayatId;

    private BigDecimal elevationMeters;
    private BigDecimal areaSqKm;
    private Long population;
    private Long householdCount;

    @Column(columnDefinition = "TEXT")
    private String geojson;

    private BigDecimal minLatitude;
    private BigDecimal minLongitude;
    private BigDecimal maxLatitude;
    private BigDecimal maxLongitude;

    protected VillageEntity() {
    }

    public VillageEntity(
            UUID gramPanchayatId,
            String code,
            String name,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal elevationMeters,
            BigDecimal areaSqKm,
            Long population,
            Long householdCount,
            String geojson,
            BigDecimal minLatitude,
            BigDecimal minLongitude,
            BigDecimal maxLatitude,
            BigDecimal maxLongitude) {
        super(code, name, latitude, longitude);
        this.gramPanchayatId = gramPanchayatId;
        this.elevationMeters = elevationMeters;
        this.areaSqKm = areaSqKm;
        this.population = population;
        this.householdCount = householdCount;
        this.geojson = geojson;
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
    }

    public UUID gramPanchayatId() { return gramPanchayatId; }
    public BigDecimal elevationMeters() { return elevationMeters; }
    public BigDecimal areaSqKm() { return areaSqKm; }
    public Long population() { return population; }
    public Long householdCount() { return householdCount; }
    public String geojson() { return geojson; }
    public BigDecimal minLatitude() { return minLatitude; }
    public BigDecimal minLongitude() { return minLongitude; }
    public BigDecimal maxLatitude() { return maxLatitude; }
    public BigDecimal maxLongitude() { return maxLongitude; }
}

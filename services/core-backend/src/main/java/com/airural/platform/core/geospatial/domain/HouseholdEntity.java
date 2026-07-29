/*
 * Purpose: Persists mapped household locations and future IoT-ready metadata.
 * Why it exists: Household geography connects surveys and evidence to the smallest operational field unit.
 * Architecture fit: Geospatial aggregate used by household mapping, survey linkage, and evidence linkage.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for households. */
@Entity
@Table(name = "households", schema = "geospatial")
public class HouseholdEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID hamletId;

    @Column(nullable = false, length = 64)
    private String householdCode;

    @Column(length = 180)
    private String headOfHousehold;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    private UUID surveyId;
    private UUID evidenceId;

    @Column(columnDefinition = "TEXT")
    private String iotMetadataJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected HouseholdEntity() {
    }

    public HouseholdEntity(
            UUID hamletId,
            String householdCode,
            String headOfHousehold,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            UUID surveyId,
            UUID evidenceId,
            String iotMetadataJson) {
        this.id = UUID.randomUUID();
        this.hamletId = hamletId;
        this.householdCode = householdCode;
        this.headOfHousehold = headOfHousehold;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.surveyId = surveyId;
        this.evidenceId = evidenceId;
        this.iotMetadataJson = iotMetadataJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID hamletId() { return hamletId; }
    public String householdCode() { return householdCode; }
    public String headOfHousehold() { return headOfHousehold; }
    public String address() { return address; }
    public BigDecimal latitude() { return latitude; }
    public BigDecimal longitude() { return longitude; }
    public UUID surveyId() { return surveyId; }
    public UUID evidenceId() { return evidenceId; }
    public String iotMetadataJson() { return iotMetadataJson; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }
}

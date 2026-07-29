/*
 * Purpose: Provides shared persistence fields for administrative units.
 * Why it exists: Country, state, district, mandal, block, gram panchayat, village, ward, and hamlet share identity, naming, location, and lifecycle behavior.
 * Architecture fit: Mapped superclass for the Geospatial administrative hierarchy.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Shared fields for administrative-unit entities. */
@MappedSuperclass
public abstract class AdministrativeUnitEntity {
    @Id
    protected UUID id;

    @Column(nullable = false, length = 32)
    protected String code;

    @Column(nullable = false, length = 180)
    protected String name;

    protected BigDecimal latitude;
    protected BigDecimal longitude;

    @Column(nullable = false)
    protected Instant createdAt;

    @Column(nullable = false)
    protected Instant updatedAt;

    @Version
    protected Integer version;

    @Column(nullable = false)
    protected boolean isActive;

    protected AdministrativeUnitEntity() {
    }

    protected AdministrativeUnitEntity(String code, String name, BigDecimal latitude, BigDecimal longitude) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public String code() { return code; }
    public String name() { return name; }
    public BigDecimal latitude() { return latitude; }
    public BigDecimal longitude() { return longitude; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }

    /** Updates common administrative-unit fields. */
    public void update(String code, String name, BigDecimal latitude, BigDecimal longitude) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = Instant.now();
    }
}

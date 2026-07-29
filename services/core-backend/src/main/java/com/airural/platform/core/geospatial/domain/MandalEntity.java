/*
 * Purpose: Persists sub-district or mandal administrative records.
 * Why it exists: Rural administrative operations often occur below district level.
 * Architecture fit: Child administrative unit below district.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for mandals or sub-districts. */
@Entity
@Table(name = "mandals", schema = "geospatial")
public class MandalEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID districtId;

    protected MandalEntity() {
    }

    public MandalEntity(UUID districtId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.districtId = districtId;
    }

    public UUID districtId() { return districtId; }
}

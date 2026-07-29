/*
 * Purpose: Persists district-level administrative records.
 * Why it exists: Districts are a primary unit for rural operations and decision intelligence.
 * Architecture fit: Child administrative unit below state.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for districts. */
@Entity
@Table(name = "districts", schema = "geospatial")
public class DistrictEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID stateId;

    protected DistrictEntity() {
    }

    public DistrictEntity(UUID stateId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.stateId = stateId;
    }

    public UUID stateId() { return stateId; }
}

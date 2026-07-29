/*
 * Purpose: Persists ward-level administrative records.
 * Why it exists: Rural and peri-urban deployments require sub-village administrative mapping.
 * Architecture fit: Child administrative unit under villages.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for wards. */
@Entity
@Table(name = "wards", schema = "geospatial")
public class WardEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID villageId;

    protected WardEntity() {
    }

    public WardEntity(UUID villageId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.villageId = villageId;
    }

    public UUID villageId() { return villageId; }
}

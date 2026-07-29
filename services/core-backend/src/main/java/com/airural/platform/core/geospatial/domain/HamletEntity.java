/*
 * Purpose: Persists hamlet-level administrative records.
 * Why it exists: Household mapping in rural regions often needs locality granularity below ward or village.
 * Architecture fit: Child administrative unit under wards and parent unit for households.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for hamlets. */
@Entity
@Table(name = "hamlets", schema = "geospatial")
public class HamletEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID wardId;

    protected HamletEntity() {
    }

    public HamletEntity(UUID wardId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.wardId = wardId;
    }

    public UUID wardId() { return wardId; }
}

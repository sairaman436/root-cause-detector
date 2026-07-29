/*
 * Purpose: Persists gram panchayat administrative records.
 * Why it exists: Gram panchayats anchor village-level rural governance.
 * Architecture fit: Child administrative unit below block.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for gram panchayats. */
@Entity
@Table(name = "gram_panchayats", schema = "geospatial")
public class GramPanchayatEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID blockId;

    protected GramPanchayatEntity() {
    }

    public GramPanchayatEntity(UUID blockId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.blockId = blockId;
    }

    public UUID blockId() { return blockId; }
}

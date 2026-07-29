/*
 * Purpose: Persists block-level administrative records.
 * Why it exists: Blocks provide the next operational hierarchy below mandals.
 * Architecture fit: Child administrative unit below mandal.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for blocks. */
@Entity
@Table(name = "blocks", schema = "geospatial")
public class BlockEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID mandalId;

    protected BlockEntity() {
    }

    public BlockEntity(UUID mandalId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.mandalId = mandalId;
    }

    public UUID mandalId() { return mandalId; }
}

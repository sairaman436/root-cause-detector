/*
 * Purpose: Persists state-level administrative records.
 * Why it exists: State hierarchy enables regional search, reporting, and future policy analysis.
 * Architecture fit: Child administrative unit below country.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** JPA entity for states. */
@Entity
@Table(name = "states", schema = "geospatial")
public class StateEntity extends AdministrativeUnitEntity {
    @Column(nullable = false)
    private UUID countryId;

    protected StateEntity() {
    }

    public StateEntity(UUID countryId, String code, String name, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.countryId = countryId;
    }

    public UUID countryId() { return countryId; }
}

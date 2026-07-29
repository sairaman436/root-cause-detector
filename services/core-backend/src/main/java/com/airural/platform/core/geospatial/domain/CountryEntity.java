/*
 * Purpose: Persists country-level administrative records.
 * Why it exists: The platform hierarchy starts at country scope for enterprise and government deployments.
 * Architecture fit: Root administrative aggregate for geospatial hierarchy.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** JPA entity for countries. */
@Entity
@Table(name = "countries", schema = "geospatial")
public class CountryEntity extends AdministrativeUnitEntity {
    @Column(length = 3)
    private String isoCode;

    protected CountryEntity() {
    }

    public CountryEntity(String code, String name, String isoCode, BigDecimal latitude, BigDecimal longitude) {
        super(code, name, latitude, longitude);
        this.isoCode = isoCode;
    }

    public String isoCode() { return isoCode; }
}

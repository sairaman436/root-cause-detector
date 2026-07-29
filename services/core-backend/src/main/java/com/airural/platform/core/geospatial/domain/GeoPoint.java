/*
 * Purpose: Represents a validated latitude and longitude pair.
 * Why it exists: Spatial calculations and persistence must reject impossible GPS coordinates.
 * Architecture fit: Value object used by geospatial application services.
 */
package com.airural.platform.core.geospatial.domain;

import java.math.BigDecimal;

/** Immutable GPS coordinate pair. */
public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {
    public GeoPoint {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}

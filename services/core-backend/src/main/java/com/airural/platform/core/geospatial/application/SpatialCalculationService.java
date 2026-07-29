/*
 * Purpose: Implements portable spatial calculations used by geospatial workflows.
 * Why it exists: Milestone 5 requires distance, radius, bounding-box, and clustering behavior without coupling to a single spatial database extension.
 * Architecture fit: Domain service for spatial math that can later be replaced or augmented by PostGIS-backed queries.
 */
package com.airural.platform.core.geospatial.application;

import com.airural.platform.core.geospatial.domain.GeoPoint;
import java.math.*;
import org.springframework.stereotype.Service;

/** Service for spatial calculations using WGS84 coordinates. */
@Service
public class SpatialCalculationService {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    /** Calculates haversine distance in kilometers. */
    public double distanceKm(GeoPoint from, GeoPoint to) {
        double lat1 = Math.toRadians(from.latitude().doubleValue());
        double lat2 = Math.toRadians(to.latitude().doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(to.longitude().doubleValue() - from.longitude().doubleValue());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Builds a coarse bounding box around a point and radius. */
    public BoundingBox boundingBox(BigDecimal latitude, BigDecimal longitude, double radiusKm) {
        if (radiusKm < 0) {
            throw new GeospatialException("INVALID_RADIUS", "Radius must be greater than or equal to zero", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        GeoPoint point = new GeoPoint(latitude, longitude);
        double lat = point.latitude().doubleValue();
        double lon = point.longitude().doubleValue();
        double deltaLat = radiusKm / 111.32;
        double deltaLon = radiusKm / (111.32 * Math.max(Math.cos(Math.toRadians(lat)), 0.01));
        return new BoundingBox(
                decimal(Math.max(-90, lat - deltaLat)),
                decimal(Math.max(-180, lon - deltaLon)),
                decimal(Math.min(90, lat + deltaLat)),
                decimal(Math.min(180, lon + deltaLon)));
    }

    /** Produces a stable grid-cell key for lightweight clustering. */
    public String gridCell(BigDecimal latitude, BigDecimal longitude, double gridSizeDegrees) {
        if (gridSizeDegrees <= 0) {
            throw new GeospatialException("INVALID_GRID_SIZE", "Grid size must be greater than zero", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        GeoPoint point = new GeoPoint(latitude, longitude);
        long latCell = (long) Math.floor(point.latitude().doubleValue() / gridSizeDegrees);
        long lonCell = (long) Math.floor(point.longitude().doubleValue() / gridSizeDegrees);
        return latCell + ":" + lonCell;
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }

    /** Bounding box for portable spatial filtering. */
    public record BoundingBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude) {
    }
}

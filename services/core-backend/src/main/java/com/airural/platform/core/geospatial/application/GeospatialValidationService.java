/*
 * Purpose: Validates geospatial inputs before persistence.
 * Why it exists: GPS, bounding-box, and GeoJSON validation must be consistent across hierarchy, boundary, household, and asset APIs.
 * Architecture fit: Application-layer validation service for the Geospatial module.
 */
package com.airural.platform.core.geospatial.application;

import com.airural.platform.core.geospatial.domain.GeoPoint;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Shared validator for geospatial requests. */
@Service
public class GeospatialValidationService {
    /** Validates optional point coordinates when either coordinate is supplied. */
    public void optionalPoint(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new GeospatialException("INCOMPLETE_GPS_POINT", "Latitude and longitude must be supplied together", HttpStatus.BAD_REQUEST);
        }
        new GeoPoint(latitude, longitude);
    }

    /** Validates a required point. */
    public void requiredPoint(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new GeospatialException("GPS_POINT_REQUIRED", "Latitude and longitude are required", HttpStatus.BAD_REQUEST);
        }
        new GeoPoint(latitude, longitude);
    }

    /** Validates bounding-box coordinates. */
    public void boundingBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude) {
        requiredPoint(minLatitude, minLongitude);
        requiredPoint(maxLatitude, maxLongitude);
        if (minLatitude.compareTo(maxLatitude) > 0 || minLongitude.compareTo(maxLongitude) > 0) {
            throw new GeospatialException("INVALID_BOUNDING_BOX", "Minimum coordinates must be less than or equal to maximum coordinates", HttpStatus.BAD_REQUEST);
        }
    }

    /** Performs lightweight GeoJSON validation while leaving full geometry validation to future spatial adapters. */
    public void geojson(String geojson) {
        if (geojson == null || geojson.isBlank()) {
            throw new GeospatialException("GEOJSON_REQUIRED", "GeoJSON is required", HttpStatus.BAD_REQUEST);
        }
        String trimmed = geojson.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}") || !trimmed.contains("\"type\"")) {
            throw new GeospatialException("INVALID_GEOJSON", "GeoJSON must be a JSON object containing a type field", HttpStatus.BAD_REQUEST);
        }
    }
}

/*
 * Purpose: Verifies portable spatial calculations and input validation.
 * Why it exists: Distance, radius, bbox, and clustering are core Milestone 5 geospatial services.
 * Architecture fit: Unit coverage for the Geospatial application service layer.
 */
package com.airural.platform.core.geospatial;

import static org.assertj.core.api.Assertions.*;

import com.airural.platform.core.geospatial.application.*;
import com.airural.platform.core.geospatial.domain.GeoPoint;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Unit tests for spatial calculations and validators. */
class SpatialCalculationServiceTests {
    private final SpatialCalculationService spatial = new SpatialCalculationService();
    private final GeospatialValidationService validation = new GeospatialValidationService();

    /** Haversine distance returns a realistic value for known coordinates. */
    @Test
    void distanceUsesHaversineFormula() {
        double distance = spatial.distanceKm(
                new GeoPoint(BigDecimal.valueOf(17.3850), BigDecimal.valueOf(78.4867)),
                new GeoPoint(BigDecimal.valueOf(17.6868), BigDecimal.valueOf(83.2185)));

        assertThat(distance).isBetween(495.0, 510.0);
    }

    /** Bounding boxes expand around the origin point and reject invalid radius values. */
    @Test
    void boundingBoxValidatesRadiusAndCoordinates() {
        SpatialCalculationService.BoundingBox box = spatial.boundingBox(BigDecimal.valueOf(17.3850), BigDecimal.valueOf(78.4867), 10);

        assertThat(box.minLatitude()).isLessThan(BigDecimal.valueOf(17.3850));
        assertThat(box.maxLatitude()).isGreaterThan(BigDecimal.valueOf(17.3850));
        assertThatThrownBy(() -> spatial.boundingBox(BigDecimal.ZERO, BigDecimal.ZERO, -1))
                .isInstanceOf(GeospatialException.class)
                .hasMessageContaining("Radius");
    }

    /** Validators reject incomplete GPS points, invalid boxes, and malformed GeoJSON. */
    @Test
    void validationRejectsInvalidSpatialInputs() {
        assertThatThrownBy(() -> validation.optionalPoint(BigDecimal.ONE, null)).isInstanceOf(GeospatialException.class);
        assertThatThrownBy(() -> validation.boundingBox(BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO)).isInstanceOf(GeospatialException.class);
        assertThatThrownBy(() -> validation.geojson("{\"coordinates\":[]}")).isInstanceOf(GeospatialException.class);
    }
}

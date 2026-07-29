/*
 * Purpose: Defines supported geometry shape categories.
 * Why it exists: Geography records must distinguish point, polygon, and multipolygon payloads across GeoJSON and WKT formats.
 * Architecture fit: Domain vocabulary for production spatial metadata.
 */
package com.airural.platform.core.geospatial.domain;

/** Supported geometry shape types. */
public enum GeoShapeType {
    POINT,
    POLYGON,
    MULTIPOLYGON
}

/*
 * Purpose: Defines supported coordinate reference systems for geography records.
 * Why it exists: Spatial data arrives from GPS, map tiles, and future local survey grids and must identify its coordinate system.
 * Architecture fit: Domain vocabulary for GeoLocation, GeoBoundary, GeoZone, and GeoRegion metadata.
 */
package com.airural.platform.core.geospatial.domain;

/** Supported coordinate systems for geography data. */
public enum CoordinateSystem {
    WGS84,
    WEB_MERCATOR,
    LOCAL_GRID
}

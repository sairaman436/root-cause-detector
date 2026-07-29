/*
 * Purpose: Defines administrative hierarchy levels.
 * Why it exists: Boundaries and hierarchy APIs need a stable level vocabulary.
 * Architecture fit: Domain vocabulary for the Geospatial module.
 */
package com.airural.platform.core.geospatial.domain;

/** Supported administrative hierarchy levels. */
public enum AdministrativeLevel {
    COUNTRY,
    STATE,
    DISTRICT,
    MANDAL,
    BLOCK,
    GRAM_PANCHAYAT,
    VILLAGE,
    WARD,
    HAMLET,
    HOUSEHOLD
}

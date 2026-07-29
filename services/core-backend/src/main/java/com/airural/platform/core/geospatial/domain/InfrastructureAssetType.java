/*
 * Purpose: Defines supported infrastructure asset categories.
 * Why it exists: Infrastructure search and nearest-facility queries need governed asset type values.
 * Architecture fit: Domain vocabulary for infrastructure mapping.
 */
package com.airural.platform.core.geospatial.domain;

/** Infrastructure assets that can be mapped geospatially. */
public enum InfrastructureAssetType {
    SCHOOL,
    HOSPITAL,
    PHC,
    ANGANWADI_CENTER,
    ROAD,
    WATER_TANK,
    BORE_WELL,
    MARKET,
    BANK,
    POLICE_STATION,
    GOVERNMENT_OFFICE,
    ELECTRIC_TRANSFORMER,
    COMMUNITY_BUILDING,
    PUBLIC_DISTRIBUTION_SHOP,
    COMMUNITY_HALL
}

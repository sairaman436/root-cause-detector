/*
 * Purpose: Maps geospatial entities to API DTOs.
 * Why it exists: API contracts must remain decoupled from persistence models.
 * Architecture fit: Application mapper for Milestone 5 geospatial responses.
 */
package com.airural.platform.core.geospatial.application;

import com.airural.platform.core.geospatial.domain.*;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import org.springframework.stereotype.Component;

/** Maps geospatial entities into response records. */
@Component
public class GeospatialMapper {
    public AdminUnitResponse country(CountryEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.COUNTRY, null, entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse state(StateEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.STATE, entity.countryId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse district(DistrictEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.DISTRICT, entity.stateId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse mandal(MandalEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.MANDAL, entity.districtId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse block(BlockEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.BLOCK, entity.mandalId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse gramPanchayat(GramPanchayatEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.GRAM_PANCHAYAT, entity.blockId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse ward(WardEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.WARD, entity.villageId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public AdminUnitResponse hamlet(HamletEntity entity) {
        return new AdminUnitResponse(entity.id(), AdministrativeLevel.HAMLET, entity.wardId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(), entity.createdAt(), entity.updatedAt());
    }

    public VillageResponse village(VillageEntity entity) {
        return village(entity, null);
    }

    public VillageResponse village(VillageEntity entity, Double distanceKm) {
        return new VillageResponse(
                entity.id(), entity.gramPanchayatId(), entity.code(), entity.name(), entity.latitude(), entity.longitude(),
                entity.elevationMeters(), entity.areaSqKm(), entity.population(), entity.householdCount(), entity.geojson(),
                entity.minLatitude(), entity.minLongitude(), entity.maxLatitude(), entity.maxLongitude(), distanceKm,
                entity.createdAt(), entity.updatedAt());
    }

    public HouseholdResponse household(HouseholdEntity entity) {
        return new HouseholdResponse(entity.id(), entity.hamletId(), entity.householdCode(), entity.headOfHousehold(), entity.address(), entity.latitude(), entity.longitude(), entity.surveyId(), entity.evidenceId(), entity.iotMetadataJson(), entity.createdAt(), entity.updatedAt());
    }

    public InfrastructureAssetResponse asset(InfrastructureAssetEntity entity) {
        return asset(entity, null);
    }

    public InfrastructureAssetResponse asset(InfrastructureAssetEntity entity, Double distanceKm) {
        return new InfrastructureAssetResponse(entity.id(), entity.villageId(), entity.assetType(), entity.code(), entity.name(), entity.description(), entity.latitude(), entity.longitude(), entity.metadataJson(), distanceKm, entity.createdAt(), entity.updatedAt());
    }

    public GeoBoundaryResponse boundary(GeoBoundaryEntity entity) {
        return new GeoBoundaryResponse(entity.id(), entity.entityType(), entity.entityId(), entity.geojson(), entity.minLatitude(), entity.minLongitude(), entity.maxLatitude(), entity.maxLongitude(), entity.areaSqKm(), entity.createdAt(), entity.updatedAt());
    }

    public HierarchyPathResponse hierarchy(AdministrativeHierarchyEntity entity) {
        return new HierarchyPathResponse(entity.id(), entity.householdId(), entity.pathCode(), entity.pathName(), entity.createdAt());
    }
}

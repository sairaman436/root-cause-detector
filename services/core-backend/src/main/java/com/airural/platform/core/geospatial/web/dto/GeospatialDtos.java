/*
 * Purpose: Defines REST contracts for Enterprise Geospatial Intelligence APIs.
 * Why it exists: Controllers need stable DTOs separate from persistence entities.
 * Architecture fit: Web adapter contracts for Milestone 5 geospatial hierarchy and spatial services.
 */
package com.airural.platform.core.geospatial.web.dto;

import com.airural.platform.core.geospatial.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Namespace for geospatial API DTO records. */
public final class GeospatialDtos {
    private GeospatialDtos() {
    }

    /** Request to create a country. */
    public record CreateCountryRequest(@NotBlank @Size(max = 3) String code, @NotBlank @Size(max = 180) String name, @Size(max = 3) String isoCode, BigDecimal latitude, BigDecimal longitude) {
    }

    /** Request to create a child administrative unit. */
    public record CreateAdminUnitRequest(@NotNull UUID parentId, @NotBlank @Size(max = 32) String code, @NotBlank @Size(max = 180) String name, BigDecimal latitude, BigDecimal longitude) {
    }

    /** Request to create a village with centroid, statistics, and optional GeoJSON metadata. */
    public record CreateVillageRequest(
            @NotNull UUID gramPanchayatId,
            @NotBlank @Size(max = 32) String code,
            @NotBlank @Size(max = 180) String name,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            BigDecimal elevationMeters,
            BigDecimal areaSqKm,
            @Min(0) Long population,
            @Min(0) Long householdCount,
            String geojson,
            BigDecimal minLatitude,
            BigDecimal minLongitude,
            BigDecimal maxLatitude,
            BigDecimal maxLongitude) {
    }

    /** Request to create a household location. */
    public record CreateHouseholdRequest(
            @NotNull UUID hamletId,
            @NotBlank @Size(max = 64) String householdCode,
            @Size(max = 180) String headOfHousehold,
            @NotBlank @Size(max = 500) String address,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            UUID surveyId,
            UUID evidenceId,
            String iotMetadataJson) {
    }

    /** Request to create infrastructure asset metadata. */
    public record CreateInfrastructureAssetRequest(
            UUID villageId,
            @NotNull InfrastructureAssetType assetType,
            @NotBlank @Size(max = 80) String code,
            @NotBlank @Size(max = 220) String name,
            @Size(max = 1000) String description,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            String metadataJson) {
    }

    /** Request to create a GeoJSON boundary. */
    public record CreateGeoBoundaryRequest(
            @NotNull AdministrativeLevel entityType,
            @NotNull UUID entityId,
            @NotBlank String geojson,
            @NotNull BigDecimal minLatitude,
            @NotNull BigDecimal minLongitude,
            @NotNull BigDecimal maxLatitude,
            @NotNull BigDecimal maxLongitude,
            BigDecimal areaSqKm) {
    }

    /** Generic administrative-unit response. */
    public record AdminUnitResponse(UUID id, AdministrativeLevel level, UUID parentId, String code, String name, BigDecimal latitude, BigDecimal longitude, Instant createdAt, Instant updatedAt) {
    }

    /** Village response. */
    public record VillageResponse(
            UUID id,
            UUID gramPanchayatId,
            String code,
            String name,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal elevationMeters,
            BigDecimal areaSqKm,
            Long population,
            Long householdCount,
            String geojson,
            BigDecimal minLatitude,
            BigDecimal minLongitude,
            BigDecimal maxLatitude,
            BigDecimal maxLongitude,
            Double distanceKm,
            Instant createdAt,
            Instant updatedAt) {
    }

    /** Household response. */
    public record HouseholdResponse(UUID id, UUID hamletId, String householdCode, String headOfHousehold, String address, BigDecimal latitude, BigDecimal longitude, UUID surveyId, UUID evidenceId, String iotMetadataJson, Instant createdAt, Instant updatedAt) {
    }

    /** Infrastructure asset response. */
    public record InfrastructureAssetResponse(UUID id, UUID villageId, InfrastructureAssetType assetType, String code, String name, String description, BigDecimal latitude, BigDecimal longitude, String metadataJson, Double distanceKm, Instant createdAt, Instant updatedAt) {
    }

    /** GeoJSON boundary response. */
    public record GeoBoundaryResponse(UUID id, AdministrativeLevel entityType, UUID entityId, String geojson, BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude, BigDecimal areaSqKm, Instant createdAt, Instant updatedAt) {
    }

    /** Distance calculation response. */
    public record DistanceResponse(BigDecimal fromLatitude, BigDecimal fromLongitude, BigDecimal toLatitude, BigDecimal toLongitude, double distanceKm) {
    }

    /** Village cluster response. */
    public record VillageClusterResponse(String cellId, long villageCount, BigDecimal centroidLatitude, BigDecimal centroidLongitude) {
    }

    /** Denormalized administrative hierarchy path response. */
    public record HierarchyPathResponse(UUID id, UUID householdId, String pathCode, String pathName, Instant createdAt) {
    }
}

/*
 * Purpose: Provides reusable JPA specifications for geospatial search.
 * Why it exists: Application services need composable filters without embedding query logic in controllers.
 * Architecture fit: Infrastructure query factory for portable latitude/longitude spatial operations.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Specification factory for geospatial entities. */
public final class GeospatialSpecifications {
    private GeospatialSpecifications() {
    }

    public static Specification<VillageEntity> activeVillages() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<VillageEntity> villageNameContains(String queryText) {
        return (root, query, cb) -> queryText == null || queryText.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + queryText.toLowerCase() + "%");
    }

    public static Specification<VillageEntity> villageGramPanchayatEquals(UUID gramPanchayatId) {
        return (root, query, cb) -> gramPanchayatId == null ? cb.conjunction() : cb.equal(root.get("gramPanchayatId"), gramPanchayatId);
    }

    public static Specification<VillageEntity> villageWithinBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude) {
        return (root, query, cb) -> cb.and(
                cb.between(root.get("latitude"), minLatitude, maxLatitude),
                cb.between(root.get("longitude"), minLongitude, maxLongitude));
    }

    public static Specification<InfrastructureAssetEntity> activeAssets() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<InfrastructureAssetEntity> assetTypeEquals(InfrastructureAssetType assetType) {
        return (root, query, cb) -> assetType == null ? cb.conjunction() : cb.equal(root.get("assetType"), assetType);
    }

    public static Specification<InfrastructureAssetEntity> assetVillageEquals(UUID villageId) {
        return (root, query, cb) -> villageId == null ? cb.conjunction() : cb.equal(root.get("villageId"), villageId);
    }

    public static Specification<InfrastructureAssetEntity> assetNameContains(String queryText) {
        return (root, query, cb) -> queryText == null || queryText.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + queryText.toLowerCase() + "%");
    }

    public static Specification<InfrastructureAssetEntity> assetWithinBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude) {
        return (root, query, cb) -> cb.and(
                cb.between(root.get("latitude"), minLatitude, maxLatitude),
                cb.between(root.get("longitude"), minLongitude, maxLongitude));
    }

    public static Specification<HouseholdEntity> activeHouseholds() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<HouseholdEntity> householdHamletEquals(UUID hamletId) {
        return (root, query, cb) -> hamletId == null ? cb.conjunction() : cb.equal(root.get("hamletId"), hamletId);
    }

    public static Specification<HouseholdEntity> householdWithinBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude) {
        return (root, query, cb) -> cb.and(
                cb.between(root.get("latitude"), minLatitude, maxLatitude),
                cb.between(root.get("longitude"), minLongitude, maxLongitude));
    }
}

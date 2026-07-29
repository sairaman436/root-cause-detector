/*
 * Purpose: Provides persistence access for denormalized administrative hierarchy paths.
 * Why it exists: Consumers need fast lookup of full geography context for households and search results.
 * Architecture fit: Infrastructure repository for read-optimized hierarchy projections.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.AdministrativeHierarchyEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for administrative hierarchy projections. */
public interface AdministrativeHierarchyRepository extends JpaRepository<AdministrativeHierarchyEntity, UUID> {
    Optional<AdministrativeHierarchyEntity> findByHouseholdId(UUID householdId);
    Optional<AdministrativeHierarchyEntity> findByPathCode(String pathCode);
}

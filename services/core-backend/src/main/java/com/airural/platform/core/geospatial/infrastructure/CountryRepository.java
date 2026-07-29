/*
 * Purpose: Provides persistence access for country administrative units.
 * Why it exists: Geospatial services need governed creation and lookup of hierarchy roots.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.CountryEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for countries. */
public interface CountryRepository extends JpaRepository<CountryEntity, UUID> {
    boolean existsByCode(String code);
    Optional<CountryEntity> findByIdAndIsActiveTrue(UUID id);
    List<CountryEntity> findByIsActiveTrueOrderByNameAsc();
}

/*
 * Purpose: Provides persistence access for state administrative units.
 * Why it exists: Geospatial services need governed child lookup below countries.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.StateEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for states. */
public interface StateRepository extends JpaRepository<StateEntity, UUID> {
    boolean existsByCountryIdAndCode(UUID countryId, String code);
    Optional<StateEntity> findByIdAndIsActiveTrue(UUID id);
    List<StateEntity> findByCountryIdAndIsActiveTrueOrderByNameAsc(UUID countryId);
}

/*
 * Purpose: Provides persistence access for block administrative units.
 * Why it exists: Geospatial services need governed child lookup below mandals.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.BlockEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for blocks. */
public interface BlockRepository extends JpaRepository<BlockEntity, UUID> {
    boolean existsByMandalIdAndCode(UUID mandalId, String code);
    Optional<BlockEntity> findByIdAndIsActiveTrue(UUID id);
    List<BlockEntity> findByMandalIdAndIsActiveTrueOrderByNameAsc(UUID mandalId);
}

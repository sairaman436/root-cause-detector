/*
 * Purpose: Provides persistence access for gram panchayat administrative units.
 * Why it exists: Geospatial services need governed child lookup below blocks.
 * Architecture fit: Infrastructure repository for the Geospatial module.
 */
package com.airural.platform.core.geospatial.infrastructure;

import com.airural.platform.core.geospatial.domain.GramPanchayatEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for gram panchayats. */
public interface GramPanchayatRepository extends JpaRepository<GramPanchayatEntity, UUID> {
    boolean existsByBlockIdAndCode(UUID blockId, String code);
    Optional<GramPanchayatEntity> findByIdAndIsActiveTrue(UUID id);
    List<GramPanchayatEntity> findByBlockIdAndIsActiveTrueOrderByNameAsc(UUID blockId);
}

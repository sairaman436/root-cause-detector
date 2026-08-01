/*
 * Purpose: Persists research dataset records.
 * Why it exists: Research datasets need provenance, license, quality, and governance status tracking.
 * Architecture fit: JPA adapter for Research-1 dataset registry.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchDatasetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research datasets. */
public interface ResearchDatasetRepository extends JpaRepository<ResearchDatasetEntity, UUID> {}

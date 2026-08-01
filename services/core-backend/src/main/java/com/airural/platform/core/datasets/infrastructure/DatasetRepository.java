/*
 * Purpose: Persists governed AI dataset registry records.
 * Why it exists: Dataset lifecycle operations need a transactional repository boundary.
 * Architecture fit: Infrastructure adapter for the AI-1 Dataset Engineering Platform.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset registry records. */
public interface DatasetRepository extends JpaRepository<DatasetEntity, UUID> {
}

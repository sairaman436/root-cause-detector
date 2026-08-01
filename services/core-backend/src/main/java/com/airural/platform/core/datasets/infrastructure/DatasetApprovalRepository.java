/*
 * Purpose: Persists dataset approval decisions.
 * Why it exists: Dataset release, export, and downstream ML usage require auditable approval gates.
 * Architecture fit: Infrastructure adapter for dataset governance workflows.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset approvals. */
public interface DatasetApprovalRepository extends JpaRepository<DatasetApprovalEntity, UUID> {
}

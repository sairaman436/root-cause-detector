/*
 * Purpose: Persists deployment package records.
 * Why it exists: Deployment target packages need pageable API access and release history.
 * Architecture fit: JPA adapter for AI-6 packaging outputs.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.DeploymentPackageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for deployment packages. */
public interface DeploymentPackageRepository extends JpaRepository<DeploymentPackageEntity, UUID> {}

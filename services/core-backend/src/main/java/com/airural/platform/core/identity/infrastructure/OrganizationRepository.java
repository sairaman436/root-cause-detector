/*
 * Purpose: Provides persistence access to organizations.
 * Why it exists: Organization management and registration require durable organization lookup.
 * Architecture fit: Infrastructure adapter for the identity application layer.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.OrganizationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for organizations. */
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
    /** Finds an organization by its code. */
    Optional<OrganizationEntity> findByCode(String code);

    /** Returns whether an organization code is already used. */
    boolean existsByCode(String code);
}

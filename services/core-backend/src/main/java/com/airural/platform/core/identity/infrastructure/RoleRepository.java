/*
 * Purpose: Provides persistence access to RBAC roles.
 * Why it exists: User authentication and authorization depend on durable role assignments.
 * Architecture fit: Infrastructure adapter for role management.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for roles. */
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    /** Finds a role by canonical name. */
    Optional<RoleEntity> findByName(String name);

    /** Returns whether a role already exists. */
    boolean existsByName(String name);
}

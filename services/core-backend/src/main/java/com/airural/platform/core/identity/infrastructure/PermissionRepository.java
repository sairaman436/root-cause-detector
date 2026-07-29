/*
 * Purpose: Provides persistence access to RBAC permissions.
 * Why it exists: Role management needs governed permission lookup and assignment.
 * Architecture fit: Infrastructure adapter for permission management.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.PermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for permissions. */
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    /** Finds a permission by its canonical name. */
    Optional<PermissionEntity> findByName(String name);

    /** Finds permissions by canonical names. */
    List<PermissionEntity> findByNameIn(Collection<String> names);
}

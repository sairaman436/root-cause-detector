/*
 * Purpose: Provides persistence access to user accounts.
 * Why it exists: Authentication and user management require durable user lookup.
 * Architecture fit: Infrastructure adapter for the identity application layer.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.UserAccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for user accounts. */
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {
    /** Finds a user by email with roles and permissions loaded. */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserAccountEntity> findByEmail(String email);

    /** Finds a user by ID with roles and permissions loaded. */
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "organization"})
    Optional<UserAccountEntity> findWithRolesById(UUID id);

    /** Returns whether an email address is already used. */
    boolean existsByEmail(String email);

    /** Returns whether a username is already used. */
    boolean existsByUsername(String username);
}

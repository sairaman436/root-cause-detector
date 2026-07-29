/*
 * Purpose: Provides persistence access to refresh tokens.
 * Why it exists: Refresh and logout workflows require token lookup and revocation.
 * Architecture fit: Infrastructure adapter for secure token lifecycle management.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.RefreshTokenEntity;
import com.airural.platform.core.identity.domain.UserAccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for refresh tokens. */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    /** Finds a refresh token by its SHA-256 hash. */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    /** Revokes all persisted refresh tokens for a user. */
    long deleteByUser(UserAccountEntity user);
}

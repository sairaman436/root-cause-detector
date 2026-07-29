/*
 * Purpose: Persists refresh token state.
 * Why it exists: Refresh tokens must support revocation, logout, and expiry enforcement.
 * Architecture fit: Implements the approved secure token lifecycle management requirement.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for a refresh token. */
@Entity
@Table(name = "refresh_tokens", schema = "identity")
public class RefreshTokenEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccountEntity user;

    @Column(nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected RefreshTokenEntity() {
    }

    public RefreshTokenEntity(UserAccountEntity user, String tokenHash, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() {
        return id;
    }

    public UserAccountEntity user() {
        return user;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public boolean isUsable(Instant now) {
        return isActive && revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke() {
        this.revokedAt = Instant.now();
        this.isActive = false;
        this.updatedAt = this.revokedAt;
    }
}

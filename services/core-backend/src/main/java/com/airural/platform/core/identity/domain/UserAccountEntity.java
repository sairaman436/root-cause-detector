/*
 * Purpose: Persists a platform user account.
 * Why it exists: Authentication, RBAC, profile ownership, and audit trails require a durable user identity.
 * Architecture fit: Implements user management and credential storage in the identity platform.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** JPA entity for a user account. */
@Entity
@Table(name = "users", schema = "identity")
public class UserAccountEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 180)
    private String fullName;

    @Column(length = 40)
    private String phoneNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            schema = "identity",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    private Instant lastLoginAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected UserAccountEntity() {
    }

    public UserAccountEntity(
            OrganizationEntity organization,
            String username,
            String email,
            String fullName,
            String phoneNumber,
            String passwordHash,
            Set<RoleEntity> roles) {
        this.id = UUID.randomUUID();
        this.organization = organization;
        this.username = username;
        this.email = email.toLowerCase();
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.status = AccountStatus.ACTIVE;
        this.roles = new HashSet<>(roles);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() {
        return id;
    }

    public OrganizationEntity organization() {
        return organization;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public AccountStatus status() {
        return status;
    }

    public Set<RoleEntity> roles() {
        return Set.copyOf(roles);
    }

    public Instant lastLoginAt() {
        return lastLoginAt;
    }

    public void markLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = this.lastLoginAt;
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
}

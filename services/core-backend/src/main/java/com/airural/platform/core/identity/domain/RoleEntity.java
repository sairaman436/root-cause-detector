/*
 * Purpose: Persists a role and its permissions.
 * Why it exists: RBAC role assignment is central to authorization and administrative governance.
 * Architecture fit: Implements role management in the identity platform.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** JPA entity for an RBAC role. */
@Entity
@Table(name = "roles", schema = "identity")
public class RoleEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            schema = "identity",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected RoleEntity() {
    }

    public RoleEntity(String name, String description, Set<PermissionEntity> permissions) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Set<PermissionEntity> permissions() {
        return Set.copyOf(permissions);
    }
}

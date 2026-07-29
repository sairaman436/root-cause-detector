/*
 * Purpose: Persists an RBAC permission.
 * Why it exists: Authorization decisions must be derived from managed permissions rather than hard-coded roles only.
 * Architecture fit: Implements permission management for the approved RBAC model.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for an RBAC permission. */
@Entity
@Table(name = "permissions", schema = "identity")
public class PermissionEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String resource;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected PermissionEntity() {
    }

    public PermissionEntity(String name, String resource, String action, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
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

    public String resource() {
        return resource;
    }

    public String action() {
        return action;
    }

    public String description() {
        return description;
    }
}

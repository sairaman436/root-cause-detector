/*
 * Purpose: Persists an organization that owns platform users.
 * Why it exists: Identity is tenant-aware from the foundation while retaining a simple governance model.
 * Architecture fit: Implements the approved organization management boundary.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for an organization. */
@Entity
@Table(name = "organizations", schema = "identity")
public class OrganizationEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrganizationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected OrganizationEntity() {
    }

    public OrganizationEntity(String name, String code) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.code = code;
        this.status = OrganizationStatus.ACTIVE;
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

    public String code() {
        return code;
    }

    public OrganizationStatus status() {
        return status;
    }
}

/*
 * Purpose: Stores resolved administrative paths for fast hierarchy lookup.
 * Why it exists: Field, reporting, and future AI workloads need stable denormalized geography context.
 * Architecture fit: Read-optimized projection maintained by geospatial application services.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for resolved administrative hierarchy paths. */
@Entity
@Table(name = "administrative_hierarchy", schema = "geospatial")
public class AdministrativeHierarchyEntity {
    @Id
    private UUID id;

    private UUID countryId;
    private UUID stateId;
    private UUID districtId;
    private UUID mandalId;
    private UUID blockId;
    private UUID gramPanchayatId;
    private UUID villageId;
    private UUID wardId;
    private UUID hamletId;
    private UUID householdId;

    @Column(nullable = false, length = 700)
    private String pathCode;

    @Column(nullable = false, length = 1200)
    private String pathName;

    @Column(nullable = false)
    private Instant createdAt;

    protected AdministrativeHierarchyEntity() {
    }

    public AdministrativeHierarchyEntity(
            UUID countryId,
            UUID stateId,
            UUID districtId,
            UUID mandalId,
            UUID blockId,
            UUID gramPanchayatId,
            UUID villageId,
            UUID wardId,
            UUID hamletId,
            UUID householdId,
            String pathCode,
            String pathName) {
        this.id = UUID.randomUUID();
        this.countryId = countryId;
        this.stateId = stateId;
        this.districtId = districtId;
        this.mandalId = mandalId;
        this.blockId = blockId;
        this.gramPanchayatId = gramPanchayatId;
        this.villageId = villageId;
        this.wardId = wardId;
        this.hamletId = hamletId;
        this.householdId = householdId;
        this.pathCode = pathCode;
        this.pathName = pathName;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID countryId() { return countryId; }
    public UUID stateId() { return stateId; }
    public UUID districtId() { return districtId; }
    public UUID mandalId() { return mandalId; }
    public UUID blockId() { return blockId; }
    public UUID gramPanchayatId() { return gramPanchayatId; }
    public UUID villageId() { return villageId; }
    public UUID wardId() { return wardId; }
    public UUID hamletId() { return hamletId; }
    public UUID householdId() { return householdId; }
    public String pathCode() { return pathCode; }
    public String pathName() { return pathName; }
    public Instant createdAt() { return createdAt; }
}

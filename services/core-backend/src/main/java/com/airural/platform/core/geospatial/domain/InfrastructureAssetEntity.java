/*
 * Purpose: Persists mapped public infrastructure assets and facility metadata.
 * Why it exists: Root-cause discovery depends on proximity to schools, health facilities, water assets, markets, and public offices.
 * Architecture fit: Geospatial aggregate for infrastructure search and nearest-facility workflows.
 */
package com.airural.platform.core.geospatial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for infrastructure assets. */
@Entity
@Table(name = "infrastructure_assets", schema = "geospatial")
public class InfrastructureAssetEntity {
    @Id
    private UUID id;

    private UUID villageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private InfrastructureAssetType assetType;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected InfrastructureAssetEntity() {
    }

    public InfrastructureAssetEntity(
            UUID villageId,
            InfrastructureAssetType assetType,
            String code,
            String name,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            String metadataJson) {
        this.id = UUID.randomUUID();
        this.villageId = villageId;
        this.assetType = assetType;
        this.code = code;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metadataJson = metadataJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID villageId() { return villageId; }
    public InfrastructureAssetType assetType() { return assetType; }
    public String code() { return code; }
    public String name() { return name; }
    public String description() { return description; }
    public BigDecimal latitude() { return latitude; }
    public BigDecimal longitude() { return longitude; }
    public String metadataJson() { return metadataJson; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }
}

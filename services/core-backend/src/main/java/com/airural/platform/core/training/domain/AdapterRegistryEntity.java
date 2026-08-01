/*
 * Purpose: Stores LoRA and QLoRA adapter metadata.
 * Why it exists: Adapter artifacts must be tracked separately from base and merged models for future recovery and review.
 * Architecture fit: AI-3 adapter registry entity; it records metadata only and does not merge adapters.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Adapter registry entity. */
@Entity
@Table(name = "adapter_registry", schema = "training")
public class AdapterRegistryEntity {
    @Id private UUID id;
    private UUID jobId;
    private String adapterName;
    private String adapterType;
    private String baseModel;
    private String storageUri;
    private String checksum;
    private String status;
    private Instant createdAt;

    protected AdapterRegistryEntity() {}

    /** Creates adapter registry metadata. */
    public AdapterRegistryEntity(UUID id, UUID jobId, String adapterName, String adapterType, String baseModel, String storageUri, String checksum, String status, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.adapterName = adapterName; this.adapterType = adapterType; this.baseModel = baseModel; this.storageUri = storageUri; this.checksum = checksum; this.status = status; this.createdAt = createdAt;
    }
}

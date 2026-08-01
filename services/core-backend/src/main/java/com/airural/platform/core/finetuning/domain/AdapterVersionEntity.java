/*
 * Purpose: Stores LoRA and QLoRA adapter artifact versions.
 * Why it exists: AI-4 produces adapter artifacts but must not merge or deploy them.
 * Architecture fit: Artifact version entity for fine-tuning adapters.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Adapter version entity. */
@Entity
@Table(name = "adapter_versions", schema = "finetuning")
public class AdapterVersionEntity {
    @Id private UUID id;
    private UUID runId;
    private String adapterType;
    private String adapterName;
    private Integer versionNumber;
    private String storageUri;
    private String checksum;
    private String status;
    private Instant createdAt;

    protected AdapterVersionEntity() {}

    /** Creates an adapter version record. */
    public AdapterVersionEntity(UUID id, UUID runId, String adapterType, String adapterName, Integer versionNumber, String storageUri, String checksum, String status, Instant createdAt) {
        this.id = id; this.runId = runId; this.adapterType = adapterType; this.adapterName = adapterName; this.versionNumber = versionNumber; this.storageUri = storageUri; this.checksum = checksum; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAdapterType() { return adapterType; }
    public String getAdapterName() { return adapterName; }
    public String getStatus() { return status; }
}

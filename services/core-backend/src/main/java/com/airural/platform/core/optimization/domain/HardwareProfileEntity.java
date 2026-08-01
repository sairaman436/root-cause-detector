/*
 * Purpose: Stores supported, minimum, and recommended hardware profiles.
 * Why it exists: Optimized artifacts need explicit cloud GPU, local GPU, CPU, edge, and air-gapped compatibility metadata.
 * Architecture fit: Reference entity for compatibility and release decisions.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Hardware profile entity. */
@Entity
@Table(name = "hardware_profiles", schema = "optimization")
public class HardwareProfileEntity {
    @Id private UUID id;
    private String hardwareKey;
    private String hardwareClass;
    private Integer minRamGb;
    private Integer minVramGb;
    private Integer minCpuCores;
    private String accelerator;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String notesJson;
    private Instant createdAt;

    protected HardwareProfileEntity() {}

    /** Creates a hardware profile. */
    public HardwareProfileEntity(UUID id, String hardwareKey, String hardwareClass, Integer minRamGb, Integer minVramGb, Integer minCpuCores, String accelerator, String status, String notesJson, Instant createdAt) {
        this.id = id; this.hardwareKey = hardwareKey; this.hardwareClass = hardwareClass; this.minRamGb = minRamGb; this.minVramGb = minVramGb; this.minCpuCores = minCpuCores; this.accelerator = accelerator; this.status = status; this.notesJson = notesJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
}

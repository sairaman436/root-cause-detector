/*
 * Purpose: JPA entity for versioned synthetic pilot evaluation datasets.
 * Why it exists: Evaluation datasets must be versioned, frozen, and durable to ensure regression reproducibility.
 * Architecture fit: Extends the evaluation bounded context without touching production data or model artefacts.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Versioned synthetic pilot evaluation dataset. All scenarios inside are labelled SYNTHETIC. */
@Entity
@Table(schema = "evaluation", name = "pilot_datasets")
public class PilotDatasetEntity {

    @Id
    private UUID id;

    @Column(name = "dataset_key", nullable = false, unique = true)
    private String datasetKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "domain_coverage", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String domainCoverageJson;

    @Column(name = "scenario_count", nullable = false)
    private int scenarioCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "frozen_at")
    private Instant frozenAt;

    protected PilotDatasetEntity() {}

    public PilotDatasetEntity(UUID id, String datasetKey, String name, String version, String description, String domainCoverageJson) {
        this.id = id;
        this.datasetKey = datasetKey;
        this.name = name;
        this.version = version;
        this.description = description;
        this.domainCoverageJson = domainCoverageJson;
        this.scenarioCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getDatasetKey() { return datasetKey; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public String getDomainCoverageJson() { return domainCoverageJson; }
    public int getScenarioCount() { return scenarioCount; }
    public void setScenarioCount(int scenarioCount) { this.scenarioCount = scenarioCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getFrozenAt() { return frozenAt; }
    public void setFrozenAt(Instant frozenAt) { this.frozenAt = frozenAt; }
}

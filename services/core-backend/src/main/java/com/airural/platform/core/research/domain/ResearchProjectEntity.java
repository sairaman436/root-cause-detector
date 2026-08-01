/*
 * Purpose: Stores Rural Intelligence Research Laboratory projects.
 * Why it exists: Research divisions and long-term programs require durable project ownership, charter, status, and governance metadata.
 * Architecture fit: Root aggregate for Research-1 research laboratory operations.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research project entity. */
@Entity
@Table(name = "research_projects", schema = "research_lab")
public class ResearchProjectEntity {
    @Id private UUID id;
    private String projectKey;
    private String title;
    private String division;
    private String program;
    private String objective;
    private String principalInvestigator;
    private String status;
    private String governanceState;
    private Instant createdAt;
    private Instant updatedAt;

    protected ResearchProjectEntity() {}

    /** Creates a research project. */
    public ResearchProjectEntity(UUID id, String projectKey, String title, String division, String program, String objective, String principalInvestigator, String status, String governanceState, Instant createdAt, Instant updatedAt) {
        this.id = id; this.projectKey = projectKey; this.title = title; this.division = division; this.program = program; this.objective = objective; this.principalInvestigator = principalInvestigator; this.status = status; this.governanceState = governanceState; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getProjectKey() { return projectKey; }
    public String getTitle() { return title; }
    public String getDivision() { return division; }
    public String getProgram() { return program; }
    public String getStatus() { return status; }
    public String getGovernanceState() { return governanceState; }
}

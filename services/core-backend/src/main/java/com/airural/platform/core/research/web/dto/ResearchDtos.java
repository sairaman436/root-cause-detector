/*
 * Purpose: Defines REST contracts for the Rural Intelligence Research Laboratory.
 * Why it exists: Research clients need stable project, experiment, publication, and finding payloads.
 * Architecture fit: DTO boundary for Research-1 laboratory APIs.
 */
package com.airural.platform.core.research.web.dto;

import jakarta.validation.constraints.*;
import java.util.*;

/** Container for research DTO records. */
public final class ResearchDtos {
    private ResearchDtos() {}

    /** Request to create a research project. */
    public record ResearchProjectRequest(@NotBlank String projectKey, @NotBlank String title, @NotBlank String division, @NotBlank String program, @NotBlank String objective, String principalInvestigator) {}

    /** Research project response. */
    public record ResearchProjectResponse(UUID id, String projectKey, String title, String division, String program, String status, String governanceState) {}

    /** Request to create a research experiment. */
    public record ResearchExperimentRequest(@NotBlank String projectKey, @NotBlank String experimentKey, @NotBlank String title, @NotBlank String hypothesis, @NotBlank String methodology, String benchmarkSuite) {}

    /** Research experiment response. */
    public record ResearchExperimentResponse(UUID id, UUID projectId, String experimentKey, String title, String approvalStatus, String replicationStatus) {}

    /** Publication response. */
    public record PublicationResponse(UUID id, String publicationType, String title, String reviewStatus) {}

    /** Finding response. */
    public record ResearchFindingResponse(UUID id, String title, String summary, Double confidence, String replicationStatus) {}
}

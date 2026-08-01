/*
 * Purpose: Defines REST contracts for the AI-10 release engineering platform.
 * Why it exists: Operators need stable latest, history, artifacts, model-card, promote, and rollback payloads.
 * Architecture fit: DTO boundary for enterprise AI model release APIs.
 */
package com.airural.platform.core.release.web.dto;

import jakarta.validation.constraints.*;
import java.util.*;

/** Container for release DTO records. */
public final class ReleaseDtos {
    private ReleaseDtos() {}

    /** Release version response. */
    public record ReleaseVersionResponse(UUID id, String modelName, String semanticVersion, String releaseChannel, String lifecycleStatus, Boolean lts) {}

    /** Release artifact response. */
    public record ReleaseArtifactResponse(UUID id, String artifactType, String packageFormat, String deploymentTarget, String checksumSha256, String status) {}

    /** Compatibility report response. */
    public record CompatibilityReportResponse(UUID id, String platform, String runtime, String hardwareProfile, String status) {}

    /** Model card response. */
    public record ModelCardResponse(String semanticVersion, String modelCardJson) {}

    /** Release history response. */
    public record ReleaseHistoryResponse(List<ReleaseVersionResponse> releases) {}

    /** Release artifact list response. */
    public record ReleaseArtifactsResponse(String semanticVersion, List<ReleaseArtifactResponse> artifacts, List<CompatibilityReportResponse> compatibility) {}

    /** Release promotion or rollback request. */
    public record ReleaseDecisionRequest(@NotBlank String semanticVersion, @NotBlank String rationale, String targetStatus) {}

    /** Release promotion or rollback response. */
    public record ReleaseDecisionResponse(UUID releaseVersionId, String semanticVersion, String decision, String lifecycleStatus, String eventHash) {}
}

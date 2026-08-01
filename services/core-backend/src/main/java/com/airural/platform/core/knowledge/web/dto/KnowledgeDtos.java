/*
 * Purpose: Defines public knowledge acquisition API contracts.
 * Why it exists: Knowledge engineers need stable REST payloads for source registration, crawling, coverage, and reindex operations.
 * Architecture fit: DTO boundary for the AI-2 Enterprise Knowledge Acquisition Platform.
 */
package com.airural.platform.core.knowledge.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** DTO namespace for knowledge acquisition APIs. */
public final class KnowledgeDtos {
    private KnowledgeDtos() {
    }

    /** Request to acquire a trusted source document. */
    public record KnowledgeAcquireRequest(
            @NotBlank String sourceKey,
            @NotBlank String name,
            @NotBlank String sourceType,
            String baseUrl,
            @NotBlank String documentTitle,
            @NotBlank String documentType,
            @NotBlank String content,
            Map<String, Object> metadata) {
    }

    /** Request to register or execute a crawler. */
    public record KnowledgeCrawlRequest(
            @NotBlank String sourceKey,
            @NotBlank String connectorType,
            String scheduleCron,
            String incrementalCursor) {
    }

    /** Request to reindex an approved knowledge dataset. */
    public record KnowledgeReindexRequest(@NotNull UUID datasetId, String reason) {
    }

    /** Knowledge source response. */
    public record SourceResponse(UUID id, String sourceKey, String name, String sourceType, String trustTier, String status) {
    }

    /** Knowledge acquisition job response. */
    public record JobResponse(UUID id, UUID sourceId, String jobType, String status, BigDecimal qualityScore) {
    }

    /** Knowledge dataset response. */
    public record DatasetResponse(UUID id, UUID sourceId, String name, String datasetType, String status, Integer versionNumber) {
    }

    /** Knowledge coverage response. */
    public record CoverageResponse(UUID datasetId, String coverageArea, BigDecimal coverageScore) {
    }

    /** Generic operation response for crawl, acquire, and reindex workflows. */
    public record OperationResponse(UUID id, String operation, String status, BigDecimal qualityScore, String details) {
    }
}

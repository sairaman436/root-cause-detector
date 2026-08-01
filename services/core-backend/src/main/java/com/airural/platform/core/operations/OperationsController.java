/*
 * Purpose: Exposes production operations metadata for deployment, health orchestration, and release verification.
 * Why it exists: Gives platform operators stable APIs for version, deployment status, and runtime metadata without adding business behavior.
 * Architecture fit: Implements Milestone 11 operational API requirements alongside Spring Boot Actuator readiness, liveness, and metrics endpoints.
 */
package com.airural.platform.core.operations;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for operational platform metadata. */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Operations", description = "Production readiness, deployment status, and version metadata")
public class OperationsController {

    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;
    private final String environment;
    private final String deploymentRegion;

    /** Creates the operations controller with optional build and git metadata. */
    public OperationsController(
            @Nullable BuildProperties buildProperties,
            @Nullable GitProperties gitProperties,
            @Value("${airural.platform.environment:local}") String environment,
            @Value("${airural.platform.region:local}") String deploymentRegion) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
        this.environment = environment;
        this.deploymentRegion = deploymentRegion;
    }

    /** Returns immutable version metadata for release verification and audit evidence. */
    @GetMapping("/version")
    @Operation(summary = "Get platform version metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> version(HttpServletRequest request) {
        Map<String, Object> payload = Map.of(
                "service", "core-backend",
                "version", valueOrDefault(buildProperties == null ? null : buildProperties.getVersion(), "0.1.0-SNAPSHOT"),
                "artifact", valueOrDefault(buildProperties == null ? null : buildProperties.getArtifact(), "core-backend"),
                "commit", valueOrDefault(gitProperties == null ? null : gitProperties.getShortCommitId(), "unknown"),
                "environment", environment,
                "region", deploymentRegion,
                "timestamp", Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.success(payload, RequestIds.from(request)));
    }

    /** Returns deployment status fields used by release gates, smoke tests, and operational dashboards. */
    @GetMapping("/deployment-status")
    @Operation(summary = "Get deployment status metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deploymentStatus(HttpServletRequest request) {
        Map<String, Object> payload = Map.of(
                "service", "core-backend",
                "status", "SERVING",
                "readinessEndpoint", "/actuator/health/readiness",
                "livenessEndpoint", "/actuator/health/liveness",
                "metricsEndpoint", "/actuator/prometheus",
                "environment", environment,
                "region", deploymentRegion,
                "timestamp", Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.success(payload, RequestIds.from(request)));
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

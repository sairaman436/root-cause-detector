/*
 * Purpose: Defines the authenticated image-analysis boundary and its validated observation response.
 * Why it exists: The browser must never call Ollama directly, and image observations must be distinguished from governed evidence.
 * Architecture fit: REST DTOs for the AI bounded context; downstream RAG receives observations only after this contract validates them.
 */
package com.airural.platform.core.ai.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/** DTO namespace for the local vision integration. */
public final class VisionDtos {
    private VisionDtos() {}

    /** Internal request sent from the backend to the AI inference service. */
    public record VisionInferenceRequest(
            @JsonProperty("image_base64") @NotBlank String imageBase64,
            @JsonProperty("mime_type") @NotBlank String mimeType,
            String question,
            String model) {}

    /** One fact returned by the vision model. */
    public record VisionObservationResponse(String description, String type) {}

    /** Validated observations returned to the authenticated portal. */
    public record VisionAnalysisResponse(
            String model,
            String provider,
            List<VisionObservationResponse> observations,
            String question,
            String uncertainty,
            @JsonProperty("latency_ms") long latencyMs,
            @JsonProperty("gpu_memory") Map<String, Object> gpuMemory) {}
}

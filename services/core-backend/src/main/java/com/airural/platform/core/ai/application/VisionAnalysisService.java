/*
 * Purpose: Validates image requests and proxies them to the configured local vision model.
 * Why it exists: Image bytes and provider credentials must remain server-side while the application enforces the observation contract.
 * Architecture fit: Application service between the authenticated REST adapter and provider-neutral AI inference service.
 */
package com.airural.platform.core.ai.application;

import com.airural.platform.core.ai.web.dto.VisionDtos.VisionAnalysisResponse;
import com.airural.platform.core.ai.web.dto.VisionDtos.VisionInferenceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/** Application service for non-persistent image observation requests. */
@Service
public class VisionAnalysisService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String inferenceServiceUrl;
    private final String visionModel;
    private final VisionImageUploadValidator imageValidator;

    public VisionAnalysisService(
            ObjectMapper objectMapper,
            @Value("${airural.ai.gateway.inference-service-url:http://localhost:8101}") String inferenceServiceUrl,
            @Value("${airural.ai.gateway.vision-model:moondream:1.8b}") String visionModel,
            @Value("${airural.ai.vision.max-image-bytes:10485760}") long maxImageBytes,
            @Value("${airural.ai.vision.max-width:4096}") int maxWidth,
            @Value("${airural.ai.vision.max-height:4096}") int maxHeight,
            @Value("${airural.ai.gateway.timeout-seconds:130}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.inferenceServiceUrl = inferenceServiceUrl.replaceAll("/$", "");
        this.visionModel = visionModel;
        this.imageValidator = new VisionImageUploadValidator(maxImageBytes, maxWidth, maxHeight);
        this.restTemplate = new org.springframework.boot.web.client.RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)))
                .build();
    }

    /** Validates the upload, performs one provider call, and validates the returned observation contract. */
    public VisionAnalysisResponse analyze(MultipartFile image, String question) {
        VisionImageUploadValidator.ValidatedImage validatedImage = imageValidator.validate(image);
        try {
            VisionInferenceRequest request = new VisionInferenceRequest(
                    Base64.getEncoder().encodeToString(validatedImage.bytes()),
                    validatedImage.mimeType(),
                    question == null ? "" : question.trim(),
                    visionModel);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    inferenceServiceUrl + "/v1/vision/analyze",
                    new HttpEntity<>(request, headers),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw unavailable("Vision analysis unavailable.");
            }
            VisionAnalysisResponse result = objectMapper.readValue(response.getBody(), VisionAnalysisResponse.class);
            if (result.observations() == null || result.observations().isEmpty()
                    || result.observations().stream().anyMatch(item -> item == null || !StringUtils.hasText(item.description()) || !StringUtils.hasText(item.type()))
                    || !StringUtils.hasText(result.uncertainty())) {
                throw invalidOutput();
            }
            return result;
        } catch (HttpStatusCodeException ex) {
            String body = ex.getResponseBodyAsString();
            if (ex.getStatusCode().value() == 502 && body.contains("VISION_INVALID_OUTPUT")) {
                throw invalidOutput();
            }
            if (ex.getStatusCode().is4xxClientError() && body.contains("VISION_IMAGE")) {
                throw new AiException("VISION_INVALID_IMAGE", "The image could not be accepted by the vision service.", HttpStatus.BAD_REQUEST);
            }
            throw unavailable("Vision analysis unavailable.");
        } catch (ResourceAccessException ex) {
            throw unavailable("Vision analysis unavailable.");
        } catch (RestClientException | java.io.IOException ex) {
            throw unavailable("Vision analysis unavailable.");
        }
    }

    private AiException unavailable(String message) {
        return new AiException("VISION_UNAVAILABLE", message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private AiException invalidOutput() {
        return new AiException("VISION_INVALID_OUTPUT", "Vision analysis could not be validated.", HttpStatus.BAD_GATEWAY);
    }
}

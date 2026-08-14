/*
 * Purpose: Exposes the authenticated image-analysis endpoint.
 * Why it exists: The web portal needs a controlled multipart boundary without direct access to Ollama.
 * Architecture fit: REST adapter for the AI bounded context; it returns observations, not evidence or hidden model reasoning.
 */
package com.airural.platform.core.ai.web;

import com.airural.platform.core.ai.application.VisionAnalysisService;
import com.airural.platform.core.ai.web.dto.VisionDtos.VisionAnalysisResponse;
import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Authenticated vision REST controller. */
@RestController
@RequestMapping("/api/v1/ai/vision")
public class VisionController {
    private final VisionAnalysisService service;

    public VisionController(VisionAnalysisService service) {
        this.service = service;
    }

    /** Analyzes an image without persisting the uploaded bytes. */
    @Operation(summary = "Analyze an image", description = "Returns strictly validated visible observations from the configured local vision model. Observations are not governed evidence.")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VisionAnalysisResponse>> analyze(
            @RequestPart("image") MultipartFile image,
            @RequestParam(required = false, defaultValue = "") String question,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        UUID ignoredUserId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.analyze(image, question), RequestIds.from(request)));
    }
}

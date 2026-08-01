/*
 * Purpose: Exposes governed dataset engineering APIs.
 * Why it exists: Dataset engineers, reviewers, and AI scientists need controlled endpoints for dataset lifecycle operations.
 * Architecture fit: REST adapter for AI-1 without implementing training or fine-tuning.
 */
package com.airural.platform.core.datasets.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.datasets.application.DatasetEngineeringService;
import com.airural.platform.core.datasets.web.dto.DatasetDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for enterprise dataset engineering workflows. */
@RestController
@RequestMapping("/api/v1/datasets")
public class DatasetController {
    private final DatasetEngineeringService service;

    public DatasetController(DatasetEngineeringService service) {
        this.service = service;
    }

    /** Creates a governed dataset registry record. */
    @Operation(summary = "Create dataset", description = "Creates a dataset registry entry and initial version.")
    @PostMapping
    public ResponseEntity<ApiResponse<DatasetResponse>> create(@Valid @RequestBody CreateDatasetRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID ownerId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.create(body, ownerId), RequestIds.from(request)));
    }

    /** Lists governed datasets. */
    @Operation(summary = "List datasets", description = "Lists dataset registry records for AI, RAG, and evaluation use.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DatasetResponse>>> list(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable), RequestIds.from(request)));
    }

    /** Cleans samples and masks PII. */
    @Operation(summary = "Clean dataset", description = "Runs deterministic cleaning, masking, deduplication, and language detection.")
    @PostMapping("/clean")
    public ResponseEntity<ApiResponse<DatasetOperationResponse>> clean(@Valid @RequestBody DatasetOperationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.clean(body), RequestIds.from(request)));
    }

    /** Validates dataset quality gates. */
    @Operation(summary = "Validate dataset", description = "Validates dataset quality, synthetic labeling, and release readiness.")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<DatasetOperationResponse>> validate(@Valid @RequestBody DatasetOperationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.validate(body), RequestIds.from(request)));
    }

    /** Registers a governed export artifact. */
    @Operation(summary = "Export dataset", description = "Returns a governed export artifact URI for approved downstream consumers.")
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<DatasetOperationResponse>> export(@Valid @RequestBody DatasetOperationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.export(body), RequestIds.from(request)));
    }

    /** Registers a synthetic dataset generation request. */
    @Operation(summary = "Create synthetic dataset metadata", description = "Registers synthetic generation metadata while enforcing human-review labeling.")
    @PostMapping("/synthetic")
    public ResponseEntity<ApiResponse<DatasetOperationResponse>> synthetic(@Valid @RequestBody DatasetOperationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.synthetic(body), RequestIds.from(request)));
    }

    /** Returns latest dataset quality. */
    @Operation(summary = "Get dataset quality", description = "Returns the latest dataset quality report.")
    @GetMapping("/quality")
    public ResponseEntity<ApiResponse<DatasetQualityResponse>> quality(@RequestParam UUID datasetId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.quality(datasetId), RequestIds.from(request)));
    }
}

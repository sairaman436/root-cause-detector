/*
 * Purpose: Exposes enterprise knowledge acquisition APIs.
 * Why it exists: Knowledge engineers need controlled REST operations for acquisition, crawling, dataset registry, coverage, and reindex requests.
 * Architecture fit: REST adapter for AI-2 while preserving platform API versioning and compatibility aliases.
 */
package com.airural.platform.core.knowledge.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.knowledge.application.KnowledgeAcquisitionService;
import com.airural.platform.core.knowledge.web.dto.KnowledgeDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for knowledge acquisition workflows. */
@RestController
@RequestMapping({"/api/v1/knowledge", "/knowledge"})
public class KnowledgeController {
    private final KnowledgeAcquisitionService service;

    public KnowledgeController(KnowledgeAcquisitionService service) {
        this.service = service;
    }

    /** Acquires a document from a trusted source. */
    @Operation(summary = "Acquire knowledge", description = "Registers source trust, fingerprints the document, extracts metadata, scores quality, and versions the dataset.")
    @PostMapping("/acquire")
    public ResponseEntity<ApiResponse<OperationResponse>> acquire(@Valid @RequestBody KnowledgeAcquireRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.acquire(body), RequestIds.from(request)));
    }

    /** Registers or executes a crawler discovery operation. */
    @Operation(summary = "Crawl knowledge source", description = "Runs connector discovery and records resumable crawler state.")
    @PostMapping("/crawl")
    public ResponseEntity<ApiResponse<OperationResponse>> crawl(@Valid @RequestBody KnowledgeCrawlRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.crawl(body), RequestIds.from(request)));
    }

    /** Lists trusted knowledge sources. */
    @Operation(summary = "List knowledge sources", description = "Lists registered trusted sources and trust tiers.")
    @GetMapping("/sources")
    public ResponseEntity<ApiResponse<Page<SourceResponse>>> sources(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.sources(pageable), RequestIds.from(request)));
    }

    /** Lists knowledge acquisition jobs. */
    @Operation(summary = "List knowledge jobs", description = "Lists acquisition, crawl, and reindex job records.")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> jobs(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.jobs(pageable), RequestIds.from(request)));
    }

    /** Lists knowledge datasets. */
    @Operation(summary = "List knowledge datasets", description = "Lists versioned knowledge corpora ready for downstream indexing and review.")
    @GetMapping("/datasets")
    public ResponseEntity<ApiResponse<Page<DatasetResponse>>> datasets(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.datasets(pageable), RequestIds.from(request)));
    }

    /** Lists coverage reports. */
    @Operation(summary = "List knowledge coverage", description = "Lists coverage and corpus gap quality records.")
    @GetMapping("/coverage")
    public ResponseEntity<ApiResponse<Page<CoverageResponse>>> coverage(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.coverage(pageable), RequestIds.from(request)));
    }

    /** Records a future-ready reindex request. */
    @Operation(summary = "Request knowledge reindex", description = "Records a reindex request for future search and RAG index workers.")
    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<OperationResponse>> reindex(@Valid @RequestBody KnowledgeReindexRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reindex(body), RequestIds.from(request)));
    }
}

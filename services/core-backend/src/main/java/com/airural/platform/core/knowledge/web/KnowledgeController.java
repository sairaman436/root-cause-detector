/*
 * Purpose: Exposes enterprise knowledge acquisition APIs.
 * Why it exists: Knowledge engineers need controlled REST operations for acquisition, crawling, dataset registry, coverage, and reindex requests.
 * Architecture fit: REST adapter for AI-2 while preserving platform API versioning and compatibility aliases.
 */
package com.airural.platform.core.knowledge.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.knowledge.application.KnowledgeAcquisitionService;
import com.airural.platform.core.knowledge.application.KnowledgeRagGatewayService;
import com.airural.platform.core.knowledge.web.dto.KnowledgeDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for knowledge acquisition workflows. */
@RestController
@RequestMapping({"/api/v1/knowledge", "/knowledge"})
public class KnowledgeController {
    private final KnowledgeAcquisitionService service;
    private final KnowledgeRagGatewayService ragGatewayService;

    public KnowledgeController(KnowledgeAcquisitionService service, KnowledgeRagGatewayService ragGatewayService) {
        this.service = service;
        this.ragGatewayService = ragGatewayService;
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
    public ResponseEntity<ApiResponse<Object>> reindex(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (body.containsKey("datasetId")) {
            KnowledgeReindexRequest command = new KnowledgeReindexRequest(UUID.fromString(String.valueOf(body.get("datasetId"))), String.valueOf(body.getOrDefault("reason", "manual_reindex")));
            return ResponseEntity.ok(ApiResponse.success(service.reindex(command), RequestIds.from(request)));
        }
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.reindexRag(body), RequestIds.from(request)));
    }

    /** Ingests a trusted text document into the RAG index. */
    @Operation(summary = "Ingest RAG document", description = "Uploads trusted document text with provenance metadata, chunks it, embeds it, indexes it, and records citation metadata.")
    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingestDocument(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.ingestJson(body), RequestIds.from(request)));
    }

    /** Ingests a trusted file into the RAG index. */
    @Operation(summary = "Upload RAG document file", description = "Uploads a PDF, DOCX, TXT, or Markdown document with approved-source provenance into the RAG pipeline.")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadDocument(@RequestPart("file") MultipartFile file, @RequestParam Map<String, String> metadata, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.ingestMultipart(file, metadata), RequestIds.from(request)));
    }

    /** Lists RAG-indexed knowledge documents. */
    @Operation(summary = "List RAG documents", description = "Lists trusted documents indexed for retrieval and citation.")
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ragDocuments(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.documents(), RequestIds.from(request)));
    }

    /** Gets a RAG-indexed knowledge document and chunk metadata. */
    @Operation(summary = "Get RAG document", description = "Returns one indexed knowledge document with citation-grade chunk metadata.")
    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ragDocument(@PathVariable String id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.document(id), RequestIds.from(request)));
    }

    /** Executes hybrid evidence search against the RAG index. */
    @Operation(summary = "Search trusted knowledge", description = "Executes hybrid vector, keyword, and metadata-filtered retrieval without generating an LLM answer.")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ragGatewayService.search(body), RequestIds.from(request)));
    }

}

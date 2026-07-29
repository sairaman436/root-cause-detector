/*
 * Purpose: Exposes evidence and asset management APIs.
 * Why it exists: Enterprise users need governed upload, metadata, search, download, delete, restore, version, and audit endpoints.
 * Architecture fit: REST adapter for the Evidence aggregate.
 */
package com.airural.platform.core.evidence.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.evidence.application.*;
import com.airural.platform.core.evidence.domain.EvidenceType;
import com.airural.platform.core.evidence.web.dto.EvidenceDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for evidence assets. */
@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceController {
    private final EvidenceManagementService evidenceManagementService;

    public EvidenceController(EvidenceManagementService evidenceManagementService) {
        this.evidenceManagementService = evidenceManagementService;
    }

    /** Uploads an evidence asset. */
    @Operation(summary = "Upload evidence", description = "Uploads a governed evidence asset with metadata and tags.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EvidenceResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam UUID organizationId,
            @RequestParam(required = false) UUID surveyId,
            @RequestParam(required = false) UUID questionId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String customMetadataJson,
            @RequestParam(required = false) Set<String> tags,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                evidenceManagementService.upload(
                        file, organizationId, surveyId, questionId, title, description, customMetadataJson, tags, user.userId()),
                RequestIds.from(request)));
    }

    /** Searches evidence metadata. */
    @Operation(summary = "Search evidence", description = "Searches evidence by survey, uploader, tags, organization, date, and file type.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EvidenceResponse>>> search(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID surveyId,
            @RequestParam(required = false) UUID uploaderId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant uploadedFrom,
            @RequestParam(required = false) EvidenceType evidenceType,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            Pageable pageable,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                evidenceManagementService.search(organizationId, surveyId, uploaderId, tag, uploadedFrom, evidenceType, includeDeleted, pageable),
                RequestIds.from(request)));
    }

    /** Gets evidence metadata. */
    @Operation(summary = "Get evidence", description = "Gets evidence metadata by ID.")
    @GetMapping("/{evidenceId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> get(
            @PathVariable("evidenceId") UUID evidenceId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evidenceManagementService.get(evidenceId, includeDeleted), RequestIds.from(request)));
    }

    /** Updates evidence metadata. */
    @Operation(summary = "Update evidence metadata", description = "Updates editable metadata and creates a new evidence version.")
    @PutMapping("/{evidenceId}/metadata")
    public ResponseEntity<ApiResponse<EvidenceResponse>> updateMetadata(
            @PathVariable("evidenceId") UUID evidenceId,
            @Valid @RequestBody UpdateEvidenceMetadataRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                evidenceManagementService.updateMetadata(evidenceId, body, user.userId()), RequestIds.from(request)));
    }

    /** Soft-deletes evidence. */
    @Operation(summary = "Soft delete evidence", description = "Soft-deletes evidence metadata while retaining governed storage.")
    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> delete(
            @PathVariable("evidenceId") UUID evidenceId,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evidenceManagementService.softDelete(evidenceId, user.userId()), RequestIds.from(request)));
    }

    /** Restores soft-deleted evidence. */
    @Operation(summary = "Restore evidence", description = "Restores a previously soft-deleted evidence asset.")
    @PostMapping("/{evidenceId}/restore")
    public ResponseEntity<ApiResponse<EvidenceResponse>> restore(
            @PathVariable("evidenceId") UUID evidenceId,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evidenceManagementService.restore(evidenceId, user.userId()), RequestIds.from(request)));
    }

    /** Downloads an evidence binary. */
    @Operation(summary = "Download evidence", description = "Downloads an active evidence binary and audits access.")
    @GetMapping("/{evidenceId}/download")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable("evidenceId") UUID evidenceId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        EvidenceBinary binary = evidenceManagementService.download(evidenceId, user.userId());
        String encodedName = URLEncoder.encode(binary.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(binary.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(binary.content().length)
                .body(new ByteArrayResource(binary.content()));
    }

    /** Requests a future-ready provider signed URL. */
    @Operation(summary = "Create signed URL", description = "Returns a provider signed URL when the active storage adapter supports it.")
    @PostMapping("/{evidenceId}/signed-url")
    public ResponseEntity<ApiResponse<SignedUrlResponse>> signedUrl(
            @PathVariable("evidenceId") UUID evidenceId,
            @RequestParam(defaultValue = "15") long ttlMinutes,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                evidenceManagementService.signedUrl(evidenceId, user.userId(), Duration.ofMinutes(ttlMinutes)),
                RequestIds.from(request)));
    }

    /** Lists evidence version history. */
    @Operation(summary = "List evidence versions", description = "Lists immutable evidence version snapshots.")
    @GetMapping("/{evidenceId}/versions")
    public ResponseEntity<ApiResponse<List<EvidenceVersionResponse>>> versions(
            @PathVariable("evidenceId") UUID evidenceId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evidenceManagementService.versions(evidenceId), RequestIds.from(request)));
    }

    /** Lists evidence audit history. */
    @Operation(summary = "List evidence audit", description = "Lists evidence module audit events.")
    @GetMapping("/{evidenceId}/audit")
    public ResponseEntity<ApiResponse<List<EvidenceAuditResponse>>> audit(
            @PathVariable("evidenceId") UUID evidenceId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evidenceManagementService.auditHistory(evidenceId), RequestIds.from(request)));
    }
}

/*
 * Purpose: Exposes survey template library APIs.
 * Why it exists: Administrators need governed reusable templates before creating surveys.
 * Architecture fit: REST adapter for the survey template boundary.
 */
package com.airural.platform.core.survey.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.survey.application.SurveyManagementService;
import com.airural.platform.core.survey.web.dto.SurveyDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for survey templates. */
@RestController
@RequestMapping("/api/v1/survey-templates")
public class SurveyTemplateController {
    private final SurveyManagementService surveyManagementService;

    public SurveyTemplateController(SurveyManagementService surveyManagementService) {
        this.surveyManagementService = surveyManagementService;
    }

    /** Creates a survey template. */
    @Operation(summary = "Create survey template", description = "Creates a reusable survey template.")
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> create(
            @Valid @RequestBody CreateTemplateRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.createTemplate(body, user.userId()), RequestIds.from(request)));
    }

    /** Updates a survey template. */
    @Operation(summary = "Update survey template", description = "Updates a reusable survey template.")
    @PutMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateResponse>> update(
            @PathVariable("templateId") UUID templateId,
            @Valid @RequestBody UpdateTemplateRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.updateTemplate(templateId, body, user.userId()), RequestIds.from(request)));
    }

    /** Lists survey templates. */
    @Operation(summary = "List survey templates", description = "Lists reusable survey templates.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TemplateResponse>>> list(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listTemplates(pageable), RequestIds.from(request)));
    }
}

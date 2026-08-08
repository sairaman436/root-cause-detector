/*
 * Purpose: Exposes survey management APIs.
 * Why it exists: Enterprise users need survey CRUD, search, workflow, versioning, questionnaire, and assignment endpoints.
 * Architecture fit: REST adapter for the survey aggregate.
 */
package com.airural.platform.core.survey.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.survey.application.SurveyManagementService;
import com.airural.platform.core.survey.domain.SurveyStatus;
import com.airural.platform.core.survey.web.dto.SurveyDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for surveys. */
@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {
    private final SurveyManagementService surveyManagementService;

    public SurveyController(SurveyManagementService surveyManagementService) {
        this.surveyManagementService = surveyManagementService;
    }

    /** Creates a survey. */
    @Operation(summary = "Create survey", description = "Creates a draft survey.")
    @PostMapping
    public ResponseEntity<ApiResponse<SurveyResponse>> create(
            @Valid @RequestBody CreateSurveyRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.createSurvey(body, user.userId()), RequestIds.from(request)));
    }

    /** Updates a survey. */
    @Operation(summary = "Update survey", description = "Updates editable survey metadata and creates a new version.")
    @PutMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<SurveyResponse>> update(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody UpdateSurveyRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.updateSurvey(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Searches surveys. */
    @Operation(summary = "Search surveys", description = "Searches surveys by name, status, tag, organization, creator, and updated date.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SurveyResponse>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SurveyStatus status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID createdByUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedFrom,
            Pageable pageable,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.searchSurveys(name, status, tag, organizationId, createdByUserId, updatedFrom, pageable),
                RequestIds.from(request)));
    }

    /** Gets a survey. */
    @Operation(summary = "Get survey", description = "Gets a survey by ID.")
    @GetMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<SurveyResponse>> get(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.getSurvey(surveyId), RequestIds.from(request)));
    }

    /** Clones a survey. */
    @Operation(summary = "Clone survey", description = "Clones a survey into a new draft survey.")
    @PostMapping("/{surveyId}/clone")
    public ResponseEntity<ApiResponse<SurveyResponse>> clone(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody CloneSurveyRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.cloneSurvey(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Transitions a survey through the approved workflow. */
    @Operation(summary = "Transition survey", description = "Moves a survey through a validated lifecycle transition.")
    @PostMapping("/{surveyId}/workflow")
    public ResponseEntity<ApiResponse<SurveyResponse>> transition(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody TransitionSurveyRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.transitionSurvey(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Archives a survey. */
    @Operation(summary = "Archive survey", description = "Archives a survey through the workflow validator.")
    @PostMapping("/{surveyId}/archive")
    public ResponseEntity<ApiResponse<SurveyResponse>> archive(
            @PathVariable("surveyId") UUID surveyId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.archiveSurvey(surveyId, reason, user.userId()), RequestIds.from(request)));
    }

    /** Soft-deletes a survey. */
    @Operation(summary = "Soft delete survey", description = "Soft-deletes a survey through the workflow validator.")
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<SurveyResponse>> delete(
            @PathVariable("surveyId") UUID surveyId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.deleteSurvey(surveyId, reason, user.userId()), RequestIds.from(request)));
    }

    /** Creates a survey section. */
    @Operation(summary = "Create survey section", description = "Creates an ordered survey section.")
    @PostMapping("/{surveyId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody CreateSectionRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.createSection(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Lists survey sections. */
    @Operation(summary = "List survey sections", description = "Lists sections for a survey.")
    @GetMapping("/{surveyId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> sections(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listSections(surveyId), RequestIds.from(request)));
    }

    /** Creates a survey question. */
    @Operation(summary = "Create survey question", description = "Creates a dynamic survey question with options and validation rules.")
    @PostMapping("/{surveyId}/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody CreateQuestionRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.createQuestion(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Lists survey questions. */
    @Operation(summary = "List survey questions", description = "Lists questions for a survey.")
    @GetMapping("/{surveyId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> questions(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listQuestions(surveyId), RequestIds.from(request)));
    }

    /** Creates a validation rule. */
    @Operation(summary = "Create validation rule", description = "Creates a survey-level or question-level validation rule.")
    @PostMapping("/{surveyId}/validation-rules")
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> createRule(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody CreateValidationRuleRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.createValidationRule(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Creates a survey assignment. */
    @Operation(summary = "Assign survey", description = "Assigns a survey to an organization, team, user, or geographic region.")
    @PostMapping("/{surveyId}/assignments")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody CreateAssignmentRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.assignSurvey(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Lists survey assignments. */
    @Operation(summary = "List survey assignments", description = "Lists assignments for a survey.")
    @GetMapping("/{surveyId}/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> assignments(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listAssignments(surveyId), RequestIds.from(request)));
    }

    /** Lists survey versions. */
    @Operation(summary = "List survey versions", description = "Lists survey version history.")
    @GetMapping("/{surveyId}/versions")
    public ResponseEntity<ApiResponse<List<VersionResponse>>> versions(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listVersions(surveyId), RequestIds.from(request)));
    }

    /** Lists survey workflow history. */
    @Operation(summary = "List survey status history", description = "Lists survey workflow transitions.")
    @GetMapping("/{surveyId}/status-history")
    public ResponseEntity<ApiResponse<List<StatusHistoryResponse>>> history(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listStatusHistory(surveyId), RequestIds.from(request)));
    }

    /** Submits survey answers. */
    @Operation(summary = "Submit survey", description = "Persists answers for a published or active survey.")
    @PostMapping("/{surveyId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(
            @PathVariable("surveyId") UUID surveyId,
            @Valid @RequestBody SubmitSurveyRequest body,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                surveyManagementService.submitSurvey(surveyId, body, user.userId()), RequestIds.from(request)));
    }

    /** Lists survey submissions. */
    @Operation(summary = "List survey submissions", description = "Lists submitted responses for a survey.")
    @GetMapping("/{surveyId}/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> submissions(@PathVariable("surveyId") UUID surveyId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.listSubmissions(surveyId), RequestIds.from(request)));
    }

    /** Gets a survey submission. */
    @Operation(summary = "Get survey submission", description = "Gets a submitted response for a survey.")
    @GetMapping("/{surveyId}/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submission(
            @PathVariable("surveyId") UUID surveyId,
            @PathVariable("submissionId") UUID submissionId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.getSubmission(surveyId, submissionId), RequestIds.from(request)));
    }
}

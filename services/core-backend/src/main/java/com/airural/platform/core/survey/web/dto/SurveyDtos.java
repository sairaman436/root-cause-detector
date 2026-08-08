/*
 * Purpose: Defines REST contracts for the Enterprise Survey Management APIs.
 * Why it exists: Controllers need stable request and response DTOs separate from persistence entities.
 * Architecture fit: Web adapter contracts for Milestone 3 survey management.
 */
package com.airural.platform.core.survey.web.dto;

import com.airural.platform.core.survey.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Namespace for survey API DTO records. */
public final class SurveyDtos {
    private SurveyDtos() {
    }

    /** Request to create a survey template. */
    public record CreateTemplateRequest(
            @NotBlank @Size(max = 180) String name,
            @Size(max = 1000) String description,
            @NotNull SurveyTemplateCategory category,
            SurveyTemplateStatus status,
            String metadataJson) {
    }

    /** Request to update a survey template. */
    public record UpdateTemplateRequest(
            @NotBlank @Size(max = 180) String name,
            @Size(max = 1000) String description,
            @NotNull SurveyTemplateCategory category,
            @NotNull SurveyTemplateStatus status,
            String metadataJson) {
    }

    /** Survey template response. */
    public record TemplateResponse(
            UUID id,
            String name,
            String description,
            SurveyTemplateCategory category,
            SurveyTemplateStatus status,
            String metadataJson,
            UUID createdByUserId,
            Instant createdAt,
            Instant updatedAt) {
    }

    /** Request to create a survey. */
    public record CreateSurveyRequest(
            UUID templateId,
            @NotNull UUID organizationId,
            @NotBlank @Size(max = 180) String name,
            @Size(max = 1000) String description,
            Set<@Size(max = 80) String> tags) {
    }

    /** Request to update a survey. */
    public record UpdateSurveyRequest(
            @NotBlank @Size(max = 180) String name,
            @Size(max = 1000) String description,
            Set<@Size(max = 80) String> tags) {
    }

    /** Request to clone a survey. */
    public record CloneSurveyRequest(
            @NotNull UUID organizationId,
            @NotBlank @Size(max = 180) String name,
            @Size(max = 1000) String description,
            Set<@Size(max = 80) String> tags) {
    }

    /** Request to transition a survey. */
    public record TransitionSurveyRequest(@NotNull SurveyStatus status, @Size(max = 1000) String reason) {
    }

    /** Survey response. */
    public record SurveyResponse(
            UUID id,
            UUID templateId,
            UUID organizationId,
            UUID createdByUserId,
            String name,
            String description,
            SurveyStatus status,
            Integer currentVersion,
            UUID clonedFromSurveyId,
            Set<String> tags,
            Instant archivedAt,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt) {
    }

    /** Request to create a section. */
    public record CreateSectionRequest(
            UUID parentSectionId,
            @NotBlank @Size(max = 100) String code,
            @NotBlank @Size(max = 220) String title,
            @Size(max = 1000) String description,
            @NotNull @Min(0) Integer orderIndex,
            boolean repeatable,
            String conditionExpression) {
    }

    /** Section response. */
    public record SectionResponse(
            UUID id,
            UUID surveyId,
            UUID parentSectionId,
            String code,
            String title,
            String description,
            Integer orderIndex,
            boolean repeatable,
            String conditionExpression) {
    }

    /** Request to create a question. */
    public record CreateQuestionRequest(
            @NotNull UUID sectionId,
            UUID parentQuestionId,
            @NotBlank @Size(max = 120) String code,
            @NotBlank @Size(max = 500) String prompt,
            @Size(max = 1000) String helpText,
            @NotBlank @Size(max = 80) String questionType,
            @NotNull @Min(0) Integer orderIndex,
            boolean required,
            String defaultValue,
            String conditionExpression,
            String calculationExpression,
            String metadataJson,
            List<@Valid CreateQuestionOptionRequest> options,
            List<@Valid CreateValidationRuleRequest> validationRules) {
    }

    /** Request to create a question option. */
    public record CreateQuestionOptionRequest(
            @NotBlank @Size(max = 220) String value,
            @NotBlank @Size(max = 220) String label,
            @NotNull @Min(0) Integer orderIndex,
            String metadataJson) {
    }

    /** Question response. */
    public record QuestionResponse(
            UUID id,
            UUID surveyId,
            UUID sectionId,
            UUID parentQuestionId,
            String code,
            String prompt,
            String helpText,
            String questionType,
            Integer orderIndex,
            boolean required,
            String defaultValue,
            String conditionExpression,
            String calculationExpression,
            String metadataJson,
            List<QuestionOptionResponse> options,
            List<ValidationRuleResponse> validationRules) {
    }

    /** Question option response. */
    public record QuestionOptionResponse(UUID id, UUID questionId, String value, String label, Integer orderIndex, String metadataJson) {
    }

    /** Request to create a validation rule. */
    public record CreateValidationRuleRequest(
            UUID questionId,
            @NotNull ValidationRuleType ruleType,
            String expression,
            @NotBlank @Size(max = 500) String message,
            String paramsJson,
            @NotNull @Min(0) Integer orderIndex) {
    }

    /** Validation rule response. */
    public record ValidationRuleResponse(
            UUID id,
            UUID surveyId,
            UUID questionId,
            ValidationRuleType ruleType,
            String expression,
            String message,
            String paramsJson,
            Integer orderIndex) {
    }

    /** Request to assign a survey. */
    public record CreateAssignmentRequest(
            @NotNull AssignmentType assignmentType,
            @NotBlank @Size(max = 160) String targetId,
            @Size(max = 220) String targetName,
            Instant dueAt) {
    }

    /** Assignment response. */
    public record AssignmentResponse(
            UUID id,
            UUID surveyId,
            AssignmentType assignmentType,
            String targetId,
            String targetName,
            UUID assignedByUserId,
            Instant assignedAt,
            Instant dueAt) {
    }

    /** Survey version response. */
    public record VersionResponse(UUID id, UUID surveyId, Integer versionNumber, String name, String description, String snapshotJson, UUID createdByUserId, Instant createdAt) {
    }

    /** Status history response. */
    public record StatusHistoryResponse(UUID id, UUID surveyId, SurveyStatus fromStatus, SurveyStatus toStatus, UUID changedByUserId, String reason, Instant createdAt) {
    }

    /** Request to submit survey answers. */
    public record SubmitSurveyRequest(@NotEmpty List<@Valid SubmitAnswerRequest> answers) {
    }

    /** Submitted answer request. */
    public record SubmitAnswerRequest(@NotNull UUID questionId, @NotBlank String value) {
    }

    /** Survey submission response. */
    public record SubmissionResponse(
            UUID id,
            UUID surveyId,
            UUID organizationId,
            UUID submittedByUserId,
            String status,
            Instant submittedAt,
            List<SubmittedAnswerResponse> answers) {
    }

    /** Submitted answer response. */
    public record SubmittedAnswerResponse(UUID id, UUID questionId, String questionCode, String value, Instant createdAt) {
    }
}

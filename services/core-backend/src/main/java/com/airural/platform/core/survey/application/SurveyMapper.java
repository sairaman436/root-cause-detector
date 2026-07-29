/*
 * Purpose: Maps survey domain entities to REST DTOs.
 * Why it exists: Web contracts must remain decoupled from JPA persistence entities.
 * Architecture fit: Application mapper for the survey module.
 */
package com.airural.platform.core.survey.application;

import com.airural.platform.core.survey.domain.*;
import com.airural.platform.core.survey.web.dto.SurveyDtos.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Mapper for survey DTO responses. */
@Component
public class SurveyMapper {
    /** Maps a template entity. */
    public TemplateResponse template(SurveyTemplateEntity template) {
        return new TemplateResponse(
                template.id(),
                template.name(),
                template.description(),
                template.category(),
                template.status(),
                template.metadataJson(),
                template.createdByUserId(),
                template.createdAt(),
                template.updatedAt());
    }

    /** Maps a survey entity. */
    public SurveyResponse survey(SurveyEntity survey) {
        Set<String> tags = survey.tags().stream().map(SurveyTagEntity::name).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return new SurveyResponse(
                survey.id(),
                survey.template() == null ? null : survey.template().id(),
                survey.organizationId(),
                survey.createdByUserId(),
                survey.name(),
                survey.description(),
                survey.status(),
                survey.currentVersion(),
                survey.clonedFromSurveyId(),
                tags,
                survey.archivedAt(),
                survey.deletedAt(),
                survey.createdAt(),
                survey.updatedAt());
    }

    /** Maps a section entity. */
    public SectionResponse section(SurveySectionEntity section) {
        return new SectionResponse(
                section.id(),
                section.surveyId(),
                section.parentSectionId(),
                section.code(),
                section.title(),
                section.description(),
                section.orderIndex(),
                section.repeatable(),
                section.conditionExpression());
    }

    /** Maps a question entity and its children. */
    public QuestionResponse question(SurveyQuestionEntity question, List<QuestionOptionEntity> options, List<ValidationRuleEntity> rules) {
        return new QuestionResponse(
                question.id(),
                question.surveyId(),
                question.sectionId(),
                question.parentQuestionId(),
                question.code(),
                question.prompt(),
                question.helpText(),
                question.questionType(),
                question.orderIndex(),
                question.required(),
                question.defaultValue(),
                question.conditionExpression(),
                question.calculationExpression(),
                question.metadataJson(),
                options.stream().map(this::option).toList(),
                rules.stream().map(this::rule).toList());
    }

    /** Maps a question option. */
    public QuestionOptionResponse option(QuestionOptionEntity option) {
        return new QuestionOptionResponse(option.id(), option.questionId(), option.value(), option.label(), option.orderIndex(), option.metadataJson());
    }

    /** Maps a validation rule. */
    public ValidationRuleResponse rule(ValidationRuleEntity rule) {
        return new ValidationRuleResponse(rule.id(), rule.surveyId(), rule.questionId(), rule.ruleType(), rule.expression(), rule.message(), rule.paramsJson(), rule.orderIndex());
    }

    /** Maps an assignment. */
    public AssignmentResponse assignment(SurveyAssignmentEntity assignment) {
        return new AssignmentResponse(
                assignment.id(),
                assignment.surveyId(),
                assignment.assignmentType(),
                assignment.targetId(),
                assignment.targetName(),
                assignment.assignedByUserId(),
                assignment.assignedAt(),
                assignment.dueAt());
    }

    /** Maps a version. */
    public VersionResponse version(SurveyVersionEntity version) {
        return new VersionResponse(version.id(), version.surveyId(), version.versionNumber(), version.name(), version.description(), version.snapshotJson(), version.createdByUserId(), version.createdAt());
    }

    /** Maps status history. */
    public StatusHistoryResponse history(SurveyStatusHistoryEntity history) {
        return new StatusHistoryResponse(history.id(), history.surveyId(), history.fromStatus(), history.toStatus(), history.changedByUserId(), history.reason(), history.createdAt());
    }
}

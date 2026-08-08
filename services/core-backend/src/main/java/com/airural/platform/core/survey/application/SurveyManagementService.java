/*
 * Purpose: Implements enterprise survey management use cases.
 * Why it exists: Controllers need a transactional application boundary for survey CRUD, workflow, templates, questionnaire structure, assignments, search, and versioning.
 * Architecture fit: Application service for Milestone 3 Enterprise Survey Management.
 */
package com.airural.platform.core.survey.application;

import static com.airural.platform.core.survey.infrastructure.SurveySpecifications.*;

import com.airural.platform.core.events.application.OutboxService;
import com.airural.platform.core.identity.application.AuditService;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.identity.infrastructure.OrganizationRepository;
import com.airural.platform.core.identity.infrastructure.UserAccountRepository;
import com.airural.platform.core.survey.domain.*;
import com.airural.platform.core.survey.infrastructure.*;
import com.airural.platform.core.survey.web.dto.SurveyDtos.*;
import com.airural.platform.shared.events.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional application service for survey management. */
@Service
public class SurveyManagementService {
    private final SurveyRepository surveyRepository;
    private final SurveyTemplateRepository templateRepository;
    private final SurveySectionRepository sectionRepository;
    private final SurveyQuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final ValidationRuleRepository ruleRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final SurveySubmissionRepository submissionRepository;
    private final SurveyVersionRepository versionRepository;
    private final SurveyStatusHistoryRepository historyRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userRepository;
    private final SurveyMapper mapper;
    private final QuestionTypeRegistry questionTypeRegistry;
    private final SurveyValidationService validationService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    public SurveyManagementService(
            SurveyRepository surveyRepository,
            SurveyTemplateRepository templateRepository,
            SurveySectionRepository sectionRepository,
            SurveyQuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            ValidationRuleRepository ruleRepository,
            SurveyAssignmentRepository assignmentRepository,
            SurveySubmissionRepository submissionRepository,
            SurveyVersionRepository versionRepository,
            SurveyStatusHistoryRepository historyRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userRepository,
            SurveyMapper mapper,
            QuestionTypeRegistry questionTypeRegistry,
            SurveyValidationService validationService,
            AuditService auditService,
            ObjectMapper objectMapper,
            OutboxService outboxService) {
        this.surveyRepository = surveyRepository;
        this.templateRepository = templateRepository;
        this.sectionRepository = sectionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.ruleRepository = ruleRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.versionRepository = versionRepository;
        this.historyRepository = historyRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.questionTypeRegistry = questionTypeRegistry;
        this.validationService = validationService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    /** Creates a reusable survey template. */
    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request, UUID actorUserId) {
        ensureUser(actorUserId);
        SurveyTemplateStatus status = request.status() == null ? SurveyTemplateStatus.DRAFT : request.status();
        if (templateRepository.existsByNameIgnoreCaseAndCategory(request.name(), request.category())) {
            throw new SurveyException("SURVEY_TEMPLATE_DUPLICATE", "Template name already exists in category", HttpStatus.CONFLICT);
        }
        SurveyTemplateEntity template = templateRepository.save(new SurveyTemplateEntity(
                request.name(), request.description(), request.category(), status, request.metadataJson(), actorUserId));
        audit(actorUserId, "SURVEY_TEMPLATE_CREATED", template.id().toString());
        return mapper.template(template);
    }

    /** Updates a reusable survey template. */
    @Transactional
    public TemplateResponse updateTemplate(UUID templateId, UpdateTemplateRequest request, UUID actorUserId) {
        SurveyTemplateEntity template = template(templateId);
        template.update(request.name(), request.description(), request.category(), request.status(), request.metadataJson());
        audit(actorUserId, "SURVEY_TEMPLATE_UPDATED", template.id().toString());
        return mapper.template(template);
    }

    /** Lists templates. */
    @Transactional(readOnly = true)
    public Page<TemplateResponse> listTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable).map(mapper::template);
    }

    /** Creates a survey in draft state. */
    @Transactional
    public SurveyResponse createSurvey(CreateSurveyRequest request, UUID actorUserId) {
        ensureUser(actorUserId);
        ensureOrganization(request.organizationId());
        SurveyTemplateEntity template = request.templateId() == null ? null : template(request.templateId());
        SurveyEntity survey = surveyRepository.save(new SurveyEntity(
                template, request.organizationId(), actorUserId, request.name(), request.description(), request.tags()));
        createVersion(survey, actorUserId);
        audit(actorUserId, "SURVEY_CREATED", survey.id().toString());
        publishSurvey(EventTopic.SURVEY_CREATED, survey, actorUserId);
        return mapper.survey(survey);
    }

    /** Updates editable survey metadata and creates a new version. */
    @Transactional
    public SurveyResponse updateSurvey(UUID surveyId, UpdateSurveyRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        try {
            survey.update(request.name(), request.description(), request.tags());
        } catch (IllegalStateException ex) {
            throw new SurveyException("SURVEY_NOT_EDITABLE", ex.getMessage(), HttpStatus.CONFLICT);
        }
        createVersion(survey, actorUserId);
        audit(actorUserId, "SURVEY_UPDATED", survey.id().toString());
        publishSurvey(EventTopic.SURVEY_UPDATED, survey, actorUserId);
        return mapper.survey(survey);
    }

    /** Clones a survey definition into a new draft survey. */
    @Transactional
    public SurveyResponse cloneSurvey(UUID surveyId, CloneSurveyRequest request, UUID actorUserId) {
        SurveyEntity source = survey(surveyId);
        ensureOrganization(request.organizationId());
        SurveyEntity clone = surveyRepository.save(new SurveyEntity(
                source.template(), request.organizationId(), actorUserId, request.name(), request.description(), request.tags()));
        clone.markClonedFrom(source.id());
        createVersion(clone, actorUserId);
        audit(actorUserId, "SURVEY_CLONED", source.id() + " -> " + clone.id());
        return mapper.survey(clone);
    }

    /** Moves a survey through a validated workflow transition. */
    @Transactional
    public SurveyResponse transitionSurvey(UUID surveyId, TransitionSurveyRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        try {
            survey.transitionTo(request.status(), actorUserId, request.reason());
        } catch (IllegalStateException ex) {
            throw new SurveyException("SURVEY_TRANSITION_INVALID", ex.getMessage(), HttpStatus.CONFLICT);
        }
        audit(actorUserId, "SURVEY_STATUS_CHANGED", survey.id() + " -> " + request.status());
        if (request.status() == SurveyStatus.COMPLETED) {
            publishSurvey(EventTopic.SURVEY_COMPLETED, survey, actorUserId);
        }
        return mapper.survey(survey);
    }

    /** Archives a survey through the workflow validator. */
    @Transactional
    public SurveyResponse archiveSurvey(UUID surveyId, String reason, UUID actorUserId) {
        return transitionSurvey(surveyId, new TransitionSurveyRequest(SurveyStatus.ARCHIVED, reason), actorUserId);
    }

    /** Soft-deletes a survey through the workflow validator. */
    @Transactional
    public SurveyResponse deleteSurvey(UUID surveyId, String reason, UUID actorUserId) {
        return transitionSurvey(surveyId, new TransitionSurveyRequest(SurveyStatus.DELETED, reason), actorUserId);
    }

    /** Searches surveys by supported filters. */
    @Transactional(readOnly = true)
    public Page<SurveyResponse> searchSurveys(
            String name,
            SurveyStatus status,
            String tag,
            UUID organizationId,
            UUID createdByUserId,
            Instant updatedFrom,
            Pageable pageable) {
        Specification<SurveyEntity> spec = Specification.where(activeOnly())
                .and(nameContains(name))
                .and(statusEquals(status))
                .and(hasTag(tag))
                .and(organizationEquals(organizationId))
                .and(createdByEquals(createdByUserId))
                .and(updatedAfter(updatedFrom));
        return surveyRepository.findAll(spec, pageable).map(mapper::survey);
    }

    /** Returns a survey by ID. */
    @Transactional(readOnly = true)
    public SurveyResponse getSurvey(UUID surveyId) {
        return mapper.survey(survey(surveyId));
    }

    /** Creates a section in a survey. */
    @Transactional
    public SectionResponse createSection(UUID surveyId, CreateSectionRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        ensureEditableDefinition(survey);
        SurveySectionEntity parent = request.parentSectionId() == null ? null : section(request.parentSectionId());
        if (parent != null && !parent.surveyId().equals(survey.id())) {
            throw new SurveyException("SECTION_PARENT_INVALID", "Parent section belongs to a different survey", HttpStatus.BAD_REQUEST);
        }
        SurveySectionEntity section = sectionRepository.save(new SurveySectionEntity(
                survey, parent, request.code(), request.title(), request.description(), request.orderIndex(), request.repeatable(), request.conditionExpression()));
        audit(actorUserId, "SURVEY_SECTION_CREATED", section.id().toString());
        return mapper.section(section);
    }

    /** Lists sections for a survey. */
    @Transactional(readOnly = true)
    public List<SectionResponse> listSections(UUID surveyId) {
        return sectionRepository.findBySurvey_IdOrderByOrderIndexAsc(surveyId).stream().map(mapper::section).toList();
    }

    /** Creates a question with options and validation rules. */
    @Transactional
    public QuestionResponse createQuestion(UUID surveyId, CreateQuestionRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        ensureEditableDefinition(survey);
        SurveySectionEntity section = section(request.sectionId());
        if (!section.surveyId().equals(survey.id())) {
            throw new SurveyException("QUESTION_SECTION_INVALID", "Section belongs to a different survey", HttpStatus.BAD_REQUEST);
        }
        SurveyQuestionEntity parent = request.parentQuestionId() == null ? null : question(request.parentQuestionId());
        if (parent != null && !parent.surveyId().equals(survey.id())) {
            throw new SurveyException("QUESTION_PARENT_INVALID", "Parent question belongs to a different survey", HttpStatus.BAD_REQUEST);
        }
        String questionType = questionTypeRegistry.validateAndNormalize(request);
        SurveyQuestionEntity question = questionRepository.save(new SurveyQuestionEntity(
                survey,
                section,
                parent,
                request.code(),
                request.prompt(),
                request.helpText(),
                questionType,
                request.orderIndex(),
                request.required(),
                request.defaultValue(),
                request.conditionExpression(),
                request.calculationExpression(),
                request.metadataJson()));
        List<QuestionOptionEntity> options = saveOptions(question, request.options());
        List<ValidationRuleEntity> rules = saveRules(survey, question, request.validationRules());
        audit(actorUserId, "SURVEY_QUESTION_CREATED", question.id().toString());
        return mapper.question(question, options, rules);
    }

    /** Lists questions for a survey. */
    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(UUID surveyId) {
        return questionRepository.findBySurvey_IdOrderByOrderIndexAsc(surveyId).stream()
                .map(question -> mapper.question(
                        question,
                        optionRepository.findByQuestion_IdOrderByOrderIndexAsc(question.id()),
                        ruleRepository.findByQuestion_IdOrderByOrderIndexAsc(question.id())))
                .toList();
    }

    /** Creates a survey-level validation rule. */
    @Transactional
    public ValidationRuleResponse createValidationRule(UUID surveyId, CreateValidationRuleRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        ensureEditableDefinition(survey);
        SurveyQuestionEntity question = request.questionId() == null ? null : question(request.questionId());
        if (question != null && !question.surveyId().equals(survey.id())) {
            throw new SurveyException("VALIDATION_QUESTION_INVALID", "Question belongs to a different survey", HttpStatus.BAD_REQUEST);
        }
        validationService.validateRule(request);
        ValidationRuleEntity rule = ruleRepository.save(new ValidationRuleEntity(
                survey, question, request.ruleType(), request.expression(), request.message(), request.paramsJson(), request.orderIndex()));
        audit(actorUserId, "SURVEY_VALIDATION_RULE_CREATED", rule.id().toString());
        return mapper.rule(rule);
    }

    /** Assigns a survey to an organization, team, user, or region. */
    @Transactional
    public AssignmentResponse assignSurvey(UUID surveyId, CreateAssignmentRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        SurveyAssignmentEntity assignment = assignmentRepository.save(new SurveyAssignmentEntity(
                survey, request.assignmentType(), request.targetId(), request.targetName(), actorUserId, request.dueAt()));
        audit(actorUserId, "SURVEY_ASSIGNED", assignment.id().toString());
        return mapper.assignment(assignment);
    }

    /** Lists assignments for a survey. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> listAssignments(UUID surveyId) {
        return assignmentRepository.findBySurvey_IdAndIsActiveTrue(surveyId).stream().map(mapper::assignment).toList();
    }

    /** Lists survey versions. */
    @Transactional(readOnly = true)
    public List<VersionResponse> listVersions(UUID surveyId) {
        return versionRepository.findBySurvey_IdOrderByVersionNumberDesc(surveyId).stream().map(mapper::version).toList();
    }

    /** Lists survey status history. */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> listStatusHistory(UUID surveyId) {
        return historyRepository.findBySurvey_IdOrderByCreatedAtAsc(surveyId).stream().map(mapper::history).toList();
    }

    /** Submits answers for a survey and persists them for retrieval/reporting. */
    @Transactional
    public SubmissionResponse submitSurvey(UUID surveyId, SubmitSurveyRequest request, UUID actorUserId) {
        SurveyEntity survey = survey(surveyId);
        if (!Set.of(SurveyStatus.PUBLISHED, SurveyStatus.ACTIVE).contains(survey.status())) {
            throw new SurveyException("SURVEY_NOT_ACCEPTING_SUBMISSIONS", "Survey must be published or active before submissions are accepted", HttpStatus.CONFLICT);
        }
        Map<UUID, SurveyQuestionEntity> questions = new LinkedHashMap<>();
        for (SurveyQuestionEntity question : questionRepository.findBySurvey_IdOrderByOrderIndexAsc(surveyId)) {
            questions.put(question.id(), question);
        }
        if (questions.isEmpty()) {
            throw new SurveyException("SURVEY_HAS_NO_QUESTIONS", "Survey must contain at least one question before submission", HttpStatus.CONFLICT);
        }
        Map<UUID, String> submittedAnswers = new LinkedHashMap<>();
        for (SubmitAnswerRequest answer : request.answers()) {
            SurveyQuestionEntity question = questions.get(answer.questionId());
            if (question == null) {
                throw new SurveyException("SURVEY_QUESTION_INVALID", "Submitted answer references a question outside this survey", HttpStatus.BAD_REQUEST);
            }
            submittedAnswers.put(answer.questionId(), answer.value().trim());
        }
        for (SurveyQuestionEntity question : questions.values()) {
            if (question.required() && (!submittedAnswers.containsKey(question.id()) || submittedAnswers.get(question.id()).isBlank())) {
                throw new SurveyException("SURVEY_REQUIRED_ANSWER_MISSING", "Required question is missing: " + question.code(), HttpStatus.BAD_REQUEST);
            }
        }
        SurveySubmissionEntity submission = new SurveySubmissionEntity(survey, actorUserId);
        submittedAnswers.forEach((questionId, value) -> submission.addAnswer(questions.get(questionId), value));
        SurveySubmissionEntity saved = submissionRepository.save(submission);
        audit(actorUserId, "SURVEY_SUBMITTED", saved.id().toString());
        publishSurvey(EventTopic.SURVEY_SUBMITTED, survey, actorUserId);
        return submission(saved);
    }

    /** Lists submissions for a survey. */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> listSubmissions(UUID surveyId) {
        survey(surveyId);
        return submissionRepository.findBySurvey_IdOrderBySubmittedAtDesc(surveyId).stream().map(this::submission).toList();
    }

    /** Gets one submission by ID. */
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(UUID surveyId, UUID submissionId) {
        survey(surveyId);
        SurveySubmissionEntity submission = submissionRepository.findWithAnswersById(submissionId)
                .orElseThrow(() -> new SurveyException("SURVEY_SUBMISSION_NOT_FOUND", "Survey submission was not found", HttpStatus.NOT_FOUND));
        if (!submission.surveyId().equals(surveyId)) {
            throw new SurveyException("SURVEY_SUBMISSION_NOT_FOUND", "Survey submission was not found", HttpStatus.NOT_FOUND);
        }
        return submission(submission);
    }

    /** Lists registered question types. */
    public Set<String> supportedQuestionTypes() {
        return questionTypeRegistry.supportedTypes();
    }

    private SubmissionResponse submission(SurveySubmissionEntity submission) {
        return new SubmissionResponse(
                submission.id(),
                submission.surveyId(),
                submission.organizationId(),
                submission.submittedByUserId(),
                submission.status(),
                submission.submittedAt(),
                submission.answers().stream()
                        .map(answer -> new SubmittedAnswerResponse(answer.id(), answer.questionId(), answer.questionCode(), answer.answerValue(), answer.createdAt()))
                        .toList());
    }

    private SurveyEntity survey(UUID surveyId) {
        return surveyRepository.findByIdAndIsActiveTrue(surveyId)
                .orElseThrow(() -> new SurveyException("SURVEY_NOT_FOUND", "Survey was not found", HttpStatus.NOT_FOUND));
    }

    private SurveyTemplateEntity template(UUID templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new SurveyException("SURVEY_TEMPLATE_NOT_FOUND", "Survey template was not found", HttpStatus.NOT_FOUND));
    }

    private SurveySectionEntity section(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new SurveyException("SURVEY_SECTION_NOT_FOUND", "Survey section was not found", HttpStatus.NOT_FOUND));
    }

    private SurveyQuestionEntity question(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new SurveyException("SURVEY_QUESTION_NOT_FOUND", "Survey question was not found", HttpStatus.NOT_FOUND));
    }

    private void ensureUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new SurveyException("USER_NOT_FOUND", "Authenticated user was not found", HttpStatus.UNAUTHORIZED);
        }
    }

    private void ensureOrganization(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new SurveyException("ORGANIZATION_NOT_FOUND", "Organization was not found", HttpStatus.BAD_REQUEST);
        }
    }

    private void ensureEditableDefinition(SurveyEntity survey) {
        if (survey.status() != SurveyStatus.DRAFT && survey.status() != SurveyStatus.REVIEW) {
            throw new SurveyException(
                    "SURVEY_DEFINITION_LOCKED",
                    "Survey definition can only be changed while draft or review",
                    HttpStatus.CONFLICT);
        }
    }

    private List<QuestionOptionEntity> saveOptions(SurveyQuestionEntity question, List<CreateQuestionOptionRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> optionRepository.save(new QuestionOptionEntity(question, request.value(), request.label(), request.orderIndex(), request.metadataJson())))
                .toList();
    }

    private List<ValidationRuleEntity> saveRules(SurveyEntity survey, SurveyQuestionEntity question, List<CreateValidationRuleRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .peek(validationService::validateRule)
                .map(request -> ruleRepository.save(new ValidationRuleEntity(
                        survey, question, request.ruleType(), request.expression(), request.message(), request.paramsJson(), request.orderIndex())))
                .toList();
    }

    private void createVersion(SurveyEntity survey, UUID actorUserId) {
        versionRepository.save(new SurveyVersionEntity(
                survey, survey.currentVersion(), survey.name(), survey.description(), snapshot(survey), actorUserId));
    }

    private String snapshot(SurveyEntity survey) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "surveyId", survey.id(),
                    "name", survey.name(),
                    "description", survey.description() == null ? "" : survey.description(),
                    "status", survey.status().name(),
                    "currentVersion", survey.currentVersion()));
        } catch (JsonProcessingException ex) {
            throw new SurveyException("SURVEY_VERSION_SNAPSHOT_FAILED", "Could not create survey version snapshot", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void audit(UUID actorUserId, String eventType, String details) {
        auditService.record(actorUserId, eventType, AuditOutcome.SUCCESS, null, null, details);
    }

    private void publishSurvey(EventTopic topic, SurveyEntity survey, UUID actorUserId) {
        outboxService.enqueue(
                topic,
                "SURVEY",
                survey.id(),
                survey.organizationId(),
                actorUserId,
                new EventPayloads.SurveyPayload(survey.id(), survey.organizationId(), survey.name(), survey.status().name(), survey.currentVersion(), survey.updatedAt()));
    }
}

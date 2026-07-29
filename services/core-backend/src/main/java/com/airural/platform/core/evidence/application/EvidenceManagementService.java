/*
 * Purpose: Implements enterprise evidence management use cases.
 * Why it exists: Controllers need a transactional boundary for upload, metadata updates, deletion, restore, download, search, audit, and versioning.
 * Architecture fit: Application service for Milestone 4 Evidence and Asset Management.
 */
package com.airural.platform.core.evidence.application;

import static com.airural.platform.core.evidence.infrastructure.EvidenceSpecifications.*;

import com.airural.platform.core.evidence.domain.*;
import com.airural.platform.core.evidence.infrastructure.*;
import com.airural.platform.core.evidence.web.dto.EvidenceDtos.*;
import com.airural.platform.core.identity.application.AuditService;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.identity.infrastructure.OrganizationRepository;
import com.airural.platform.core.identity.infrastructure.UserAccountRepository;
import com.airural.platform.core.survey.domain.SurveyEntity;
import com.airural.platform.core.survey.domain.SurveyQuestionEntity;
import com.airural.platform.core.survey.infrastructure.SurveyQuestionRepository;
import com.airural.platform.core.survey.infrastructure.SurveyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Transactional application service for evidence management. */
@Service
public class EvidenceManagementService {
    private final EvidenceRepository evidenceRepository;
    private final EvidenceVersionRepository versionRepository;
    private final EvidenceAuditRepository auditRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final EvidenceValidationService validationService;
    private final EvidenceStorageService storageService;
    private final EvidenceMapper mapper;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public EvidenceManagementService(
            EvidenceRepository evidenceRepository,
            EvidenceVersionRepository versionRepository,
            EvidenceAuditRepository auditRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userRepository,
            SurveyRepository surveyRepository,
            SurveyQuestionRepository questionRepository,
            EvidenceValidationService validationService,
            EvidenceStorageService storageService,
            EvidenceMapper mapper,
            AuditService auditService,
            ObjectMapper objectMapper) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.auditRepository = auditRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.validationService = validationService;
        this.storageService = storageService;
        this.mapper = mapper;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /** Uploads an evidence asset and creates initial version and audit records. */
    @Transactional
    public EvidenceResponse upload(
            MultipartFile file,
            UUID organizationId,
            UUID surveyId,
            UUID questionId,
            String title,
            String description,
            String customMetadataJson,
            Set<String> tags,
            UUID actorUserId) {
        ensureUser(actorUserId);
        ensureOrganization(organizationId);
        ensureSurveyReference(organizationId, surveyId);
        ensureQuestionReference(surveyId, questionId);
        EvidenceValidationService.ValidatedEvidenceUpload validated = validationService.validate(file, organizationId);
        StoredEvidenceObject stored = storageService.store(validated.content(), validated.sanitizedFileName());
        EvidenceEntity evidence = evidenceRepository.save(new EvidenceEntity(
                organizationId,
                surveyId,
                questionId,
                actorUserId,
                validated.evidenceType(),
                validated.sanitizedFileName(),
                stored.storedFileName(),
                validated.mimeType(),
                validated.sizeBytes(),
                validated.checksum(),
                stored.provider(),
                stored.storageKey(),
                title,
                description,
                customMetadataJson,
                tags));
        createVersion(evidence, actorUserId);
        audit(evidence, actorUserId, EvidenceAuditAction.UPLOADED, "Evidence uploaded");
        return mapper.evidence(evidence);
    }

    /** Updates editable metadata and records a new evidence version. */
    @Transactional
    public EvidenceResponse updateMetadata(UUID evidenceId, UpdateEvidenceMetadataRequest request, UUID actorUserId) {
        EvidenceEntity evidence = evidence(evidenceId, false);
        evidence.updateMetadata(request.title(), request.description(), request.customMetadataJson(), request.tags());
        createVersion(evidence, actorUserId);
        audit(evidence, actorUserId, EvidenceAuditAction.METADATA_UPDATED, "Evidence metadata updated");
        return mapper.evidence(evidence);
    }

    /** Soft-deletes an evidence record. */
    @Transactional
    public EvidenceResponse softDelete(UUID evidenceId, UUID actorUserId) {
        EvidenceEntity evidence = evidence(evidenceId, false);
        evidence.softDelete();
        audit(evidence, actorUserId, EvidenceAuditAction.SOFT_DELETED, "Evidence soft deleted");
        return mapper.evidence(evidence);
    }

    /** Restores a soft-deleted evidence record. */
    @Transactional
    public EvidenceResponse restore(UUID evidenceId, UUID actorUserId) {
        EvidenceEntity evidence = evidence(evidenceId, true);
        if (evidence.isActive()) {
            throw new EvidenceException("EVIDENCE_NOT_DELETED", "Evidence is not deleted", HttpStatus.CONFLICT);
        }
        if (evidenceRepository.existsByOrganizationIdAndSha256ChecksumAndIsActiveTrue(evidence.organizationId(), evidence.sha256Checksum())) {
            throw new EvidenceException("EVIDENCE_RESTORE_DUPLICATE", "An active duplicate exists for this organization", HttpStatus.CONFLICT);
        }
        evidence.restore();
        audit(evidence, actorUserId, EvidenceAuditAction.RESTORED, "Evidence restored");
        return mapper.evidence(evidence);
    }

    /** Downloads an active evidence binary and audits access. */
    @Transactional
    public EvidenceBinary download(UUID evidenceId, UUID actorUserId) {
        EvidenceEntity evidence = evidence(evidenceId, false);
        EvidenceBinary binary = storageService.load(evidence.storageKey(), evidence.mimeType(), evidence.originalFileName());
        audit(evidence, actorUserId, EvidenceAuditAction.DOWNLOADED, "Evidence downloaded");
        return binary;
    }

    /** Requests a provider-backed signed URL when supported by the active storage adapter. */
    @Transactional
    public SignedUrlResponse signedUrl(UUID evidenceId, UUID actorUserId, Duration ttl) {
        EvidenceEntity evidence = evidence(evidenceId, false);
        Instant expiresAt = Instant.now().plus(ttl);
        Optional<String> url = storageService.createSignedUrl(evidence.storageKey(), ttl);
        audit(evidence, actorUserId, EvidenceAuditAction.SIGNED_URL_REQUESTED, "Signed URL requested");
        return new SignedUrlResponse(evidence.id(), url.orElse(null), expiresAt, url.isPresent());
    }

    /** Searches evidence by supported filters. */
    @Transactional(readOnly = true)
    public Page<EvidenceResponse> search(
            UUID organizationId,
            UUID surveyId,
            UUID uploaderId,
            String tag,
            Instant uploadedFrom,
            EvidenceType evidenceType,
            boolean includeDeleted,
            Pageable pageable) {
        Specification<EvidenceEntity> spec = Specification.where(activeOnly(includeDeleted))
                .and(organizationEquals(organizationId))
                .and(surveyEquals(surveyId))
                .and(uploaderEquals(uploaderId))
                .and(hasTag(tag))
                .and(createdAfter(uploadedFrom))
                .and(typeEquals(evidenceType));
        return evidenceRepository.findAll(spec, pageable).map(mapper::evidence);
    }

    /** Returns evidence metadata by ID. */
    @Transactional(readOnly = true)
    public EvidenceResponse get(UUID evidenceId, boolean includeDeleted) {
        return mapper.evidence(evidence(evidenceId, includeDeleted));
    }

    /** Lists evidence version history. */
    @Transactional(readOnly = true)
    public List<EvidenceVersionResponse> versions(UUID evidenceId) {
        evidence(evidenceId, true);
        return versionRepository.findByEvidence_IdOrderByVersionNumberDesc(evidenceId).stream().map(mapper::version).toList();
    }

    /** Lists evidence audit history. */
    @Transactional(readOnly = true)
    public List<EvidenceAuditResponse> auditHistory(UUID evidenceId) {
        evidence(evidenceId, true);
        return auditRepository.findByEvidence_IdOrderByCreatedAtAsc(evidenceId).stream().map(mapper::audit).toList();
    }

    private EvidenceEntity evidence(UUID evidenceId, boolean includeDeleted) {
        Optional<EvidenceEntity> entity = includeDeleted ? evidenceRepository.findById(evidenceId) : evidenceRepository.findByIdAndIsActiveTrue(evidenceId);
        return entity.orElseThrow(() -> new EvidenceException("EVIDENCE_NOT_FOUND", "Evidence was not found", HttpStatus.NOT_FOUND));
    }

    private void ensureUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EvidenceException("USER_NOT_FOUND", "Authenticated user was not found", HttpStatus.UNAUTHORIZED);
        }
    }

    private void ensureOrganization(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EvidenceException("ORGANIZATION_NOT_FOUND", "Organization was not found", HttpStatus.BAD_REQUEST);
        }
    }

    private void ensureSurveyReference(UUID organizationId, UUID surveyId) {
        if (surveyId == null) {
            return;
        }
        SurveyEntity survey = surveyRepository.findByIdAndIsActiveTrue(surveyId)
                .orElseThrow(() -> new EvidenceException("SURVEY_NOT_FOUND", "Survey was not found", HttpStatus.BAD_REQUEST));
        if (!survey.organizationId().equals(organizationId)) {
            throw new EvidenceException("EVIDENCE_SURVEY_ORGANIZATION_MISMATCH", "Survey belongs to a different organization", HttpStatus.BAD_REQUEST);
        }
    }

    private void ensureQuestionReference(UUID surveyId, UUID questionId) {
        if (questionId == null) {
            return;
        }
        if (surveyId == null) {
            throw new EvidenceException("EVIDENCE_SURVEY_REQUIRED", "Survey reference is required when question reference is supplied", HttpStatus.BAD_REQUEST);
        }
        SurveyQuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EvidenceException("SURVEY_QUESTION_NOT_FOUND", "Survey question was not found", HttpStatus.BAD_REQUEST));
        if (!question.surveyId().equals(surveyId)) {
            throw new EvidenceException("EVIDENCE_QUESTION_SURVEY_MISMATCH", "Question belongs to a different survey", HttpStatus.BAD_REQUEST);
        }
    }

    private void createVersion(EvidenceEntity evidence, UUID actorUserId) {
        versionRepository.save(new EvidenceVersionEntity(evidence, snapshot(evidence), actorUserId));
    }

    private String snapshot(EvidenceEntity evidence) {
        try {
            EvidenceMetadataEntity metadata = evidence.metadata();
            return objectMapper.writeValueAsString(Map.of(
                    "evidenceId", evidence.id(),
                    "title", metadata == null || metadata.title() == null ? "" : metadata.title(),
                    "description", metadata == null || metadata.description() == null ? "" : metadata.description(),
                    "tags", evidence.tags().stream().map(EvidenceTagEntity::name).toList(),
                    "active", evidence.isActive()));
        } catch (JsonProcessingException ex) {
            throw new EvidenceException("EVIDENCE_VERSION_SNAPSHOT_FAILED", "Could not create evidence version snapshot", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void audit(EvidenceEntity evidence, UUID actorUserId, EvidenceAuditAction action, String details) {
        auditRepository.save(new EvidenceAuditEntity(evidence, actorUserId, action, AuditOutcome.SUCCESS, details));
        auditService.record(actorUserId, "EVIDENCE_" + action.name(), AuditOutcome.SUCCESS, null, null, evidence.id() + ": " + details);
    }
}

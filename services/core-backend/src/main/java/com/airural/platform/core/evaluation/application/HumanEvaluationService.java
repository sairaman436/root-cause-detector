/*
 * Purpose: Coordinates read-only held-out example exposure and authenticated human rubric submission.
 * Why it exists: Human scores must be explicit, independently persisted, task-valid, and attributable to a JWT principal.
 * Architecture fit: Application service for the evaluation bounded context; it never mutates training or evaluation-set files.
 */
package com.airural.platform.core.evaluation.application;

import com.airural.platform.core.evaluation.domain.HumanEvaluationEntity;
import com.airural.platform.core.evaluation.domain.HumanEvaluationScoreEntity;
import com.airural.platform.core.evaluation.infrastructure.HumanEvaluationRepository;
import com.airural.platform.core.evaluation.web.dto.HumanEvaluationDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Human evaluation workflow application service. */
@Service
public class HumanEvaluationService {
    private final HumanEvaluationSetCatalog catalog;
    private final HumanEvaluationRepository reviews;
    private final ObjectMapper objectMapper;

    public HumanEvaluationService(HumanEvaluationSetCatalog catalog, HumanEvaluationRepository reviews, ObjectMapper objectMapper) {
        this.catalog = catalog;
        this.reviews = reviews;
        this.objectMapper = objectMapper;
    }

    /** Lists immutable examples and review progress without exposing other reviewers' scores. */
    @Transactional(readOnly = true)
    public HumanEvaluationExamplesResponse examples() {
        List<HumanEvaluationEntity> persisted = reviews.findByEvaluationSetVersion(HumanEvaluationSetCatalog.EVALUATION_SET_VERSION);
        Set<String> scored = persisted.stream().map(HumanEvaluationEntity::getExampleId).collect(Collectors.toSet());
        List<HumanEvaluationExampleResponse> result = catalog.all().stream()
                .map(example -> response(example, persisted.stream().filter(review -> review.getExampleId().equals(example.exampleId())).toList()))
                .toList();
        return new HumanEvaluationExamplesResponse(
                HumanEvaluationSetCatalog.EVALUATION_SET_VERSION,
                HumanEvaluationSetCatalog.RUBRIC_VERSION,
                result.size(), scored.size(), Math.max(0, result.size() - scored.size()), result);
    }

    /** Returns one immutable example and its review progress. */
    @Transactional(readOnly = true)
    public HumanEvaluationExampleResponse example(String exampleId) {
        HumanEvaluationSetCatalog.Example example = catalog.byId(exampleId);
        List<HumanEvaluationEntity> persisted = reviews.findByEvaluationSetVersion(HumanEvaluationSetCatalog.EVALUATION_SET_VERSION).stream()
                .filter(review -> review.getExampleId().equals(exampleId)).toList();
        return response(example, persisted);
    }

    /** Persists one explicit score set using only the authenticated JWT principal as reviewer identity. */
    @Transactional
    public HumanEvaluationReviewResponse submit(HumanEvaluationReviewRequest request, AuthenticatedUser reviewer) {
        HumanEvaluationSetCatalog.Example example = catalog.byId(request.exampleId());
        validateTaskScores(example.task(), request.scores());
        validateEvidenceReferences(example, request.evidenceReferencesUsed());
        if (reviews.existsByEvaluationSetVersionAndExampleIdAndReviewerId(
                HumanEvaluationSetCatalog.EVALUATION_SET_VERSION, example.exampleId(), reviewer.userId())) {
            throw new HumanEvaluationException("HUMAN_REVIEW_ALREADY_EXISTS", "This reviewer has already scored the example", HttpStatus.CONFLICT);
        }

        HumanEvaluationEntity evaluation = new HumanEvaluationEntity(
                UUID.randomUUID(), HumanEvaluationSetCatalog.EVALUATION_SET_VERSION, example.exampleId(), example.task(),
                example.modelVersion(), example.promptVersion(), HumanEvaluationSetCatalog.RUBRIC_VERSION,
                json(example.inferenceConfiguration()), example.outputSha256(), reviewer.userId(), request.reviewerComments(),
                json(request.evidenceReferencesUsed()), Instant.now());
        evaluation.attachScore(new HumanEvaluationScoreEntity(UUID.randomUUID(),
                request.scores().rootCauseQuality(), request.scores().recommendationQuality(), request.scores().ragEvidenceQuality(),
                request.scores().uncertaintyHandling(), request.scores().practicalUsefulness()));
        try {
            HumanEvaluationEntity saved = reviews.saveAndFlush(evaluation);
            return new HumanEvaluationReviewResponse(saved.getId(), saved.getEvaluationSetVersion(), saved.getExampleId(),
                    saved.getTask(), saved.getRubricVersion(), saved.getReviewerId(), saved.getReviewedAt(), request.scores(),
                    request.evidenceReferencesUsed(), saved.getReviewerComments(), "SUBMITTED");
        } catch (DataIntegrityViolationException exception) {
            throw new HumanEvaluationException("HUMAN_REVIEW_ALREADY_EXISTS", "This reviewer has already scored the example", HttpStatus.CONFLICT);
        }
    }

    private HumanEvaluationExampleResponse response(HumanEvaluationSetCatalog.Example example, List<HumanEvaluationEntity> persisted) {
        return new HumanEvaluationExampleResponse(
                HumanEvaluationSetCatalog.EVALUATION_SET_VERSION, HumanEvaluationSetCatalog.RUBRIC_VERSION,
                example.exampleId(), example.task(), example.scenarioGroup(), example.input(), example.retrievedContext(),
                example.output(), example.citations(), example.provenance(), example.modelVersion(), example.promptVersion(),
                example.inferenceConfiguration(), example.outputSha256(), persisted.isEmpty() ? "REMAINING" : "SCORED", persisted.size());
    }

    private void validateTaskScores(String task, Scores scores) {
        require(scores.ragEvidenceQuality(), "ragEvidenceQuality");
        require(scores.uncertaintyHandling(), "uncertaintyHandling");
        require(scores.practicalUsefulness(), "practicalUsefulness");
        if ("root-cause-analysis".equals(task) || "recommendation-generation".equals(task)) {
            require(scores.rootCauseQuality(), "rootCauseQuality");
        }
        if ("recommendation-generation".equals(task)) {
            require(scores.recommendationQuality(), "recommendationQuality");
        }
    }

    private void require(Integer score, String field) {
        if (score == null) {
            throw new HumanEvaluationException("REQUIRED_RUBRIC_SCORE_MISSING", "Required rubric score is missing: " + field, HttpStatus.BAD_REQUEST);
        }
        if (score < 0 || score > 4) {
            throw new HumanEvaluationException("RUBRIC_SCORE_OUT_OF_RANGE", "Rubric scores must be between 0 and 4", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEvidenceReferences(HumanEvaluationSetCatalog.Example example, List<String> references) {
        Set<String> allowed = new HashSet<>();
        example.citations().forEach(citation -> {
            if (citation.has("source_id")) allowed.add(citation.path("source_id").asText());
        });
        if (references.stream().anyMatch(reference -> !allowed.contains(reference))) {
            throw new HumanEvaluationException("INVALID_EVIDENCE_REFERENCE", "Evidence references must use source IDs from the held-out example", HttpStatus.BAD_REQUEST);
        }
    }

    private String json(JsonNode node) {
        try { return objectMapper.writeValueAsString(node); }
        catch (JsonProcessingException exception) { throw new HumanEvaluationException("REVIEW_SERIALIZATION_FAILED", "Review metadata could not be stored", HttpStatus.INTERNAL_SERVER_ERROR); }
    }

    private String json(List<String> values) {
        try { return objectMapper.writeValueAsString(values); }
        catch (JsonProcessingException exception) { throw new HumanEvaluationException("REVIEW_SERIALIZATION_FAILED", "Review evidence references could not be stored", HttpStatus.INTERNAL_SERVER_ERROR); }
    }
}


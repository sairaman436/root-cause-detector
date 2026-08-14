/*
 * Purpose: Coordinates immutable multimodal trace exposure and authenticated human scoring.
 * Why it exists: Human quality data must be explicit, durable, attributable, and separate from AI trace mutation.
 * Architecture fit: Application service for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.application;

import com.airural.platform.core.evaluation.domain.MultimodalHumanReviewEntity;
import com.airural.platform.core.evaluation.infrastructure.MultimodalHumanReviewRepository;
import com.airural.platform.core.evaluation.web.dto.MultimodalEvaluationDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Server-backed multimodal human evaluation service. */
@Service
public class MultimodalHumanEvaluationService {
    private final MultimodalEvaluationTraceCatalog catalog;
    private final MultimodalHumanReviewRepository reviews;
    private final ObjectMapper objectMapper;

    public MultimodalHumanEvaluationService(MultimodalEvaluationTraceCatalog catalog,
            MultimodalHumanReviewRepository reviews, ObjectMapper objectMapper) {
        this.catalog = catalog;
        this.reviews = reviews;
        this.objectMapper = objectMapper;
    }

    /** Lists immutable traces with server-backed progress and dashboard summaries. */
    @Transactional(readOnly = true)
    public TraceQueueResponse traces() {
        List<MultimodalHumanReviewEntity> persisted = reviews.findAll();
        Map<String, List<MultimodalHumanReviewEntity>> byTrace = persisted.stream()
                .collect(Collectors.groupingBy(MultimodalHumanReviewEntity::getTraceId));
        List<TraceResponse> traceResponses = catalog.all().stream()
                .map(trace -> toTraceResponse(trace, byTrace.getOrDefault(trace.traceId(), List.of())))
                .toList();
        Set<UUID> reviewers = persisted.stream().map(MultimodalHumanReviewEntity::getReviewerId).collect(Collectors.toSet());
        int scored = (int) traceResponses.stream().filter(trace -> "SCORED".equals(trace.reviewStatus())).count();
        return new TraceQueueResponse(MultimodalEvaluationTraceCatalog.ARTIFACT_VERSION,
                MultimodalEvaluationTraceCatalog.EVALUATION_ROUND, MultimodalEvaluationTraceCatalog.RUBRIC_VERSION,
                traceResponses.size(), scored, Math.max(0, traceResponses.size() - scored), reviewers.size(),
                traceResponses, summaries(catalog.all(), persisted));
    }

    /** Returns one immutable trace and its review progress. */
    @Transactional(readOnly = true)
    public TraceResponse trace(String traceId) {
        MultimodalEvaluationTraceCatalog.Trace trace = catalog.byId(traceId);
        return toTraceResponse(trace, reviews.findByTraceId(traceId));
    }

    /** Persists one review with reviewer identity taken exclusively from the authenticated JWT. */
    @Transactional
    public ReviewResponse submit(ReviewRequest request, AuthenticatedUser reviewer) {
        MultimodalEvaluationTraceCatalog.Trace trace = catalog.byId(request.traceId());
        exact(request.artifactVersion(), trace.artifactVersion(), "ARTIFACT_VERSION_MISMATCH");
        exact(request.evaluationRound(), trace.evaluationRound(), "EVALUATION_ROUND_MISMATCH");
        exact(request.rubricVersion(), MultimodalEvaluationTraceCatalog.RUBRIC_VERSION, "RUBRIC_VERSION_MISMATCH");
        requireScores(request.scores());
        if (reviews.existsByTraceIdAndReviewerId(trace.traceId(), reviewer.userId())) {
            throw new HumanEvaluationException("MULTIMODAL_REVIEW_ALREADY_EXISTS",
                    "This reviewer has already submitted a review for this trace", HttpStatus.CONFLICT);
        }
        String flags = json(request.unsupportedClaimFlags());
        Instant reviewedAt = Instant.now();
        MultimodalHumanReviewEntity entity = new MultimodalHumanReviewEntity(UUID.randomUUID(), trace.traceId(),
                trace.artifactVersion(), trace.evaluationRound(), reviewer.userId(), request.rubricVersion(),
                request.scores().observationQuality(), request.scores().evidenceRelevance(), request.scores().rootCauseQuality(),
                request.scores().recommendationQuality(), request.scores().grounding(), request.scores().overallUsefulness(),
                request.failureClassification(), flags, request.reviewerComments(), "SUBMITTED", reviewedAt);
        try {
            MultimodalHumanReviewEntity saved = reviews.saveAndFlush(entity);
            return new ReviewResponse(saved.getId(), saved.getTraceId(), saved.getArtifactVersion(), saved.getEvaluationRound(),
                    saved.getRubricVersion(), saved.getReviewerId(), saved.getReviewedAt(), request.scores(),
                    saved.getFailureClassification(), request.unsupportedClaimFlags(), saved.getReviewerComments(), saved.getSubmissionStatus());
        } catch (DataIntegrityViolationException exception) {
            throw new HumanEvaluationException("MULTIMODAL_REVIEW_ALREADY_EXISTS",
                    "This reviewer has already submitted a review for this trace", HttpStatus.CONFLICT);
        }
    }

    private TraceResponse toTraceResponse(MultimodalEvaluationTraceCatalog.Trace trace,
            List<MultimodalHumanReviewEntity> traceReviews) {
        return new TraceResponse(trace.traceId(), trace.artifactVersion(), trace.evaluationRound(), trace.domain(),
                trace.question(), trace.imageName(), trace.imageType(), trace.imageSize(), trace.artifact(),
                traceReviews.isEmpty() ? "REMAINING" : "SCORED", traceReviews.size());
    }

    private List<DomainSummary> summaries(List<MultimodalEvaluationTraceCatalog.Trace> traces,
            List<MultimodalHumanReviewEntity> persisted) {
        Map<String, String> domainByTrace = traces.stream().collect(Collectors.toMap(
                MultimodalEvaluationTraceCatalog.Trace::traceId, MultimodalEvaluationTraceCatalog.Trace::domain, (left, right) -> left));
        Map<String, List<MultimodalHumanReviewEntity>> grouped = persisted.stream()
                .filter(review -> domainByTrace.containsKey(review.getTraceId()))
                .collect(Collectors.groupingBy(review -> domainByTrace.get(review.getTraceId()), LinkedHashMap::new, Collectors.toList()));
        return grouped.entrySet().stream().map(entry -> {
            List<MultimodalHumanReviewEntity> values = entry.getValue();
            Map<String, Integer> failures = new HashMap<>();
            values.forEach(value -> failures.merge(value.getFailureClassification(), 1, Integer::sum));
            return new DomainSummary(entry.getKey(), values.size(), average(values, MultimodalHumanReviewEntity::getObservationQuality),
                    average(values, MultimodalHumanReviewEntity::getEvidenceRelevance), average(values, MultimodalHumanReviewEntity::getRootCauseQuality),
                    average(values, MultimodalHumanReviewEntity::getRecommendationQuality), average(values, MultimodalHumanReviewEntity::getGrounding),
                    average(values, MultimodalHumanReviewEntity::getOverallUsefulness),
                    (int) values.stream().filter(value -> value.getRecommendationQuality() != null).count(), Map.copyOf(failures));
        }).toList();
    }

    private Double average(List<MultimodalHumanReviewEntity> values, Function<MultimodalHumanReviewEntity, Integer> getter) {
        List<Integer> scored = values.stream().map(getter).filter(java.util.Objects::nonNull).toList();
        return scored.isEmpty() ? null : scored.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
    }

    private void exact(String actual, String expected, String code) {
        if (!expected.equals(actual)) {
            throw new HumanEvaluationException(code, "The submitted immutable evaluation metadata does not match the trace", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireScores(Scores scores) {
        if (scores.observationQuality() == null || scores.evidenceRelevance() == null || scores.rootCauseQuality() == null
                || scores.grounding() == null || scores.overallUsefulness() == null) {
            throw new HumanEvaluationException("REQUIRED_MULTIMODAL_SCORE_MISSING",
                    "Observation, evidence, root-cause, grounding, and usefulness scores are required", HttpStatus.BAD_REQUEST);
        }
    }

    private String json(UnsupportedClaimFlags flags) {
        try { return objectMapper.writeValueAsString(flags); }
        catch (JsonProcessingException exception) {
            throw new HumanEvaluationException("MULTIMODAL_REVIEW_SERIALIZATION_FAILED",
                    "Review metadata could not be stored", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

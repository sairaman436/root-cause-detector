/*
 * Purpose: Converts validation, authentication, and application exceptions into standard API errors.
 * Why it exists: Prevents raw exceptions from leaking to clients and keeps error handling consistent.
 * Architecture fit: Implements the approved exception handling model for backend APIs.
 */
package com.airural.platform.core.common;

import com.airural.platform.core.identity.application.IdentityException;
import com.airural.platform.core.agents.application.AgentException;
import com.airural.platform.core.ai.application.AiException;
import com.airural.platform.core.decision.application.DecisionException;
import com.airural.platform.core.evidence.application.EvidenceException;
import com.airural.platform.core.evaluation.application.EvaluationException;
import com.airural.platform.core.finetuning.application.FineTuningException;
import com.airural.platform.core.geospatial.application.GeospatialException;
import com.airural.platform.core.governance.application.GovernanceException;
import com.airural.platform.core.datasets.application.DatasetException;
import com.airural.platform.core.knowledge.application.KnowledgeException;
import com.airural.platform.core.learning.application.LearningException;
import com.airural.platform.core.evaluation.application.HumanEvaluationException;
import com.airural.platform.core.optimization.application.OptimizationException;
import com.airural.platform.core.release.application.ReleaseException;
import com.airural.platform.core.reports.application.ReportException;
import com.airural.platform.core.research.application.ResearchException;
import com.airural.platform.core.serving.application.ServingException;
import com.airural.platform.core.survey.application.SurveyException;
import com.airural.platform.core.training.application.TrainingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Central REST exception mapper. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles domain-level identity errors. */
    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<ErrorResponse> handleIdentity(IdentityException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level survey errors. */
    @ExceptionHandler(SurveyException.class)
    public ResponseEntity<ErrorResponse> handleSurvey(SurveyException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level evidence errors. */
    @ExceptionHandler(EvidenceException.class)
    public ResponseEntity<ErrorResponse> handleEvidence(EvidenceException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level geospatial errors. */
    @ExceptionHandler(GeospatialException.class)
    public ResponseEntity<ErrorResponse> handleGeospatial(GeospatialException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level AI foundation errors. */
    @ExceptionHandler(AiException.class)
    public ResponseEntity<ErrorResponse> handleAi(AiException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level agent platform errors. */
    @ExceptionHandler(AgentException.class)
    public ResponseEntity<ErrorResponse> handleAgent(AgentException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level decision intelligence errors. */
    @ExceptionHandler(DecisionException.class)
    public ResponseEntity<ErrorResponse> handleDecision(DecisionException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level dataset engineering errors. */
    @ExceptionHandler(DatasetException.class)
    public ResponseEntity<ErrorResponse> handleDataset(DatasetException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level knowledge acquisition errors. */
    @ExceptionHandler(KnowledgeException.class)
    public ResponseEntity<ErrorResponse> handleKnowledge(KnowledgeException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level training factory errors. */
    @ExceptionHandler(TrainingException.class)
    public ResponseEntity<ErrorResponse> handleTraining(TrainingException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level fine-tuning lifecycle errors. */
    @ExceptionHandler(FineTuningException.class)
    public ResponseEntity<ErrorResponse> handleFineTuning(FineTuningException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level model evaluation errors. */
    @ExceptionHandler(EvaluationException.class)
    public ResponseEntity<ErrorResponse> handleEvaluation(EvaluationException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level model optimization errors. */
    @ExceptionHandler(OptimizationException.class)
    public ResponseEntity<ErrorResponse> handleOptimization(OptimizationException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level continuous learning errors. */
    @ExceptionHandler(LearningException.class)
    public ResponseEntity<ErrorResponse> handleLearning(LearningException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles controlled human-evaluation workflow failures. */
    @ExceptionHandler(HumanEvaluationException.class)
    public ResponseEntity<ErrorResponse> handleHumanEvaluation(HumanEvaluationException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level AI serving errors. */
    @ExceptionHandler(ServingException.class)
    public ResponseEntity<ErrorResponse> handleServing(ServingException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level AI governance errors. */
    @ExceptionHandler(GovernanceException.class)
    public ResponseEntity<ErrorResponse> handleGovernance(GovernanceException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level AI release engineering errors. */
    @ExceptionHandler(ReleaseException.class)
    public ResponseEntity<ErrorResponse> handleRelease(ReleaseException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles domain-level report errors. */
    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ErrorResponse> handleReport(ReportException ex, HttpServletRequest request) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex.code(), ex.getReason(), List.of(), request);
    }

    /** Handles domain-level research laboratory errors. */
    @ExceptionHandler(ResearchException.class)
    public ResponseEntity<ErrorResponse> handleResearch(ResearchException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), List.of(), request);
    }

    /** Handles bean validation errors for request bodies. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", details, request);
    }

    /** Handles bean validation errors for request parameters. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream().map(Object::toString).toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", details, request);
    }

    /** Handles authentication failures. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage(), List.of(), request);
    }

    /** Handles RBAC failures. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", List.of(), request);
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status, String code, String message, List<String> details, HttpServletRequest request) {
        String requestId = RequestIds.from(request);
        ErrorResponse body = ErrorResponse.of(code, message, details, requestId, requestId);
        return ResponseEntity.status(status).body(body);
    }
}

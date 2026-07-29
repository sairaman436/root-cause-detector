/*
 * Purpose: Converts validation, authentication, and application exceptions into standard API errors.
 * Why it exists: Prevents raw exceptions from leaking to clients and keeps error handling consistent.
 * Architecture fit: Implements the approved exception handling model for backend APIs.
 */
package com.airural.platform.core.common;

import com.airural.platform.core.identity.application.IdentityException;
import com.airural.platform.core.survey.application.SurveyException;
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

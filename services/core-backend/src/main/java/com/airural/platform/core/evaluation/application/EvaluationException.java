/*
 * Purpose: Represents domain errors for enterprise AI evaluation workflows.
 * Why it exists: AI-5 APIs need typed failures for immutable evaluations, promotion decisions, and model references.
 * Architecture fit: Application exception integrated with centralized REST error handling.
 */
package com.airural.platform.core.evaluation.application;

import org.springframework.http.HttpStatus;

/** Domain exception for AI-5 evaluation workflows. */
public class EvaluationException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a typed evaluation exception. */
    public EvaluationException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

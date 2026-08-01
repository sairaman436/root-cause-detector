/*
 * Purpose: Represents domain errors for the enterprise model training factory.
 * Why it exists: Training APIs need typed failures for dataset approval, scheduling, checkpoint, and model registry guardrails.
 * Architecture fit: Application exception integrated with centralized REST error handling.
 */
package com.airural.platform.core.training.application;

import org.springframework.http.HttpStatus;

/** Domain exception for AI-3 training factory workflows. */
public class TrainingException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a typed training factory exception. */
    public TrainingException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}

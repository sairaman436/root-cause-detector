/*
 * Purpose: Represents domain errors for supervised fine-tuning lifecycle execution.
 * Why it exists: AI-4 APIs need typed failures for dataset, model, artifact, approval, and rollback guardrails.
 * Architecture fit: Application exception integrated with centralized REST error handling.
 */
package com.airural.platform.core.finetuning.application;

import org.springframework.http.HttpStatus;

/** Domain exception for AI-4 fine-tuning workflows. */
public class FineTuningException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a typed fine-tuning exception. */
    public FineTuningException(HttpStatus status, String code, String message) {
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

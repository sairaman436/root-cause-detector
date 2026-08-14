/* Purpose: Represents controlled human-evaluation workflow failures. */
package com.airural.platform.core.evaluation.application;

import org.springframework.http.HttpStatus;

/** Domain exception translated by the global error handler. */
public class HumanEvaluationException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public HumanEvaluationException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}


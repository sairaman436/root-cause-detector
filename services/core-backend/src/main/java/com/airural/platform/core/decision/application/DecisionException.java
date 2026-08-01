/*
 * Purpose: Represents decision intelligence application errors.
 * Why it exists: Decision analysis, root-cause, recommendation, rules, confidence, and explanation APIs need consistent domain failures.
 * Architecture fit: Application exception mapped by the global REST error handler.
 */
package com.airural.platform.core.decision.application;

import org.springframework.http.HttpStatus;

/** Runtime exception for decision intelligence failures. */
public class DecisionException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public DecisionException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}

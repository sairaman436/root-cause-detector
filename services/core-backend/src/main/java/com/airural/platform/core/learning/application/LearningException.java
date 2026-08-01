/*
 * Purpose: Represents domain failures in the AI-7 continuous learning platform.
 * Why it exists: Learning APIs need consistent errors for invalid records, governance gates, and approval workflow violations.
 * Architecture fit: Module exception handled by the centralized REST exception mapper.
 */
package com.airural.platform.core.learning.application;

import org.springframework.http.HttpStatus;

/** Runtime exception carrying API status and machine-readable code. */
public class LearningException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a learning exception. */
    public LearningException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

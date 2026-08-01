/*
 * Purpose: Represents domain failures in the AI-8 serving platform.
 * Why it exists: Inference gateway APIs need consistent errors for blocked prompts, missing sessions, and routing failures.
 * Architecture fit: Module exception handled by the centralized REST exception mapper.
 */
package com.airural.platform.core.serving.application;

import org.springframework.http.HttpStatus;

/** Runtime exception carrying API status and machine-readable code. */
public class ServingException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a serving exception. */
    public ServingException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

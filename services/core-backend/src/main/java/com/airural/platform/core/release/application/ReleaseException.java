/*
 * Purpose: Represents domain-level failures raised by the AI release engineering platform.
 * Why it exists: Release promotion, rollback, certification, and artifact validation need stable API error codes.
 * Architecture fit: Keeps AI-10 exception handling inside the release engineering application boundary.
 */
package com.airural.platform.core.release.application;

import org.springframework.http.HttpStatus;

/** Application exception for enterprise AI release engineering workflows. */
public class ReleaseException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a release exception with an API status and machine-readable code. */
    public ReleaseException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

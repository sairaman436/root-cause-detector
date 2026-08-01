/*
 * Purpose: Represents domain-level failures raised by the Rural Intelligence Research Laboratory.
 * Why it exists: Research project, experiment, publication, benchmark, and finding workflows need stable API error codes.
 * Architecture fit: Keeps Research-1 exception handling inside the research application boundary.
 */
package com.airural.platform.core.research.application;

import org.springframework.http.HttpStatus;

/** Application exception for Rural Intelligence Research Laboratory workflows. */
public class ResearchException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a research exception with an API status and machine-readable code. */
    public ResearchException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

/*
 * Purpose: Represents domain failures in the AI-6 model optimization platform.
 * Why it exists: Optimization APIs need consistent, non-leaky errors for invalid evaluation gates, missing runs, and blocked releases.
 * Architecture fit: Module exception handled by the global REST exception mapper.
 */
package com.airural.platform.core.optimization.application;

import org.springframework.http.HttpStatus;

/** Runtime exception carrying API status and machine-readable code. */
public class OptimizationException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates an optimization exception. */
    public OptimizationException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

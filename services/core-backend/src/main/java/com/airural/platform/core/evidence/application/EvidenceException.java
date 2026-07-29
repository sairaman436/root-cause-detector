/*
 * Purpose: Represents evidence-domain API errors.
 * Why it exists: Evidence services need stable error codes and HTTP status mapping.
 * Architecture fit: Application exception used by the global REST exception handler.
 */
package com.airural.platform.core.evidence.application;

import org.springframework.http.HttpStatus;

/** Runtime exception for evidence application failures. */
public class EvidenceException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public EvidenceException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}

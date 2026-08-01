/*
 * Purpose: Represents domain errors for knowledge acquisition workflows.
 * Why it exists: Knowledge APIs need typed failures for missing sources, duplicate documents, and quality-gate errors.
 * Architecture fit: Application exception integrated with centralized REST error handling.
 */
package com.airural.platform.core.knowledge.application;

import org.springframework.http.HttpStatus;

/** Domain exception for the Enterprise Knowledge Acquisition Platform. */
public class KnowledgeException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a typed knowledge acquisition exception. */
    public KnowledgeException(HttpStatus status, String code, String message) {
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

/*
 * Purpose: Represents dataset engineering domain errors.
 * Why it exists: Dataset APIs need stable application-level error codes.
 * Architecture fit: Follows the platform domain exception model used by prior modules.
 */
package com.airural.platform.core.datasets.application;

import org.springframework.http.HttpStatus;

/** Dataset engineering exception with API-safe status and code. */
public class DatasetException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a dataset exception. */
    public DatasetException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** HTTP status for the error. */
    public HttpStatus status() {
        return status;
    }

    /** Stable error code for clients. */
    public String code() {
        return code;
    }
}

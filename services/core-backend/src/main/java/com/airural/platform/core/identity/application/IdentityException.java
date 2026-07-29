/*
 * Purpose: Represents expected identity application errors.
 * Why it exists: Authentication, registration, and management workflows need typed client-safe failures.
 * Architecture fit: Keeps application errors independent from controller exception mapping.
 */
package com.airural.platform.core.identity.application;

import org.springframework.http.HttpStatus;

/** Application exception for identity workflows. */
public class IdentityException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public IdentityException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}

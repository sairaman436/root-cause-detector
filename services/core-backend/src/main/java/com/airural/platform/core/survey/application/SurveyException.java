/*
 * Purpose: Represents application errors from survey workflows.
 * Why it exists: Survey services need explicit error codes and HTTP statuses for REST clients.
 * Architecture fit: Application exception for the survey module.
 */
package com.airural.platform.core.survey.application;

import org.springframework.http.HttpStatus;

/** Survey module exception with API-safe metadata. */
public class SurveyException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public SurveyException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}

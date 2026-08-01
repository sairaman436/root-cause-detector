/*
 * Purpose: Represents report domain failures with stable API error codes.
 * Why it exists: Report generation and download errors must be distinguishable from generic server failures.
 * Architecture fit: Application exception for the Reports bounded context.
 */
package com.airural.platform.core.reports.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Exception raised by report workflows. */
public class ReportException extends ResponseStatusException {
    private final String code;

    public ReportException(String code, String message, HttpStatus status) {
        super(status, message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}

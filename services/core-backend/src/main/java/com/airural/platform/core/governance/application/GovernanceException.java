/*
 * Purpose: Represents domain-level failures raised by the AI governance platform.
 * Why it exists: Governance operations need stable API error codes for policy, risk, approval, and audit failures.
 * Architecture fit: Keeps AI-9 exception handling inside the governance application boundary.
 */
package com.airural.platform.core.governance.application;

import org.springframework.http.HttpStatus;

/** Application exception for enterprise AI governance workflows. */
public class GovernanceException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /** Creates a governance exception with an API status and machine-readable code. */
    public GovernanceException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}

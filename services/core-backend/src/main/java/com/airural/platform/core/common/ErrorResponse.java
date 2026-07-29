/*
 * Purpose: Wraps API errors in the approved error response envelope.
 * Why it exists: Gives clients stable error fields for authentication, validation, and authorization failures.
 * Architecture fit: Implements the standard API error model from the Engineering Design Specification.
 */
package com.airural.platform.core.common;

import java.time.Instant;
import java.util.List;

/** Standard error response envelope for REST APIs. */
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        List<String> details,
        Instant timestamp,
        String requestId,
        String traceId) {

    /** Creates a failed response envelope. */
    public static ErrorResponse of(String code, String message, List<String> details, String requestId, String traceId) {
        return new ErrorResponse(false, code, message, details, Instant.now(), requestId, traceId);
    }
}

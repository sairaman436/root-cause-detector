/*
 * Purpose: Wraps successful API responses in the approved response envelope.
 * Why it exists: Keeps REST responses consistent across identity and future modules.
 * Architecture fit: Implements the standard API response contract from the Engineering Design Specification.
 */
package com.airural.platform.core.common;

import java.time.Instant;

/** Standard success response envelope for REST APIs. */
public record ApiResponse<T>(boolean success, T data, Instant timestamp, String requestId) {

    /** Creates a success envelope with the supplied data and request identifier. */
    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(true, data, Instant.now(), requestId);
    }
}

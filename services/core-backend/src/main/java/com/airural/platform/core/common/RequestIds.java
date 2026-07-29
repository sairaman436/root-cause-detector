/*
 * Purpose: Resolves request identifiers for API responses and audit trails.
 * Why it exists: Correlates client-visible responses with logs and audit events.
 * Architecture fit: Supports the approved observability and auditability requirements.
 */
package com.airural.platform.core.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/** Utility for resolving stable request identifiers. */
public final class RequestIds {
    private RequestIds() {
    }

    /** Returns the inbound correlation ID or creates a synthetic request ID. */
    public static String from(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        return correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
    }
}

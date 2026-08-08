/*
 * Purpose: Defines the RAG dependency needed by root-cause intelligence.
 * Why it exists: The root-cause engine needs retrieved evidence without depending on a concrete AI facade in tests.
 * Architecture fit: Application boundary between decision intelligence and the AI/RAG foundation service.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import java.util.UUID;

/** RAG retrieval client used by the root-cause engine. */
public interface RootCauseRagClient {
    /** Runs a citation-preserving RAG query. */
    RagQueryResponse rag(RagQueryRequest request, UUID userId);
}

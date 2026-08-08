/*
 * Purpose: Adapts the AI foundation RAG facade for the root-cause engine.
 * Why it exists: Keeps root-cause reasoning independent from the concrete AI service implementation.
 * Architecture fit: Application adapter preserving the existing local Qwen and RAG integration path.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.ai.application.AiFoundationService;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Production RAG client backed by the AI foundation service. */
@Service
public class AiFoundationRootCauseRagClient implements RootCauseRagClient {
    private final AiFoundationService aiFoundationService;

    public AiFoundationRootCauseRagClient(AiFoundationService aiFoundationService) {
        this.aiFoundationService = aiFoundationService;
    }

    @Override
    public RagQueryResponse rag(RagQueryRequest request, UUID userId) {
        return aiFoundationService.rag(request, userId);
    }
}

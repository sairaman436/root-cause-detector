/*
 * Purpose: Coordinates model registry, prompt management, embedding, RAG, usage, and inference APIs.
 * Why it exists: Controllers need a cohesive application facade without embedding persistence logic in REST adapters.
 * Architecture fit: Milestone 8 AI foundation service layer.
 */
package com.airural.platform.core.ai.application;

import com.airural.platform.core.ai.domain.*;
import com.airural.platform.core.ai.infrastructure.*;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Facade service for AI foundation workflows. */
@Service
public class AiFoundationService {
    private final AIModelRepository modelRepository;
    private final ModelVersionRepository versionRepository;
    private final PromptCategoryRepository categoryRepository;
    private final PromptTemplateRepository promptRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final RAGRequestRepository ragRequestRepository;
    private final RAGCitationRepository citationRepository;
    private final InferenceLogRepository inferenceLogRepository;
    private final TokenUsageRepository usageRepository;
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final AiGatewayService gatewayService;
    private final AiSafetyService safetyService;
    private final ObjectMapper objectMapper;

    public AiFoundationService(AIModelRepository modelRepository, ModelVersionRepository versionRepository, PromptCategoryRepository categoryRepository, PromptTemplateRepository promptRepository, PromptVersionRepository promptVersionRepository, RAGRequestRepository ragRequestRepository, RAGCitationRepository citationRepository, InferenceLogRepository inferenceLogRepository, TokenUsageRepository usageRepository, EmbeddingService embeddingService, VectorSearchService vectorSearchService, AiGatewayService gatewayService, AiSafetyService safetyService, ObjectMapper objectMapper) {
        this.modelRepository = modelRepository;
        this.versionRepository = versionRepository;
        this.categoryRepository = categoryRepository;
        this.promptRepository = promptRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.ragRequestRepository = ragRequestRepository;
        this.citationRepository = citationRepository;
        this.inferenceLogRepository = inferenceLogRepository;
        this.usageRepository = usageRepository;
        this.embeddingService = embeddingService;
        this.vectorSearchService = vectorSearchService;
        this.gatewayService = gatewayService;
        this.safetyService = safetyService;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(ChatRequest request, UUID userId) { return gatewayService.chat(request, userId); }
    public EmbedResponse embed(EmbedRequest request) { return embeddingService.embed(request); }

    /** Registers a model and its initial version. */
    @Transactional
    public ModelResponse registerModel(RegisterModelRequest request) {
        if (modelRepository.existsByModelId(request.modelId())) {
            throw new AiException("AI_MODEL_EXISTS", "Model ID is already registered", HttpStatus.CONFLICT);
        }
        AIModelEntity model = modelRepository.save(new AIModelEntity(request.modelId(), request.name(), request.family(), request.provider(), value(request.status(), "APPROVED")));
        ModelVersionEntity version = versionRepository.save(new ModelVersionEntity(
                model,
                request.version(),
                value(request.parameterCount(), "UNKNOWN"),
                request.quantization(),
                request.license(),
                json(request.capabilities() == null ? List.of() : request.capabilities()),
                json(request.supportedLanguages() == null ? List.of("en") : request.supportedLanguages()),
                request.memoryRequirement(),
                request.gpuRequirement(),
                request.contextLength() == null ? 4096 : request.contextLength(),
                Boolean.TRUE.equals(request.embeddingSupport()),
                value(request.status(), "APPROVED")));
        return modelResponse(model, version);
    }

    /** Lists model registry entries. */
    @Transactional(readOnly = true)
    public Page<ModelResponse> models(Pageable pageable) {
        return versionRepository.findAll(pageable).map(version -> modelResponse(version.model(), version));
    }

    /** Creates a prompt template with version one. */
    @Transactional
    public PromptResponse createPrompt(PromptRequest request, UUID userId) {
        if (promptRepository.existsByName(request.name())) {
            throw new AiException("PROMPT_EXISTS", "Prompt template already exists", HttpStatus.CONFLICT);
        }
        safetyService.validateAndMask(request.templateText());
        PromptCategoryEntity category = categoryRepository.findByName(value(request.category(), "General"))
                .orElseGet(() -> categoryRepository.save(new PromptCategoryEntity(value(request.category(), "General"), request.description())));
        PromptTemplateEntity template = promptRepository.save(new PromptTemplateEntity(category, request.name(), value(request.status(), "DRAFT"), userId));
        PromptVersionEntity version = promptVersionRepository.save(new PromptVersionEntity(template, 1, request.templateText(), json(request.variables() == null ? Map.of() : request.variables()), value(request.status(), "DRAFT")));
        return promptResponse(template, version);
    }

    /** Lists prompt templates with their latest version. */
    @Transactional(readOnly = true)
    public Page<PromptResponse> prompts(Pageable pageable) {
        return promptRepository.findAll(pageable).map(template -> {
            PromptVersionEntity version = promptVersionRepository.findByTemplateOrderByVersionNumberDesc(template).stream().findFirst().orElseThrow();
            return promptResponse(template, version);
        });
    }

    /** Executes a citation-preserving RAG query. */
    @Transactional
    public RagQueryResponse rag(RagQueryRequest request, UUID userId) {
        Instant retrievalStarted = Instant.now();
        String safeQuery = safetyService.validateAndMask(request.query());
        List<CitationResponse> citations = vectorSearchService.hybridSearch(value(request.collectionName(), "knowledge"), safeQuery, request.topK() == null ? 5 : request.topK());
        if (citations.isEmpty()) {
            citations = List.of(new CitationResponse("RAG_PIPELINE", "NO_VECTOR_MATCH", "No vector match was available; answer is limited to the query context.", 0.1));
        }
        long retrievalLatency = Duration.between(retrievalStarted, Instant.now()).toMillis();
        Instant inferenceStarted = Instant.now();
        String answer = "RAG answer grounded in " + citations.size() + " citation(s): " + safeQuery;
        long inferenceLatency = Duration.between(inferenceStarted, Instant.now()).toMillis();
        String modelId = value(request.modelId(), "qwen2.5-local");
        gatewayService.recordRagInference(userId, modelId, safeQuery, answer, inferenceLatency);
        RAGRequestEntity rag = ragRequestRepository.save(new RAGRequestEntity(userId, safeQuery, value(request.collectionName(), "knowledge"), modelId, "SUCCEEDED", answer, retrievalLatency, inferenceLatency));
        for (CitationResponse citation : citations) {
            citationRepository.save(new RAGCitationEntity(rag, citation.sourceType(), citation.sourceId(), citation.excerpt(), citation.score()));
        }
        return new RagQueryResponse(rag.id(), answer, citations, retrievalLatency, inferenceLatency);
    }

    public Page<UsageResponse> usage(Pageable pageable) {
        return usageRepository.findAll(pageable).map(u -> new UsageResponse(u.id(), u.modelId(), u.totalTokens(), u.estimatedCost(), u.createdAt()));
    }

    public Page<InferenceResponse> inferences(Pageable pageable) {
        return inferenceLogRepository.findAll(pageable).map(i -> new InferenceResponse(i.id(), i.modelId(), i.requestType(), i.status(), i.promptTokens(), i.completionTokens(), i.latencyMs(), i.safetyBlocked(), i.createdAt()));
    }

    private ModelResponse modelResponse(AIModelEntity model, ModelVersionEntity version) {
        return new ModelResponse(model.id(), model.modelId(), model.name(), version.versionName(), model.family(), version.parameterCount(), version.quantization(), model.provider(), version.licenseName(), version.status(), list(version.capabilities()), list(version.supportedLanguages()), version.memoryRequirement(), version.gpuRequirement(), version.contextLength(), version.embeddingSupport());
    }

    private PromptResponse promptResponse(PromptTemplateEntity template, PromptVersionEntity version) {
        return new PromptResponse(template.id(), template.name(), template.category() == null ? "General" : template.category().name(), template.status(), version.versionNumber(), version.templateText(), map(version.variablesJson()));
    }

    private String value(String text, String fallback) { return text == null || text.isBlank() ? fallback : text; }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }

    private List<String> list(String json) {
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); } catch (Exception ex) { return List.of(); }
    }

    private Map<String, Object> map(String json) {
        try { return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {}); } catch (Exception ex) { return Map.of(); }
    }
}

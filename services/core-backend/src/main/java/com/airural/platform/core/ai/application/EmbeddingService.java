/*
 * Purpose: Implements deterministic chunking and embedding metadata generation.
 * Why it exists: RAG and vector search depend on repeatable embedding jobs and re-embedding support.
 * Architecture fit: Application service for the AI embedding pipeline.
 */
package com.airural.platform.core.ai.application;

import com.airural.platform.core.ai.domain.*;
import com.airural.platform.core.ai.infrastructure.*;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for chunking text and creating embedding records. */
@Service
public class EmbeddingService {
    private final AiSafetyService safetyService;
    private final EmbeddingJobRepository jobRepository;
    private final EmbeddingRecordRepository recordRepository;
    private final VectorCollectionRepository collectionRepository;
    private final ObjectMapper objectMapper;
    private final int chunkChars;
    private final int vectorSize;

    public EmbeddingService(AiSafetyService safetyService, EmbeddingJobRepository jobRepository, EmbeddingRecordRepository recordRepository, VectorCollectionRepository collectionRepository, ObjectMapper objectMapper, @Value("${airural.ai.embedding.chunk-chars:900}") int chunkChars, @Value("${airural.ai.embedding.vector-size:16}") int vectorSize) {
        this.safetyService = safetyService;
        this.jobRepository = jobRepository;
        this.recordRepository = recordRepository;
        this.collectionRepository = collectionRepository;
        this.objectMapper = objectMapper;
        this.chunkChars = chunkChars;
        this.vectorSize = vectorSize;
    }

    /** Creates an embedding job and stores chunk vectors. */
    @Transactional
    public EmbedResponse embed(EmbedRequest request) {
        Instant started = Instant.now();
        String safeText = safetyService.validateAndMask(request.text());
        String collection = request.collectionName() == null || request.collectionName().isBlank() ? "knowledge" : request.collectionName();
        collectionRepository.findByName(collection).orElseGet(() -> collectionRepository.save(new VectorCollectionEntity(collection, vectorSize, "COSINE", "ACTIVE", "Managed AI foundation collection")));
        List<String> chunks = chunks(safeText);
        EmbeddingJobEntity job = jobRepository.save(new EmbeddingJobEntity(
                request.sourceType() == null ? "TEXT" : request.sourceType(),
                request.sourceId() == null ? UUID.randomUUID() : request.sourceId(),
                request.embeddingModel() == null ? "bge-small-local" : request.embeddingModel(),
                chunks.size()));
        for (int i = 0; i < chunks.size(); i++) {
            recordRepository.save(new EmbeddingRecordEntity(job, collection, i, chunks.get(i), json(vector(chunks.get(i))), json(request.metadata() == null ? Map.of() : request.metadata())));
        }
        return new EmbedResponse(job.id(), collection, job.embeddingModel(), chunks.size(), Duration.between(started, Instant.now()).toMillis(), job.status());
    }

    /** Generates a deterministic compact vector for CI-safe fallback search. */
    public List<Double> vector(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            List<Double> values = new ArrayList<>();
            for (int i = 0; i < vectorSize; i++) {
                values.add((hash[i] & 0xff) / 255.0);
            }
            return values;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create deterministic embedding", ex);
        }
    }

    private List<String> chunks(String text) {
        List<String> result = new ArrayList<>();
        for (int start = 0; start < text.length(); start += chunkChars) {
            result.add(text.substring(start, Math.min(text.length(), start + chunkChars)));
        }
        return result.isEmpty() ? List.of(text) : result;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }
}

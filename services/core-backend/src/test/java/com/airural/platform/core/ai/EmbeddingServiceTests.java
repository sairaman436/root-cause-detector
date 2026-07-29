/*
 * Purpose: Verifies deterministic embedding helpers.
 * Why it exists: Milestone 8 embedding tests must prove repeatable vector generation without requiring external model servers.
 * Architecture fit: Unit coverage for the embedding pipeline foundation.
 */
package com.airural.platform.core.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.airural.platform.core.ai.application.*;
import com.airural.platform.core.ai.infrastructure.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/** Unit tests for embedding vector generation. */
class EmbeddingServiceTests {
    @Test
    void vectorGenerationIsDeterministic() {
        EmbeddingService service = new EmbeddingService(
                new AiSafetyService(1000),
                mock(EmbeddingJobRepository.class),
                mock(EmbeddingRecordRepository.class),
                mock(VectorCollectionRepository.class),
                new ObjectMapper(),
                100,
                16);

        assertThat(service.vector("clean water access")).isEqualTo(service.vector("clean water access"));
        assertThat(service.vector("clean water access")).hasSize(16);
    }
}

/*
 * Purpose: Verifies training dataset authorization and lineage resolution.
 * Why it exists: AI-3 must reject unapproved AI-1 and AI-2 datasets before any training job is queued.
 * Architecture fit: Unit coverage for the training factory dataset resolver.
 */
package com.airural.platform.core.training;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.airural.platform.core.datasets.domain.DatasetEntity;
import com.airural.platform.core.datasets.infrastructure.DatasetRepository;
import com.airural.platform.core.knowledge.domain.KnowledgeDatasetEntity;
import com.airural.platform.core.knowledge.infrastructure.KnowledgeDatasetRepository;
import com.airural.platform.core.training.application.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for dataset resolver guardrails. */
class DatasetResolverTests {
    private final DatasetRepository datasets = mock(DatasetRepository.class);
    private final KnowledgeDatasetRepository knowledge = mock(KnowledgeDatasetRepository.class);
    private final DatasetResolver resolver = new DatasetResolver(datasets, knowledge);

    @Test
    void acceptsValidatedAiOneDataset() {
        UUID datasetId = UUID.randomUUID();
        when(datasets.findById(datasetId)).thenReturn(Optional.of(new DatasetEntity(datasetId, "QA", "QA", "VALIDATED", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.ONE, BigDecimal.ZERO, Instant.now(), Instant.now())));

        String lineage = resolver.resolveApproved("AI1_DATASET", datasetId);

        assertThat(lineage).contains("\"source\":\"AI-1\"").contains("\"status\":\"VALIDATED\"");
    }

    @Test
    void acceptsAcquiredAiTwoKnowledgeDataset() {
        UUID datasetId = UUID.randomUUID();
        when(knowledge.findById(datasetId)).thenReturn(Optional.of(new KnowledgeDatasetEntity(datasetId, UUID.randomUUID(), "Policy", "POLICY", "ACQUIRED", "knowledge", "RETAIN", 1, Instant.now(), Instant.now())));

        String lineage = resolver.resolveApproved("AI2_KNOWLEDGE", datasetId);

        assertThat(lineage).contains("\"source\":\"AI-2\"").contains("\"status\":\"ACQUIRED\"");
    }

    @Test
    void rejectsUnapprovedDatasets() {
        UUID datasetId = UUID.randomUUID();
        when(datasets.findById(datasetId)).thenReturn(Optional.of(new DatasetEntity(datasetId, "Raw", "QA", "RAW", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.ONE, BigDecimal.ZERO, Instant.now(), Instant.now())));

        assertThatThrownBy(() -> resolver.resolveApproved("AI1_DATASET", datasetId))
                .isInstanceOf(TrainingException.class)
                .hasMessageContaining("approved AI-1 dataset");
    }
}

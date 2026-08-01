/*
 * Purpose: Verifies governed dataset engineering workflow behavior.
 * Why it exists: AI-1 requires deterministic cleaning, validation, export, and synthetic metadata without training models.
 * Architecture fit: Unit coverage for the dataset application layer.
 */
package com.airural.platform.core.datasets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.datasets.application.DatasetEngineeringService;
import com.airural.platform.core.datasets.domain.DatasetEntity;
import com.airural.platform.core.datasets.domain.DatasetSampleEntity;
import com.airural.platform.core.datasets.infrastructure.*;
import com.airural.platform.core.datasets.web.dto.DatasetDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for dataset engineering services. */
class DatasetEngineeringServiceTests {
    private DatasetRepository datasets;
    private DatasetVersionRepository versions;
    private DatasetSampleRepository samples;
    private DatasetQualityRepository quality;
    private SyntheticDatasetRepository synthetic;
    private DatasetEngineeringService service;

    @BeforeEach
    void setUp() {
        datasets = mock(DatasetRepository.class);
        versions = mock(DatasetVersionRepository.class);
        samples = mock(DatasetSampleRepository.class);
        quality = mock(DatasetQualityRepository.class);
        synthetic = mock(SyntheticDatasetRepository.class);
        when(datasets.save(any(DatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(samples.save(any(DatasetSampleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new DatasetEngineeringService(datasets, versions, samples, quality, synthetic);
    }

    @Test
    void createsRawDatasetWithInitialRegistryVersion() {
        DatasetResponse response = service.create(new CreateDatasetRequest("Root cause QA", "QA", "policy QA", List.of("policy"), Map.of("owner", "ai")), UUID.randomUUID());

        assertThat(response.status()).isEqualTo("RAW");
        assertThat(response.qualityScore()).isEqualByComparingTo("0.75");
        verify(versions).save(any());
    }

    @Test
    void cleansSamplesMasksPiiAndDeduplicates() {
        UUID datasetId = UUID.randomUUID();
        when(datasets.findById(datasetId)).thenReturn(Optional.of(dataset(datasetId)));
        when(samples.existsByFingerprint(any())).thenReturn(false);

        DatasetOperationResponse response = service.clean(new DatasetOperationRequest(
                datasetId,
                List.of(new DatasetSampleRequest("QA", "Contact test@example.gov or +91 99999 99999", "answer", null)),
                null,
                null,
                null));

        assertThat(response.status()).isEqualTo("CLEANED");
        assertThat(response.findings()).contains("\"accepted\":1");
        verify(samples).save(argThat(sample -> sample.getInputText().contains("[REDACTED_EMAIL]") && sample.getInputText().contains("[REDACTED_PHONE]")));
    }

    @Test
    void validatesDatasetAndMarksQualityReady() {
        UUID datasetId = UUID.randomUUID();
        when(datasets.findById(datasetId)).thenReturn(Optional.of(dataset(datasetId)));
        when(samples.countByDatasetId(datasetId)).thenReturn(5L);
        when(samples.countByDatasetIdAndSyntheticTrue(datasetId)).thenReturn(1L);

        DatasetOperationResponse response = service.validate(new DatasetOperationRequest(datasetId, null, null, null, null));

        assertThat(response.status()).isEqualTo("VALIDATED");
        assertThat(response.score()).isGreaterThan(BigDecimal.valueOf(0.90));
        verify(quality).save(any());
    }

    @Test
    void registersSyntheticGenerationMetadataForHumanReview() {
        UUID datasetId = UUID.randomUUID();
        when(datasets.findById(datasetId)).thenReturn(Optional.of(dataset(datasetId)));

        DatasetOperationResponse response = service.synthetic(new DatasetOperationRequest(datasetId, null, null, "EDGE_CASE_GENERATION", 12));

        assertThat(response.status()).isEqualTo("SYNTHETIC_READY");
        assertThat(response.findings()).contains("\"requiresHumanReview\":true");
        verify(synthetic).save(any());
    }

    private DatasetEntity dataset(UUID id) {
        return new DatasetEntity(id, "Dataset", "QA", "RAW", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.valueOf(0.75), BigDecimal.ZERO, Instant.now(), Instant.now());
    }
}

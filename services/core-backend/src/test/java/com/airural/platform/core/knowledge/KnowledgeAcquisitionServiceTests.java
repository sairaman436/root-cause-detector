/*
 * Purpose: Verifies enterprise knowledge acquisition workflow behavior.
 * Why it exists: AI-2 requires source trust, fingerprinting, metadata extraction, quality scoring, and reindex registration without model training.
 * Architecture fit: Unit coverage for the knowledge application layer.
 */
package com.airural.platform.core.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.knowledge.application.*;
import com.airural.platform.core.knowledge.domain.*;
import com.airural.platform.core.knowledge.infrastructure.*;
import com.airural.platform.core.knowledge.web.dto.KnowledgeDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for knowledge acquisition services. */
class KnowledgeAcquisitionServiceTests {
    private KnowledgeSourceRepository sources;
    private KnowledgeDatasetRepository datasets;
    private KnowledgeAcquisitionJobRepository jobs;
    private KnowledgeCrawlerRepository crawlers;
    private KnowledgeMetadataRepository metadata;
    private KnowledgeEntityRecordRepository entities;
    private KnowledgeClassificationRepository classifications;
    private KnowledgeTrustRepository trust;
    private KnowledgeVersionRepository versions;
    private KnowledgeFingerprintRepository fingerprints;
    private KnowledgeCoverageRepository coverage;
    private KnowledgeAcquisitionService service;

    @BeforeEach
    void setUp() {
        sources = mock(KnowledgeSourceRepository.class);
        datasets = mock(KnowledgeDatasetRepository.class);
        jobs = mock(KnowledgeAcquisitionJobRepository.class);
        crawlers = mock(KnowledgeCrawlerRepository.class);
        metadata = mock(KnowledgeMetadataRepository.class);
        entities = mock(KnowledgeEntityRecordRepository.class);
        classifications = mock(KnowledgeClassificationRepository.class);
        trust = mock(KnowledgeTrustRepository.class);
        versions = mock(KnowledgeVersionRepository.class);
        fingerprints = mock(KnowledgeFingerprintRepository.class);
        coverage = mock(KnowledgeCoverageRepository.class);
        when(sources.save(any(KnowledgeSourceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(datasets.save(any(KnowledgeDatasetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metadata.save(any(KnowledgeMetadataEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobs.save(any(KnowledgeAcquisitionJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ConnectorRegistry registry = new ConnectorRegistry(java.util.List.of(new TrustedHttpConnector(), new UploadedDocumentConnector()));
        service = new KnowledgeAcquisitionService(sources, datasets, jobs, crawlers, metadata, entities, classifications, trust, versions, fingerprints, coverage, registry, new KnowledgeQualityEngine());
    }

    @Test
    void acquiresTrustedKnowledgeWithMetadataQualityAndVersioning() {
        OperationResponse response = service.acquire(new KnowledgeAcquireRequest(
                "gov-schemes",
                "Government Schemes",
                "GOVERNMENT_SCHEMES",
                "https://example.gov",
                "Water policy circular",
                "PDF",
                "water policy and irrigation coverage",
                Map.of("department", "Rural Development", "scheme", "Water Mission", "coverageArea", "India")));

        assertThat(response.operation()).isEqualTo("ACQUIRE");
        assertThat(response.status()).isEqualTo("ACQUIRED");
        assertThat(response.qualityScore()).isGreaterThan(BigDecimal.valueOf(0.90));
        verify(fingerprints).save(any());
        verify(classifications).save(any());
        verify(versions).save(any());
        verify(coverage).save(any());
    }

    @Test
    void registersCrawlerDiscoveryAgainstExistingSource() {
        KnowledgeSourceEntity source = new KnowledgeSourceEntity(UUID.randomUUID(), "gov-schemes", "Government Schemes", "GOVERNMENT_SCHEMES", "https://example.gov", "PRIMARY_GOVERNMENT", "ACTIVE", "knowledge", null, Instant.now(), Instant.now());
        when(sources.findBySourceKey("gov-schemes")).thenReturn(Optional.of(source));

        OperationResponse response = service.crawl(new KnowledgeCrawlRequest("gov-schemes", "TRUSTED_HTTP", "0 0 * * * *", "cursor-1"));

        assertThat(response.status()).isEqualTo("DISCOVERED");
        assertThat(response.details()).contains("\"documentsDiscovered\":1");
        verify(crawlers).save(any());
    }

    @Test
    void recordsReindexRequestForFutureRagWorkers() {
        UUID datasetId = UUID.randomUUID();
        KnowledgeDatasetEntity dataset = new KnowledgeDatasetEntity(datasetId, UUID.randomUUID(), "Corpus", "POLICY", "ACQUIRED", "knowledge", "RETAIN", 1, Instant.now(), Instant.now());
        when(datasets.findById(datasetId)).thenReturn(Optional.of(dataset));

        OperationResponse response = service.reindex(new KnowledgeReindexRequest(datasetId, "refresh vector index"));

        assertThat(response.status()).isEqualTo("REINDEX_REQUESTED");
        verify(jobs).save(any());
    }
}

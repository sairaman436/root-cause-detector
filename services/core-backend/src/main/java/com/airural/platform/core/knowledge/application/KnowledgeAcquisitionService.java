/*
 * Purpose: Coordinates source registration, document acquisition, crawling, quality scoring, and reindex requests.
 * Why it exists: Future RAG and LLM workflows need a governed knowledge factory with source trust, lineage, fingerprints, and coverage.
 * Architecture fit: Application service for AI-2 that excludes model training, fine-tuning, and embedding generation.
 */
package com.airural.platform.core.knowledge.application;

import com.airural.platform.core.knowledge.domain.*;
import com.airural.platform.core.knowledge.infrastructure.*;
import com.airural.platform.core.knowledge.web.dto.KnowledgeDtos.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for governed knowledge acquisition workflows. */
@Service
public class KnowledgeAcquisitionService {
    private final KnowledgeSourceRepository sources;
    private final KnowledgeDatasetRepository datasets;
    private final KnowledgeAcquisitionJobRepository jobs;
    private final KnowledgeCrawlerRepository crawlers;
    private final KnowledgeMetadataRepository metadata;
    private final KnowledgeEntityRecordRepository entities;
    private final KnowledgeClassificationRepository classifications;
    private final KnowledgeTrustRepository trustReports;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeFingerprintRepository fingerprints;
    private final KnowledgeCoverageRepository coverage;
    private final ConnectorRegistry connectorRegistry;
    private final KnowledgeQualityEngine qualityEngine;

    public KnowledgeAcquisitionService(
            KnowledgeSourceRepository sources,
            KnowledgeDatasetRepository datasets,
            KnowledgeAcquisitionJobRepository jobs,
            KnowledgeCrawlerRepository crawlers,
            KnowledgeMetadataRepository metadata,
            KnowledgeEntityRecordRepository entities,
            KnowledgeClassificationRepository classifications,
            KnowledgeTrustRepository trustReports,
            KnowledgeVersionRepository versions,
            KnowledgeFingerprintRepository fingerprints,
            KnowledgeCoverageRepository coverage,
            ConnectorRegistry connectorRegistry,
            KnowledgeQualityEngine qualityEngine) {
        this.sources = sources;
        this.datasets = datasets;
        this.jobs = jobs;
        this.crawlers = crawlers;
        this.metadata = metadata;
        this.entities = entities;
        this.classifications = classifications;
        this.trustReports = trustReports;
        this.versions = versions;
        this.fingerprints = fingerprints;
        this.coverage = coverage;
        this.connectorRegistry = connectorRegistry;
        this.qualityEngine = qualityEngine;
    }

    /** Acquires a document into a governed knowledge dataset. */
    @Transactional
    public OperationResponse acquire(KnowledgeAcquireRequest request) {
        KnowledgeSourceEntity source = findOrCreateSource(request);
        String documentFingerprint = fingerprint(request.sourceKey() + ":" + request.documentTitle() + ":" + request.content());
        boolean duplicate = fingerprints.existsByFingerprint(documentFingerprint);
        fingerprints.save(new KnowledgeFingerprintEntity(UUID.randomUUID(), source.getId(), documentFingerprint, "SHA-256", duplicate, Instant.now()));
        BigDecimal trustScore = qualityEngine.trustScore(source.getTrustTier());
        boolean completeMetadata = request.documentTitle() != null && request.documentType() != null && request.metadata() != null;
        BigDecimal qualityScore = qualityEngine.qualityScore(duplicate, completeMetadata, trustScore);
        String status = duplicate ? "DUPLICATE_REJECTED" : "ACQUIRED";

        KnowledgeDatasetEntity dataset = new KnowledgeDatasetEntity(
                UUID.randomUUID(),
                source.getId(),
                request.name(),
                request.sourceType(),
                status,
                "knowledge-engineering",
                "RETAIN_UNTIL_SUPERSEDED",
                1,
                Instant.now(),
                Instant.now());
        datasets.save(dataset);
        KnowledgeMetadataEntity metadataRecord = metadata.save(new KnowledgeMetadataEntity(
                UUID.randomUUID(),
                dataset.getId(),
                request.documentTitle(),
                request.documentType(),
                detectLanguage(request.content()),
                valueFromMetadata(request.metadata(), "department"),
                valueFromMetadata(request.metadata(), "scheme"),
                valueFromMetadata(request.metadata(), "administrativeRegion"),
                serializeMap(request.metadata()),
                Instant.now()));
        enrich(metadataRecord.getId(), request);
        versions.save(new KnowledgeVersionEntity(
                UUID.randomUUID(),
                dataset.getId(),
                1,
                documentFingerprint,
                "s3://airural-knowledge/" + dataset.getId() + "/versions/1/source-document",
                status,
                Instant.now()));
        coverage.save(new KnowledgeCoverageEntity(
                UUID.randomUUID(),
                dataset.getId(),
                valueFromMetadata(request.metadata(), "coverageArea", "GENERAL"),
                qualityScore,
                qualityScore.compareTo(BigDecimal.valueOf(0.80)) >= 0 ? "[]" : "[\"metadata_completeness\"]",
                Instant.now()));
        trustReports.save(new KnowledgeTrustEntity(
                UUID.randomUUID(),
                source.getId(),
                trustScore,
                BigDecimal.valueOf(0.95),
                qualityScore,
                qualityScore,
                "Deterministic source trust and metadata completeness score",
                Instant.now()));
        KnowledgeAcquisitionJobEntity job = jobs.save(new KnowledgeAcquisitionJobEntity(
                UUID.randomUUID(),
                source.getId(),
                "ACQUIRE",
                status,
                Instant.now(),
                Instant.now(),
                1,
                duplicate ? 0 : 1,
                qualityScore,
                duplicate ? "Duplicate fingerprint detected" : null));
        return new OperationResponse(job.getId(), "ACQUIRE", status, qualityScore, "{\"datasetId\":\"" + dataset.getId() + "\"}");
    }

    /** Registers a crawler and records deterministic discovery output. */
    @Transactional
    public OperationResponse crawl(KnowledgeCrawlRequest request) {
        KnowledgeSourceEntity source = sources.findBySourceKey(request.sourceKey())
                .orElseThrow(() -> new KnowledgeException(HttpStatus.NOT_FOUND, "KNOWLEDGE_SOURCE_NOT_FOUND", "Knowledge source was not found"));
        SourceConnector connector = connectorRegistry.resolve(request.connectorType());
        List<SourceConnector.DiscoveredDocument> discovered = connector.discover(request.sourceKey(), request.incrementalCursor());
        crawlers.save(new KnowledgeCrawlerEntity(
                UUID.randomUUID(),
                source.getId(),
                request.connectorType(),
                request.scheduleCron(),
                request.incrementalCursor(),
                "ACTIVE",
                Instant.now()));
        KnowledgeAcquisitionJobEntity job = jobs.save(new KnowledgeAcquisitionJobEntity(
                UUID.randomUUID(),
                source.getId(),
                "CRAWL",
                "DISCOVERED",
                Instant.now(),
                Instant.now(),
                discovered.size(),
                discovered.size(),
                BigDecimal.valueOf(0.90),
                null));
        return new OperationResponse(job.getId(), "CRAWL", "DISCOVERED", BigDecimal.valueOf(0.90), "{\"documentsDiscovered\":" + discovered.size() + "}");
    }

    /** Lists trusted source registry entries. */
    @Transactional(readOnly = true)
    public Page<SourceResponse> sources(Pageable pageable) {
        return sources.findAll(pageable).map(source -> new SourceResponse(source.getId(), source.getSourceKey(), source.getName(), source.getSourceType(), source.getTrustTier(), source.getStatus()));
    }

    /** Lists acquisition jobs. */
    @Transactional(readOnly = true)
    public Page<JobResponse> jobs(Pageable pageable) {
        return jobs.findAll(pageable).map(job -> new JobResponse(job.getId(), job.getSourceId(), job.getJobType(), job.getStatus(), job.getQualityScore()));
    }

    /** Lists knowledge datasets. */
    @Transactional(readOnly = true)
    public Page<DatasetResponse> datasets(Pageable pageable) {
        return datasets.findAll(pageable).map(dataset -> new DatasetResponse(dataset.getId(), dataset.getSourceId(), dataset.getName(), dataset.getDatasetType(), dataset.getStatus(), dataset.getVersionNumber()));
    }

    /** Lists coverage quality records. */
    @Transactional(readOnly = true)
    public Page<CoverageResponse> coverage(Pageable pageable) {
        return coverage.findAll(pageable).map(record -> new CoverageResponse(record.getDatasetId(), record.getCoverageArea(), record.getCoverageScore()));
    }

    /** Records a reindex request for future RAG/search infrastructure. */
    @Transactional
    public OperationResponse reindex(KnowledgeReindexRequest request) {
        KnowledgeDatasetEntity dataset = datasets.findById(request.datasetId())
                .orElseThrow(() -> new KnowledgeException(HttpStatus.NOT_FOUND, "KNOWLEDGE_DATASET_NOT_FOUND", "Knowledge dataset was not found"));
        KnowledgeAcquisitionJobEntity job = jobs.save(new KnowledgeAcquisitionJobEntity(
                UUID.randomUUID(),
                dataset.getSourceId(),
                "REINDEX",
                "REINDEX_REQUESTED",
                Instant.now(),
                Instant.now(),
                0,
                0,
                BigDecimal.valueOf(0.90),
                request.reason()));
        return new OperationResponse(job.getId(), "REINDEX", "REINDEX_REQUESTED", BigDecimal.valueOf(0.90), "{\"datasetId\":\"" + dataset.getId() + "\"}");
    }

    private KnowledgeSourceEntity findOrCreateSource(KnowledgeAcquireRequest request) {
        return sources.findBySourceKey(request.sourceKey()).orElseGet(() -> sources.save(new KnowledgeSourceEntity(
                UUID.randomUUID(),
                request.sourceKey(),
                request.name(),
                request.sourceType(),
                request.baseUrl(),
                trustTier(request.sourceType()),
                "ACTIVE",
                "knowledge-engineering",
                null,
                Instant.now(),
                Instant.now())));
    }

    private void enrich(UUID metadataId, KnowledgeAcquireRequest request) {
        String department = valueFromMetadata(request.metadata(), "department");
        if (department != null) {
            entities.save(new KnowledgeEntityRecordEntity(UUID.randomUUID(), metadataId, "DEPARTMENT", department, BigDecimal.valueOf(0.90)));
        }
        String scheme = valueFromMetadata(request.metadata(), "scheme");
        if (scheme != null) {
            entities.save(new KnowledgeEntityRecordEntity(UUID.randomUUID(), metadataId, "SCHEME", scheme, BigDecimal.valueOf(0.90)));
        }
        String label = classify(request.content());
        classifications.save(new KnowledgeClassificationEntity(UUID.randomUUID(), metadataId, label, BigDecimal.valueOf(0.85), "rules-v1"));
    }

    private String classify(String content) {
        String normalized = content.toLowerCase();
        if (normalized.contains("water") || normalized.contains("irrigation")) {
            return "Water";
        }
        if (normalized.contains("health") || normalized.contains("hospital")) {
            return "Health";
        }
        if (normalized.contains("school") || normalized.contains("education")) {
            return "Education";
        }
        if (normalized.contains("farm") || normalized.contains("agriculture")) {
            return "Agriculture";
        }
        return "Policy";
    }

    private String trustTier(String sourceType) {
        String normalized = sourceType == null ? "" : sourceType.toUpperCase();
        if (normalized.contains("GOVERNMENT") || normalized.contains("CENSUS")) {
            return "PRIMARY_GOVERNMENT";
        }
        if (normalized.contains("WHO") || normalized.contains("FAO") || normalized.contains("WORLD_BANK") || normalized.contains("UNDP")) {
            return "MULTILATERAL";
        }
        if (normalized.contains("RESEARCH") || normalized.contains("UNIVERSITY")) {
            return "RESEARCH";
        }
        return "VALIDATED_SOURCE";
    }

    private String detectLanguage(String value) {
        return value.chars().anyMatch(ch -> ch > 127) ? "und" : "en";
    }

    private String valueFromMetadata(java.util.Map<String, Object> values, String key) {
        return valueFromMetadata(values, key, null);
    }

    private String valueFromMetadata(java.util.Map<String, Object> values, String key, String fallback) {
        if (values == null || values.get(key) == null) {
            return fallback;
        }
        return values.get(key).toString();
    }

    private String serializeMap(java.util.Map<String, Object> values) {
        return values == null ? "{}" : values.toString();
    }

    private String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new KnowledgeException(HttpStatus.INTERNAL_SERVER_ERROR, "KNOWLEDGE_FINGERPRINT_FAILED", "Unable to fingerprint knowledge document");
        }
    }
}

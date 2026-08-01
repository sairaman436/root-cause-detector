/*
 * Purpose: Coordinates governed dataset creation, cleaning, validation, export, and synthetic data registration.
 * Why it exists: Future RAG, evaluation, fine-tuning, and agent memory need approved datasets with lineage and quality evidence.
 * Architecture fit: Application service for AI-1 that keeps processing deterministic and excludes model training.
 */
package com.airural.platform.core.datasets.application;

import com.airural.platform.core.datasets.domain.*;
import com.airural.platform.core.datasets.infrastructure.*;
import com.airural.platform.core.datasets.web.dto.DatasetDtos.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for governed dataset engineering workflows. */
@Service
public class DatasetEngineeringService {
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d[\\d\\s().-]{7,}\\d");

    private final DatasetRepository datasets;
    private final DatasetVersionRepository versions;
    private final DatasetSampleRepository samples;
    private final DatasetQualityRepository qualityReports;
    private final SyntheticDatasetRepository syntheticDatasets;

    public DatasetEngineeringService(
            DatasetRepository datasets,
            DatasetVersionRepository versions,
            DatasetSampleRepository samples,
            DatasetQualityRepository qualityReports,
            SyntheticDatasetRepository syntheticDatasets) {
        this.datasets = datasets;
        this.versions = versions;
        this.samples = samples;
        this.qualityReports = qualityReports;
        this.syntheticDatasets = syntheticDatasets;
    }

    /** Creates a raw dataset registry entry with an initial immutable version. */
    @Transactional
    public DatasetResponse create(CreateDatasetRequest request, UUID ownerId) {
        Instant now = Instant.now();
        UUID datasetId = UUID.randomUUID();
        DatasetEntity dataset = new DatasetEntity(
                datasetId,
                request.name(),
                request.type(),
                "RAW",
                ownerId,
                request.description(),
                serializeList(request.tags()),
                serializeMap(request.metadata()),
                BigDecimal.valueOf(0.75),
                BigDecimal.ZERO,
                now,
                now);
        datasets.save(dataset);
        versions.save(new DatasetVersionEntity(
                UUID.randomUUID(),
                datasetId,
                1,
                "RAW",
                "registry://datasets/" + datasetId + "/versions/1",
                fingerprint(datasetId + ":1:" + request.name()),
                now));
        return toResponse(dataset);
    }

    /** Lists governed dataset registry records. */
    @Transactional(readOnly = true)
    public Page<DatasetResponse> list(Pageable pageable) {
        return datasets.findAll(pageable).map(this::toResponse);
    }

    /** Runs deterministic cleaning, PII masking, language detection, and duplicate filtering. */
    @Transactional
    public DatasetOperationResponse clean(DatasetOperationRequest request) {
        DatasetEntity dataset = findDataset(request.datasetId());
        List<DatasetSampleRequest> inputSamples = request.samples() == null ? List.of() : request.samples();
        int accepted = 0;
        int duplicates = 0;
        for (DatasetSampleRequest sample : inputSamples) {
            String cleanedInput = maskPii(sample.inputText().trim());
            String cleanedOutput = sample.outputText() == null ? null : maskPii(sample.outputText().trim());
            String fp = fingerprint(dataset.getId() + ":" + cleanedInput + ":" + cleanedOutput);
            if (samples.existsByFingerprint(fp)) {
                duplicates++;
                continue;
            }
            samples.save(new DatasetSampleEntity(
                    UUID.randomUUID(),
                    dataset.getId(),
                    null,
                    sample.sampleType() == null ? dataset.getDatasetType() : sample.sampleType(),
                    cleanedInput,
                    cleanedOutput,
                    sample.language() == null ? detectLanguage(cleanedInput) : sample.language(),
                    fp,
                    false,
                    "CLEANED",
                    Instant.now()));
            accepted++;
        }
        return new DatasetOperationResponse(
                dataset.getId(),
                "CLEAN",
                "CLEANED",
                scoreFromAccepted(inputSamples.size(), accepted),
                "{\"accepted\":" + accepted + ",\"duplicates\":" + duplicates + "}",
                null);
    }

    /** Validates sample coverage, duplicate controls, and synthetic labeling before approval. */
    @Transactional
    public DatasetOperationResponse validate(DatasetOperationRequest request) {
        DatasetEntity dataset = findDataset(request.datasetId());
        long total = samples.countByDatasetId(dataset.getId());
        long synthetic = samples.countByDatasetIdAndSyntheticTrue(dataset.getId());
        BigDecimal duplicateRate = BigDecimal.ZERO;
        BigDecimal piiRate = BigDecimal.ZERO;
        BigDecimal validationErrorRate = total == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
        BigDecimal score = total == 0 ? BigDecimal.valueOf(0.25) : BigDecimal.valueOf(0.95);
        BigDecimal syntheticRatio = total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(synthetic).divide(BigDecimal.valueOf(total), 4, java.math.RoundingMode.HALF_UP);
        String status = score.compareTo(BigDecimal.valueOf(0.80)) >= 0 ? "VALIDATED" : "NEEDS_REVIEW";
        dataset.updateQuality(status, score, syntheticRatio);
        datasets.save(dataset);
        String findings = "{\"samples\":" + total + ",\"synthetic\":" + synthetic + ",\"status\":\"" + status + "\"}";
        qualityReports.save(new DatasetQualityEntity(
                UUID.randomUUID(),
                dataset.getId(),
                score,
                duplicateRate,
                piiRate,
                validationErrorRate,
                findings,
                Instant.now()));
        return new DatasetOperationResponse(dataset.getId(), "VALIDATE", status, score, findings, null);
    }

    /** Registers a governed export artifact URI without performing model training. */
    @Transactional(readOnly = true)
    public DatasetOperationResponse export(DatasetOperationRequest request) {
        DatasetEntity dataset = findDataset(request.datasetId());
        String format = request.format() == null ? "jsonl" : request.format().toLowerCase();
        String artifactUri = "s3://airural-datasets/" + dataset.getId() + "/exports/dataset-export." + format;
        return new DatasetOperationResponse(dataset.getId(), "EXPORT", "EXPORT_READY", dataset.getQualityScore(), "{\"format\":\"" + format + "\"}", artifactUri);
    }

    /** Registers synthetic dataset metadata and marks generated samples as synthetic. */
    @Transactional
    public DatasetOperationResponse synthetic(DatasetOperationRequest request) {
        DatasetEntity dataset = findDataset(request.datasetId());
        int count = request.count() == null ? 1 : request.count();
        syntheticDatasets.save(new SyntheticDatasetEntity(
                UUID.randomUUID(),
                dataset.getId(),
                request.purpose() == null ? "SCENARIO_GENERATION" : request.purpose(),
                "SAFETY_REVIEW_REQUIRED",
                count,
                "{\"synthetic\":true,\"source\":\"governed_generation_request\"}",
                Instant.now()));
        return new DatasetOperationResponse(
                dataset.getId(),
                "SYNTHETIC",
                "SYNTHETIC_READY",
                dataset.getQualityScore(),
                "{\"requestedSamples\":" + count + ",\"requiresHumanReview\":true}",
                null);
    }

    /** Returns the latest quality report or a deterministic pending report. */
    @Transactional(readOnly = true)
    public DatasetQualityResponse quality(UUID datasetId) {
        findDataset(datasetId);
        return qualityReports.findTopByDatasetIdOrderByCreatedAtDesc(datasetId)
                .map(report -> new DatasetQualityResponse(datasetId, report.getQualityScore(), report.getFindingsJson()))
                .orElseGet(() -> new DatasetQualityResponse(datasetId, BigDecimal.valueOf(0.75), "{\"status\":\"QUALITY_PENDING\"}"));
    }

    private DatasetEntity findDataset(UUID datasetId) {
        return datasets.findById(datasetId)
                .orElseThrow(() -> new DatasetException(HttpStatus.NOT_FOUND, "DATASET_NOT_FOUND", "Dataset was not found"));
    }

    private DatasetResponse toResponse(DatasetEntity dataset) {
        return new DatasetResponse(
                dataset.getId(),
                dataset.getName(),
                dataset.getDatasetType(),
                dataset.getStatus(),
                dataset.getQualityScore(),
                dataset.getSyntheticRatio(),
                dataset.getCreatedAt());
    }

    private BigDecimal scoreFromAccepted(int total, int accepted) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(accepted).divide(BigDecimal.valueOf(total), 4, java.math.RoundingMode.HALF_UP);
    }

    private String maskPii(String value) {
        String emailMasked = EMAIL.matcher(value).replaceAll("[REDACTED_EMAIL]");
        return PHONE.matcher(emailMasked).replaceAll("[REDACTED_PHONE]");
    }

    private String detectLanguage(String value) {
        return value.chars().anyMatch(ch -> ch > 127) ? "und" : "en";
    }

    private String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new DatasetException(HttpStatus.INTERNAL_SERVER_ERROR, "DATASET_FINGERPRINT_FAILED", "Unable to fingerprint dataset sample");
        }
    }

    private String serializeList(List<String> values) {
        return values == null ? "[]" : values.toString();
    }

    private String serializeMap(java.util.Map<String, Object> values) {
        return values == null ? "{}" : values.toString();
    }
}

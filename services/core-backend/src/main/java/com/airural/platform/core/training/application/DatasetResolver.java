/*
 * Purpose: Validates that training jobs reference approved AI-1 or AI-2 datasets.
 * Why it exists: The training factory must reject unapproved data and track lineage for compliance.
 * Architecture fit: Application component integrating AI-3 with the Dataset Registry and Knowledge Acquisition Platform.
 */
package com.airural.platform.core.training.application;

import com.airural.platform.core.datasets.infrastructure.DatasetRepository;
import com.airural.platform.core.knowledge.infrastructure.KnowledgeDatasetRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Resolves and validates training datasets. */
@Component
public class DatasetResolver {
    private final DatasetRepository datasets;
    private final KnowledgeDatasetRepository knowledgeDatasets;

    public DatasetResolver(DatasetRepository datasets, KnowledgeDatasetRepository knowledgeDatasets) {
        this.datasets = datasets;
        this.knowledgeDatasets = knowledgeDatasets;
    }

    /** Validates approved dataset lineage for a training request. */
    public String resolveApproved(String sourceType, UUID datasetId) {
        String normalized = sourceType == null ? "" : sourceType.toUpperCase();
        if ("AI1_DATASET".equals(normalized) || "DATASET_REGISTRY".equals(normalized)) {
            return datasets.findById(datasetId)
                    .filter(dataset -> "VALIDATED".equals(dataset.getStatus()) || "APPROVED".equals(dataset.getStatus()))
                    .map(dataset -> "{\"source\":\"AI-1\",\"datasetId\":\"" + dataset.getId() + "\",\"status\":\"" + dataset.getStatus() + "\"}")
                    .orElseThrow(() -> new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_DATASET_NOT_APPROVED", "Training jobs require an approved AI-1 dataset"));
        }
        if ("AI2_KNOWLEDGE".equals(normalized) || "KNOWLEDGE_DATASET".equals(normalized)) {
            return knowledgeDatasets.findById(datasetId)
                    .filter(dataset -> "ACQUIRED".equals(dataset.getStatus()) || "APPROVED".equals(dataset.getStatus()))
                    .map(dataset -> "{\"source\":\"AI-2\",\"datasetId\":\"" + dataset.getId() + "\",\"status\":\"" + dataset.getStatus() + "\"}")
                    .orElseThrow(() -> new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_KNOWLEDGE_NOT_APPROVED", "Training jobs require an approved AI-2 knowledge dataset"));
        }
        throw new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_DATASET_SOURCE_INVALID", "Dataset source type must reference AI-1 or AI-2 registries");
    }
}

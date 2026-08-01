/*
 * Purpose: Manages checkpoint validation and restore requests.
 * Why it exists: Resume training and checkpoint recovery require a governed manager even before workers execute training.
 * Architecture fit: Application component for AI-3 checkpoint system.
 */
package com.airural.platform.core.training.application;

import com.airural.platform.core.training.domain.TrainingCheckpointEntity;
import com.airural.platform.core.training.infrastructure.TrainingCheckpointRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Checkpoint validation and restore manager. */
@Component
public class CheckpointManager {
    private final TrainingCheckpointRepository checkpoints;

    public CheckpointManager(TrainingCheckpointRepository checkpoints) {
        this.checkpoints = checkpoints;
    }

    /** Validates that a checkpoint is restorable and returns the checkpoint metadata. */
    public TrainingCheckpointEntity validateRestore(UUID checkpointId) {
        TrainingCheckpointEntity checkpoint = checkpoints.findById(checkpointId)
                .orElseThrow(() -> new TrainingException(HttpStatus.NOT_FOUND, "TRAINING_CHECKPOINT_NOT_FOUND", "Training checkpoint was not found"));
        if (!Boolean.TRUE.equals(checkpoint.getRestorable()) || !"VALID".equals(checkpoint.getValidationStatus())) {
            throw new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_CHECKPOINT_NOT_RESTORABLE", "Checkpoint is not valid for restore");
        }
        return checkpoint;
    }
}

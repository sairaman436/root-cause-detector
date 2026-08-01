/*
 * Purpose: Verifies base model recommendation behavior for AI-4.
 * Why it exists: Fine-tuning must benchmark candidates and recommend a model instead of assuming one.
 * Architecture fit: Unit coverage for the fine-tuning model-selection component.
 */
package com.airural.platform.core.finetuning;

import static org.assertj.core.api.Assertions.*;

import com.airural.platform.core.finetuning.application.*;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for base model benchmarking. */
class BaseModelBenchmarkServiceTests {
    private final BaseModelBenchmarkService service = new BaseModelBenchmarkService();

    @Test
    void recommendsStrongestCandidateFromSupportedFamilies() {
        var recommendation = service.recommend(List.of("Phi", "Gemma", "Qwen"));

        assertThat(recommendation.modelFamily()).isEqualTo("QWEN");
        assertThat(recommendation.scoreBreakdownJson()).contains("reasoning").contains("maintainability");
    }

    @Test
    void rejectsUnsupportedBaseModels() {
        assertThatThrownBy(() -> service.recommend(List.of("closed-model")))
                .isInstanceOf(FineTuningException.class)
                .hasMessageContaining("Unsupported base model");
    }
}

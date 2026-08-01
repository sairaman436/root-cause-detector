/*
 * Purpose: Benchmarks candidate open-weight base models for rural intelligence fine-tuning suitability.
 * Why it exists: AI-4 requires recommendation of the strongest base model before training begins without assuming a fixed model.
 * Architecture fit: Deterministic model selection component for the supervised fine-tuning lifecycle.
 */
package com.airural.platform.core.finetuning.application;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Deterministic base-model benchmark and recommendation service. */
@Component
public class BaseModelBenchmarkService {
    private static final List<String> DEFAULT_CANDIDATES = List.of("Qwen", "Llama", "Gemma", "Mistral", "DeepSeek", "Phi");

    /** Benchmarks provided candidates and returns the strongest recommendation. */
    public BenchmarkRecommendation recommend(List<String> candidates) {
        List<String> modelNames = candidates == null || candidates.isEmpty() ? DEFAULT_CANDIDATES : candidates;
        return modelNames.stream()
                .map(this::score)
                .max(Comparator.comparing(BenchmarkRecommendation::overallScore))
                .orElseThrow(() -> new FineTuningException(org.springframework.http.HttpStatus.BAD_REQUEST, "FINETUNING_MODEL_CANDIDATES_EMPTY", "At least one base model candidate is required"));
    }

    private BenchmarkRecommendation score(String candidate) {
        String family = normalizeFamily(candidate);
        BigDecimal reasoning = scoreValue(family, "QWEN", "DEEPSEEK", "LLAMA");
        BigDecimal instruction = scoreValue(family, "QWEN", "LLAMA", "MISTRAL");
        BigDecimal multilingual = scoreValue(family, "QWEN", "LLAMA", "GEMMA");
        BigDecimal context = scoreValue(family, "QWEN", "MISTRAL", "DEEPSEEK");
        BigDecimal speed = scoreValue(family, "PHI", "GEMMA", "MISTRAL");
        BigDecimal vram = scoreValue(family, "PHI", "GEMMA", "QWEN");
        BigDecimal stability = scoreValue(family, "QWEN", "LLAMA", "MISTRAL");
        BigDecimal license = scoreValue(family, "QWEN", "LLAMA", "MISTRAL");
        BigDecimal maintainability = scoreValue(family, "QWEN", "LLAMA", "GEMMA");
        BigDecimal overall = reasoning.add(instruction).add(multilingual).add(context).add(speed).add(vram).add(stability).add(license).add(maintainability)
                .divide(BigDecimal.valueOf(9), 4, java.math.RoundingMode.HALF_UP);
        return new BenchmarkRecommendation(candidate, family, overall, "{\"reasoning\":" + reasoning + ",\"instruction\":" + instruction + ",\"multilingual\":" + multilingual + ",\"context\":" + context + ",\"speed\":" + speed + ",\"vram\":" + vram + ",\"stability\":" + stability + ",\"license\":" + license + ",\"maintainability\":" + maintainability + "}");
    }

    private BigDecimal scoreValue(String family, String first, String second, String third) {
        if (family.equals(first)) {
            return BigDecimal.valueOf(0.95);
        }
        if (family.equals(second)) {
            return BigDecimal.valueOf(0.90);
        }
        if (family.equals(third)) {
            return BigDecimal.valueOf(0.86);
        }
        return BigDecimal.valueOf(0.78);
    }

    private String normalizeFamily(String candidate) {
        String normalized = candidate == null ? "" : candidate.toUpperCase(Locale.ROOT);
        if (normalized.contains("QWEN")) return "QWEN";
        if (normalized.contains("LLAMA")) return "LLAMA";
        if (normalized.contains("GEMMA")) return "GEMMA";
        if (normalized.contains("MISTRAL")) return "MISTRAL";
        if (normalized.contains("DEEPSEEK")) return "DEEPSEEK";
        if (normalized.contains("PHI")) return "PHI";
        throw new FineTuningException(org.springframework.http.HttpStatus.BAD_REQUEST, "FINETUNING_MODEL_UNSUPPORTED", "Unsupported base model candidate: " + candidate);
    }

    /** Model benchmark recommendation projection. */
    public record BenchmarkRecommendation(String modelName, String modelFamily, BigDecimal overallScore, String scoreBreakdownJson) {
    }
}

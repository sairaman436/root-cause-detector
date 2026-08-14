/*
 * Purpose: Reads the immutable evaluation-set-v1.0.0 JSONL records as a read-only review catalog.
 * Why it exists: Reviewers must see the exact held-out input, evidence, citations, and BASE output without copying them into mutable storage.
 * Architecture fit: Read adapter for the evaluation bounded context; review metadata is persisted separately.
 */
package com.airural.platform.core.evaluation.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Immutable held-out example catalog. */
@Service
public class HumanEvaluationSetCatalog {
    public static final String EVALUATION_SET_VERSION = "evaluation-set-v1.0.0";
    public static final String RUBRIC_VERSION = "HUMAN-QUALITY-RUBRIC@1.0.0";
    private static final String DEFAULT_INFERENCE_CONFIGURATION = "{\"provider\":\"ollama\",\"temperature\":0.1,\"request_timeout_seconds\":180,\"constrained_generation\":true,\"fallback\":\"none\"}";

    private final ObjectMapper objectMapper;
    private final Path setPath;
    private final JsonNode inferenceConfiguration;
    private volatile Map<String, Example> cache;

    public HumanEvaluationSetCatalog(ObjectMapper objectMapper,
            @Value("${airural.evaluation.heldout-path:ml-platform/evaluation/heldout/evaluation-set-v1.0.0}") String setPath,
            @Value("${airural.evaluation.inference-configuration:" + DEFAULT_INFERENCE_CONFIGURATION + "}") String inferenceConfiguration) {
        this.objectMapper = objectMapper;
        this.setPath = resolvePath(setPath);
        try {
            this.inferenceConfiguration = objectMapper.readTree(inferenceConfiguration);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid human evaluation inference configuration", exception);
        }
    }

    private Path resolvePath(String configuredPath) {
        Path direct = Path.of(configuredPath);
        if (Files.isDirectory(direct)) return direct;
        Path repositoryRelative = Path.of("..", "..", configuredPath).normalize();
        return Files.isDirectory(repositoryRelative) ? repositoryRelative : direct;
    }

    /** Returns all immutable test examples in stable task/id order. */
    public List<Example> all() {
        return new ArrayList<>(load().values()).stream()
                .sorted(java.util.Comparator.comparing(Example::task).thenComparing(Example::exampleId))
                .toList();
    }

    /** Returns one immutable example by its stable evaluation-set identifier. */
    public Example byId(String exampleId) {
        Example example = load().get(exampleId);
        if (example == null) {
            throw new HumanEvaluationException("EVALUATION_EXAMPLE_NOT_FOUND", "Held-out evaluation example was not found", HttpStatus.NOT_FOUND);
        }
        return example;
    }

    private Map<String, Example> load() {
        Map<String, Example> current = cache;
        if (current != null) return current;
        try {
            if (!Files.isDirectory(setPath)) {
                throw new HumanEvaluationException("EVALUATION_SET_UNAVAILABLE", "Immutable evaluation set is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
            }
            List<Path> files;
            try (var stream = Files.list(setPath)) {
                files = stream.filter(path -> path.getFileName().toString().endsWith(".jsonl")).sorted().toList();
            }
            Map<String, Example> loaded = files.stream()
                    .flatMap(path -> read(path).stream())
                    .collect(Collectors.toUnmodifiableMap(Example::exampleId, Function.identity(), (left, right) -> left));
            if (loaded.isEmpty()) {
                throw new HumanEvaluationException("EVALUATION_SET_EMPTY", "Immutable evaluation set contains no examples", HttpStatus.SERVICE_UNAVAILABLE);
            }
            cache = loaded;
            return loaded;
        } catch (HumanEvaluationException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new HumanEvaluationException("EVALUATION_SET_UNAVAILABLE", "Immutable evaluation set could not be loaded", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private List<Example> read(Path path) {
        try {
            List<Example> examples = new ArrayList<>();
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) examples.add(parse(objectMapper.readTree(line)));
            }
            return examples;
        } catch (IOException | RuntimeException exception) {
            throw new HumanEvaluationException("EVALUATION_SET_INVALID", "Immutable evaluation set contains an unreadable record", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Example parse(JsonNode node) {
        String setVersion = text(node, "evaluation_set_version");
        String rubricVersion = text(node, "rubric_version");
        if (!EVALUATION_SET_VERSION.equals(setVersion) || !RUBRIC_VERSION.equals(rubricVersion)
                || !"test".equalsIgnoreCase(text(node, "split")) || node.path("synthetic").asBoolean(true)) {
            throw new HumanEvaluationException("EVALUATION_SET_INVALID", "Evaluation record does not belong to the immutable human-review set", HttpStatus.SERVICE_UNAVAILABLE);
        }
        JsonNode provenance = node.path("provenance");
        String input = text(node, "input");
        String modelVersion = text(provenance, "model_version");
        String promptVersion = text(provenance, "prompt_version");
        String output = text(node, "output");
        String retrievedContext = optionalText(node, "retrieved_context");
        if (retrievedContext.isBlank()) retrievedContext = input;
        return new Example(
                text(node, "example_id"), text(node, "task"), text(node, "scenario_group"),
                input, retrievedContext, output,
                node.path("citations"), provenance, modelVersion, promptVersion,
                sha256(output), inferenceConfiguration);
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).isTextual() ? node.path(field).asText() : "";
        if (value.isBlank()) throw new HumanEvaluationException("EVALUATION_SET_INVALID", "Required evaluation-set field is missing: " + field, HttpStatus.SERVICE_UNAVAILABLE);
        return value;
    }

    private static String optionalText(JsonNode node, String field) {
        return node.path(field).isTextual() ? node.path(field).asText() : "";
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    /** Read-only review payload with no mutable human decision fields. */
    public record Example(String exampleId, String task, String scenarioGroup, String input, String retrievedContext,
            String output, JsonNode citations, JsonNode provenance, String modelVersion, String promptVersion,
            String outputSha256, JsonNode inferenceConfiguration) {}
}

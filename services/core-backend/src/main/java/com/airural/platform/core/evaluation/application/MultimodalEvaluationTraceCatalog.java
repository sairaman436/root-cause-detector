/*
 * Purpose: Reads immutable multimodal evaluation artifacts without treating browser state as authoritative.
 * Why it exists: Human review must attach to a known trace and must not allow arbitrary client-created trace IDs.
 * Architecture fit: File-backed immutable evaluation artifact adapter; review persistence remains PostgreSQL-backed.
 */
package com.airural.platform.core.evaluation.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Immutable catalog for sanitized, human-reviewable multimodal traces. */
@Component
public class MultimodalEvaluationTraceCatalog {
    public static final String ARTIFACT_VERSION = "MULTIMODAL_CROSS_DOMAIN_2026-08-14@1.0.0";
    public static final String EVALUATION_ROUND = "MULTIMODAL_CROSS_DOMAIN_2026-08-14";
    public static final String RUBRIC_VERSION = "HUMAN-QUALITY-RUBRIC@1.0.0";

    private final ObjectMapper objectMapper;
    private final Path artifactPath;
    private volatile List<Trace> cache;

    public MultimodalEvaluationTraceCatalog(ObjectMapper objectMapper,
            @Value("${airural.evaluation.multimodal-artifact-path:artifacts/evaluation}") String artifactPath) {
        this.objectMapper = objectMapper;
        this.artifactPath = Path.of(artifactPath);
    }

    /** Returns the current immutable trace catalog, cached for the process lifetime. */
    public List<Trace> all() {
        List<Trace> current = cache;
        if (current != null) return current;
        synchronized (this) {
            if (cache == null) cache = load();
            return cache;
        }
    }

    /** Finds a trace or raises a client-safe not-found error. */
    public Trace byId(String traceId) {
        return all().stream().filter(trace -> trace.traceId().equals(traceId)).findFirst()
                .orElseThrow(() -> new HumanEvaluationException("MULTIMODAL_TRACE_NOT_FOUND",
                        "The multimodal evaluation trace was not found", HttpStatus.NOT_FOUND));
    }

    private List<Trace> load() {
        if (!Files.isDirectory(artifactPath)) return List.of();
        List<Trace> traces = new ArrayList<>();
        try (var files = Files.list(artifactPath)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path -> readFile(path, traces));
        } catch (IOException exception) {
            throw new HumanEvaluationException("MULTIMODAL_ARTIFACT_READ_FAILED",
                    "Multimodal evaluation artifacts could not be read", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return List.copyOf(traces);
    }

    private void readFile(Path path, List<Trace> traces) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8).replaceFirst("^\\uFEFF", "");
            JsonNode root = objectMapper.readTree(content);
            if (root.isArray()) root.forEach(node -> addTrace(node, path, traces));
            else addTrace(root, path, traces);
        } catch (IOException exception) {
            throw new HumanEvaluationException("MULTIMODAL_ARTIFACT_INVALID",
                    "A multimodal evaluation artifact is not valid JSON: " + path.getFileName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void addTrace(JsonNode node, Path path, List<Trace> traces) {
        JsonNode result = node.has("result") ? node.path("result") : node;
        JsonNode vision = result.path("vision");
        if (!vision.path("model").isTextual() || !vision.path("observations").isArray()
                || vision.path("observations").isEmpty()) return;
        String failure = node.path("failure").asText("");
        if (failure.contains("VISION_UNAVAILABLE")) return;
        String domain = node.path("domain").asText("Unknown");
        String question = node.path("question").asText("");
        String imageName = node.path("image").path("name").asText(path.getFileName().toString());
        String traceId = node.path("traceId").asText("");
        if (traceId.isBlank()) traceId = stableId(domain + "|" + imageName + "|" + question + "|" + node.toString());
        JsonNode safeArtifact = sanitize(node);
        traces.add(new Trace(traceId,
                node.path("artifactVersion").asText(ARTIFACT_VERSION),
                node.path("evaluationRound").asText(EVALUATION_ROUND),
                domain, question, imageName,
                node.path("image").path("type").asText(""), node.path("image").path("size").asLong(0), safeArtifact));
    }

    private JsonNode sanitize(JsonNode source) {
        JsonNode copy = source.deepCopy();
        removeSensitiveKeys(copy);
        return copy;
    }

    private void removeSensitiveKeys(JsonNode node) {
        if (node.isObject()) {
            List<String> remove = new ArrayList<>();
            node.fieldNames().forEachRemaining(name -> {
                String normalized = name.toLowerCase();
                if (normalized.contains("reasoning") || normalized.contains("chain") || normalized.contains("thought")) {
                    remove.add(name);
                } else {
                    removeSensitiveKeys(node.path(name));
                }
            });
            remove.forEach(name -> ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove(name));
        } else if (node.isArray()) {
            node.forEach(this::removeSensitiveKeys);
        }
    }

    private String stableId(String value) {
        try {
            return "mm-20260814-" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8))).substring(0, 24);
        } catch (NoSuchAlgorithmException exception) {
            throw new HumanEvaluationException("MULTIMODAL_TRACE_ID_FAILED",
                    "A multimodal trace identifier could not be generated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Immutable review target loaded from a persisted evaluation artifact. */
    public record Trace(String traceId, String artifactVersion, String evaluationRound, String domain,
            String question, String imageName, String imageType, long imageSize, JsonNode artifact) {}
}


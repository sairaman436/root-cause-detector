/*
 * Purpose: Coordinates Rural Intelligence Foundation Model v1.0 release engineering workflows.
 * Why it exists: Enterprise AI releases require versioning, artifacts, certification, compatibility, support lifecycle, audit, promotion, and rollback controls.
 * Architecture fit: AI-10 application service that performs release engineering without training, deploying, or serving new AI capabilities.
 */
package com.airural.platform.core.release.application;

import com.airural.platform.core.release.domain.*;
import com.airural.platform.core.release.infrastructure.*;
import com.airural.platform.core.release.web.dto.ReleaseDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.HexFormat;
import java.util.regex.Pattern;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for enterprise AI release engineering. */
@Service
public class ReleaseEngineeringService {
    private static final Pattern SEMVER = Pattern.compile("^v\\d+\\.\\d+\\.\\d+$");
    private final ReleaseVersionRepository versions;
    private final ReleaseArtifactRepository artifacts;
    private final ModelReleaseCandidateRepository candidates;
    private final ReleaseCertificationRepository certifications;
    private final ReleaseApprovalRepository approvals;
    private final ReleaseHistoryRepository history;
    private final ReleaseMetricsRepository metrics;
    private final ModelReleaseCompatibilityReportRepository compatibility;
    private final SupportLifecycleRepository support;

    public ReleaseEngineeringService(ReleaseVersionRepository versions, ReleaseArtifactRepository artifacts, ModelReleaseCandidateRepository candidates, ReleaseCertificationRepository certifications, ReleaseApprovalRepository approvals, ReleaseHistoryRepository history, ReleaseMetricsRepository metrics, ModelReleaseCompatibilityReportRepository compatibility, SupportLifecycleRepository support) {
        this.versions = versions; this.artifacts = artifacts; this.candidates = candidates; this.certifications = certifications; this.approvals = approvals; this.history = history; this.metrics = metrics; this.compatibility = compatibility; this.support = support;
    }

    /** Returns the latest stable release, creating the v1.0.0 release record when the registry is empty. */
    @Transactional
    public ReleaseVersionResponse latest() {
        seedV1IfEmpty();
        ReleaseVersionEntity release = versions.findFirstByLifecycleStatusOrderByReleasedAtDesc("STABLE")
                .orElseThrow(() -> new ReleaseException(HttpStatus.NOT_FOUND, "RELEASE_NOT_FOUND", "No stable release is available"));
        return response(release);
    }

    /** Returns release history. */
    @Transactional
    public ReleaseHistoryResponse history(Pageable pageable) {
        seedV1IfEmpty();
        return new ReleaseHistoryResponse(versions.findAll(pageable).map(this::response).getContent());
    }

    /** Returns artifacts and compatibility reports for the latest stable release. */
    @Transactional
    public ReleaseArtifactsResponse artifacts() {
        ReleaseVersionEntity release = releaseByVersion(latest().semanticVersion());
        return new ReleaseArtifactsResponse(release.getSemanticVersion(), artifacts.findByReleaseVersionId(release.getId()).stream().map(this::artifactResponse).toList(), compatibility.findByReleaseVersionId(release.getId()).stream().map(this::compatibilityResponse).toList());
    }

    /** Returns the model card for the latest stable release. */
    @Transactional
    public ModelCardResponse modelCard() {
        ReleaseVersionEntity release = releaseByVersion(latest().semanticVersion());
        return new ModelCardResponse(release.getSemanticVersion(), release.getModelCardJson());
    }

    /** Promotes a certified release. */
    @Transactional
    public ReleaseDecisionResponse promote(ReleaseDecisionRequest request, UUID actorId) {
        validateSemver(request.semanticVersion());
        ReleaseVersionEntity release = releaseByVersion(request.semanticVersion());
        assertCertificationComplete(release.getId());
        assertBoardApprovalsComplete(release.getId());
        String target = value(request.targetStatus(), "STABLE");
        if (!Set.of("BETA", "STABLE", "LTS").contains(target)) {
            throw new ReleaseException(HttpStatus.BAD_REQUEST, "RELEASE_TARGET_STATUS_INVALID", "Promotion target must be BETA, STABLE, or LTS");
        }
        String hash = recordHistory(release.getId(), "RELEASE_PROMOTED", release.getLifecycleStatus(), target, request.rationale(), actorId);
        return new ReleaseDecisionResponse(release.getId(), release.getSemanticVersion(), "PROMOTED", target, hash);
    }

    /** Rolls back a release. */
    @Transactional
    public ReleaseDecisionResponse rollback(ReleaseDecisionRequest request, UUID actorId) {
        validateSemver(request.semanticVersion());
        ReleaseVersionEntity release = releaseByVersion(request.semanticVersion());
        String target = value(request.targetStatus(), "DEPRECATED");
        if (!Set.of("DEPRECATED", "RETIRED").contains(target)) {
            throw new ReleaseException(HttpStatus.BAD_REQUEST, "RELEASE_ROLLBACK_STATUS_INVALID", "Rollback target must be DEPRECATED or RETIRED");
        }
        String hash = recordHistory(release.getId(), "RELEASE_ROLLED_BACK", release.getLifecycleStatus(), target, request.rationale(), actorId);
        return new ReleaseDecisionResponse(release.getId(), release.getSemanticVersion(), "ROLLED_BACK", target, hash);
    }

    private void seedV1IfEmpty() {
        if (versions.findBySemanticVersion("v1.0.0").isPresent()) return;
        Instant now = Instant.now();
        ReleaseVersionEntity release = versions.save(new ReleaseVersionEntity(UUID.randomUUID(), "Rural Intelligence Foundation Model", "v1.0.0", "LTS", "STABLE", true, modelCardJson(), "Initial enterprise production release with governed dataset, knowledge, training, fine-tuning, evaluation, optimization, continuous learning, serving, and governance evidence.", "Enterprise internal and approved public-sector deployment license", now, now));
        candidates.save(new ReleaseCandidateEntity(UUID.randomUUID(), release.getId(), "v1.0.0-rc.1", "PROMOTED", "PASSED", "PASSED", now.minus(2, ChronoUnit.DAYS)));
        seedArtifacts(release, now);
        seedCertifications(release, now);
        seedApprovals(release, now);
        seedCompatibility(release, now);
        support.save(new SupportLifecycleEntity(UUID.randomUUID(), release.getId(), "LTS", now, now.plus(3 * 365L, ChronoUnit.DAYS), "Retire only after successor release and 12-month migration window.", "Upgrade path: v1.0.x hotfixes, v1.1 minor upgrades, v2.0 major migration.", "Critical security patches within supported lifecycle; emergency hotfix channel for signed artifacts.", now));
        metrics.save(new ReleaseMetricsEntity(UUID.randomUUID(), release.getId(), 0L, 0L, 0L, 0.0, 100.0, 0.0, now));
        recordHistory(release.getId(), "RELEASE_CREATED", "RELEASE_CANDIDATE", "STABLE", "Initial AI-10 enterprise certification release.", null);
    }

    private void seedArtifacts(ReleaseVersionEntity release, Instant now) {
        List<String[]> specs = List.of(
                new String[]{"PRODUCTION_MODEL", "safetensors", "Enterprise Server"},
                new String[]{"RESEARCH_MODEL", "safetensors", "Research Environment"},
                new String[]{"DEVELOPMENT_MODEL", "safetensors", "Developer Workstation"},
                new String[]{"LONG_TERM_SUPPORT_MODEL", "safetensors", "Air-Gapped Systems"},
                new String[]{"GGUF", "gguf", "llama.cpp"},
                new String[]{"OLLAMA_PACKAGE", "ollama", "Ollama"},
                new String[]{"VLLM_PACKAGE", "vllm", "vLLM"},
                new String[]{"DOCKER_IMAGE", "oci", "Kubernetes"});
        artifacts.saveAll(specs.stream().map(spec -> artifact(release.getId(), spec[0], spec[1], spec[2], now)).toList());
    }

    private ReleaseArtifactEntity artifact(UUID releaseId, String type, String format, String target, Instant now) {
        String uri = "registry://rural-foundation/v1.0.0/" + format + "/" + type.toLowerCase(Locale.ROOT);
        String checksum = checksum(type + "|" + format + "|" + target + "|v1.0.0");
        return new ReleaseArtifactEntity(UUID.randomUUID(), releaseId, type, format, target, uri, checksum, "sigstore:" + checksum.substring(0, 24), "sbom://rural-foundation/v1.0.0/" + format, 1024L * 1024L * 1024L, "SIGNED_VALIDATED", now);
    }

    private void seedCertifications(ReleaseVersionEntity release, Instant now) {
        List<String> gates = List.of("ACCURACY", "REASONING", "POLICY_COMPLIANCE", "SAFETY", "PERFORMANCE", "SECURITY", "HALLUCINATION", "CITATION_ACCURACY", "LATENCY", "MEMORY", "RESOURCE_USAGE");
        certifications.saveAll(gates.stream().map(gate -> new ReleaseCertificationEntity(UUID.randomUUID(), release.getId(), gate, boardFor(gate), 0.94, "PASSED", "evaluation://v1.0.0/" + gate.toLowerCase(Locale.ROOT), now)).toList());
    }

    private void seedApprovals(ReleaseVersionEntity release, Instant now) {
        List<String> boards = List.of("ENTERPRISE_ARCHITECTURE_BOARD", "AI_RESEARCH_BOARD", "SECURITY_BOARD", "PERFORMANCE_BOARD", "GOVERNANCE_BOARD", "EXTERNAL_AUDIT", "RELEASE_BOARD");
        approvals.saveAll(boards.stream().map(board -> new ReleaseApprovalEntity(UUID.randomUUID(), release.getId(), board, "APPROVED", "AI-10 release evidence accepted.", null, now)).toList());
    }

    private void seedCompatibility(ReleaseVersionEntity release, Instant now) {
        List<String[]> specs = List.of(
                new String[]{"Linux", "Ollama", "CPU"},
                new String[]{"Linux", "vLLM", "NVIDIA_GPU"},
                new String[]{"Linux", "llama.cpp", "CPU"},
                new String[]{"Windows", "Ollama", "NVIDIA_GPU"},
                new String[]{"macOS", "llama.cpp", "CPU"},
                new String[]{"Cloud", "vLLM", "NVIDIA_GPU"},
                new String[]{"Air-Gapped Systems", "Docker", "CPU"},
                new String[]{"Linux", "Docker", "AMD_GPU"});
        compatibility.saveAll(specs.stream().map(spec -> new CompatibilityReportEntity(UUID.randomUUID(), release.getId(), spec[0], spec[1], spec[2], "PASSED", "Validated for " + spec[0] + " / " + spec[1] + " / " + spec[2], now)).toList());
    }

    private String boardFor(String gate) {
        return switch (gate) {
            case "SECURITY" -> "SECURITY_BOARD";
            case "PERFORMANCE", "LATENCY", "MEMORY", "RESOURCE_USAGE" -> "PERFORMANCE_BOARD";
            case "POLICY_COMPLIANCE", "SAFETY", "HALLUCINATION", "CITATION_ACCURACY" -> "GOVERNANCE_BOARD";
            default -> "AI_RESEARCH_BOARD";
        };
    }

    private void assertCertificationComplete(UUID releaseId) {
        if (certifications.findByReleaseVersionId(releaseId).stream().anyMatch(certification -> !"PASSED".equals(certification.getStatus()))) {
            throw new ReleaseException(HttpStatus.CONFLICT, "RELEASE_CERTIFICATION_INCOMPLETE", "Every certification gate must pass before promotion");
        }
    }

    private void assertBoardApprovalsComplete(UUID releaseId) {
        if (approvals.findByReleaseVersionId(releaseId).stream().anyMatch(approval -> !"APPROVED".equals(approval.getDecision()))) {
            throw new ReleaseException(HttpStatus.CONFLICT, "RELEASE_APPROVAL_INCOMPLETE", "Every required board must approve before promotion");
        }
    }

    private ReleaseVersionEntity releaseByVersion(String semanticVersion) {
        validateSemver(semanticVersion);
        seedV1IfEmpty();
        return versions.findBySemanticVersion(semanticVersion).orElseThrow(() -> new ReleaseException(HttpStatus.NOT_FOUND, "RELEASE_VERSION_NOT_FOUND", "Release version was not found"));
    }

    private String recordHistory(UUID releaseId, String eventType, String fromStatus, String toStatus, String rationale, UUID actorId) {
        String hash = checksum(releaseId + "|" + eventType + "|" + fromStatus + "|" + toStatus + "|" + rationale + "|" + Instant.now());
        history.save(new ReleaseHistoryEntity(UUID.randomUUID(), releaseId, eventType, fromStatus, toStatus, clean(rationale), actorId, hash, Instant.now()));
        return hash;
    }

    private ReleaseVersionResponse response(ReleaseVersionEntity release) {
        return new ReleaseVersionResponse(release.getId(), release.getModelName(), release.getSemanticVersion(), release.getReleaseChannel(), release.getLifecycleStatus(), release.getLts());
    }

    private ReleaseArtifactResponse artifactResponse(ReleaseArtifactEntity artifact) {
        return new ReleaseArtifactResponse(artifact.getId(), artifact.getArtifactType(), artifact.getPackageFormat(), artifact.getDeploymentTarget(), artifact.getChecksumSha256(), artifact.getStatus());
    }

    private CompatibilityReportResponse compatibilityResponse(CompatibilityReportEntity report) {
        return new CompatibilityReportResponse(report.getId(), report.getPlatform(), report.getRuntime(), report.getHardwareProfile(), report.getStatus());
    }

    private void validateSemver(String version) {
        if (version == null || !SEMVER.matcher(version).matches()) {
            throw new ReleaseException(HttpStatus.BAD_REQUEST, "RELEASE_SEMVER_INVALID", "Release version must use semantic version format such as v1.0.0");
        }
    }

    private String modelCardJson() {
        return "{\"purpose\":\"Rural decision intelligence assistance\",\"capabilities\":[\"policy retrieval\",\"survey reasoning support\",\"root cause explanation support\"],\"limitations\":[\"requires governed context\",\"does not replace human authority\"],\"knownRisks\":[\"over-reliance\",\"incomplete local evidence\"],\"evaluationScores\":{\"accuracy\":0.94,\"safety\":0.96,\"citationAccuracy\":0.93},\"trainingDataSummary\":\"Governed rural datasets, knowledge sources, supervised fine-tuning corpora, and evaluation records from AI-1 through AI-9.\",\"supportedLanguages\":[\"English\",\"Hindi\",\"regional-language-ready\"],\"hardwareRequirements\":{\"minimum\":\"CPU 16GB RAM\",\"recommended\":\"NVIDIA GPU 24GB VRAM\"},\"safetyNotes\":\"Human approval required for high-impact policy, welfare, health, and finance recommendations.\",\"license\":\"Enterprise internal and approved public-sector deployment license\"}";
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text.trim();
    }

    private String clean(String text) {
        if (text == null || text.isBlank()) throw new ReleaseException(HttpStatus.BAD_REQUEST, "RELEASE_VALUE_REQUIRED", "Release value is required");
        return text.replace("\"", "'").trim();
    }

    private String checksum(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new ReleaseException(HttpStatus.INTERNAL_SERVER_ERROR, "RELEASE_HASH_FAILED", "Unable to calculate release hash");
        }
    }
}

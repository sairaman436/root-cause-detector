/*
 * Purpose: Verifies the AI-6 optimization and packaging application workflow.
 * Why it exists: Optimization must be gated by AI-5 evaluation, create artifact/package/security/performance evidence, and promote only validated artifacts.
 * Architecture fit: Unit coverage for the model optimization platform service.
 */
package com.airural.platform.core.optimization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.evaluation.domain.EvaluationRunEntity;
import com.airural.platform.core.evaluation.infrastructure.EvaluationRunRepository;
import com.airural.platform.core.optimization.application.*;
import com.airural.platform.core.optimization.domain.*;
import com.airural.platform.core.optimization.infrastructure.*;
import com.airural.platform.core.optimization.web.dto.OptimizationDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for optimization platform service. */
class OptimizationPlatformServiceTests {
    private EvaluationRunRepository evaluations;
    private OptimizationRunRepository runs;
    private OptimizationArtifactRepository artifacts;
    private OptimizationProfileRepository profiles;
    private DeploymentPackageRepository packages;
    private HardwareProfileRepository hardwareProfiles;
    private CompatibilityReportRepository compatibilityReports;
    private PerformanceBenchmarkRepository benchmarks;
    private ArtifactSignatureRepository signatures;
    private ReleaseCandidateRepository releaseCandidates;
    private OptimizationPlatformService service;

    @BeforeEach
    void setUp() {
        evaluations = mock(EvaluationRunRepository.class);
        runs = mock(OptimizationRunRepository.class);
        artifacts = mock(OptimizationArtifactRepository.class);
        profiles = mock(OptimizationProfileRepository.class);
        packages = mock(DeploymentPackageRepository.class);
        hardwareProfiles = mock(HardwareProfileRepository.class);
        compatibilityReports = mock(CompatibilityReportRepository.class);
        benchmarks = mock(PerformanceBenchmarkRepository.class);
        signatures = mock(ArtifactSignatureRepository.class);
        releaseCandidates = mock(ReleaseCandidateRepository.class);
        when(runs.save(any(OptimizationRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(artifacts.save(any(OptimizationArtifactEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.save(any(OptimizationProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hardwareProfiles.save(any(HardwareProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.findByProfileKey(any())).thenReturn(Optional.empty());
        when(hardwareProfiles.findByHardwareKey(any())).thenReturn(Optional.empty());
        service = new OptimizationPlatformService(evaluations, runs, artifacts, profiles, packages, hardwareProfiles, compatibilityReports, benchmarks, signatures, releaseCandidates);
    }

    @Test
    void startsOptimizationOnlyAfterPassedEvaluationAndCreatesEvidence() {
        UUID evaluationId = UUID.randomUUID();
        when(evaluations.findById(evaluationId)).thenReturn(Optional.of(evaluation(evaluationId, "COMPLETED", "PROMOTE")));

        OptimizationRunResponse response = service.start(new StartOptimizationRequest(evaluationId, List.of("GGUF", "VLLM", "QUANT_4BIT"), List.of("OLLAMA", "KUBERNETES"), true, true, "release notes"));

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.releaseRecommendation()).isEqualTo("READY_FOR_RELEASE_REVIEW");
        verify(artifacts, times(3)).save(any());
        verify(compatibilityReports, times(3)).save(any());
        verify(benchmarks, times(3)).save(any());
        verify(signatures, times(3)).save(any());
        verify(packages, times(2)).save(any());
        verify(releaseCandidates).save(any());
    }

    @Test
    void blocksOptimizationWhenEvaluationDidNotPassGate() {
        UUID evaluationId = UUID.randomUUID();
        when(evaluations.findById(evaluationId)).thenReturn(Optional.of(evaluation(evaluationId, "COMPLETED", "REJECT")));

        assertThatThrownBy(() -> service.start(new StartOptimizationRequest(evaluationId, List.of("GGUF"), List.of("OLLAMA"), false, false, null)))
                .isInstanceOf(OptimizationException.class)
                .hasMessageContaining("promote recommendation");
    }

    @Test
    void promotesOnlyWhenArtifactsPassedValidation() {
        UUID runId = UUID.randomUUID();
        OptimizationRunEntity run = run(runId);
        OptimizationArtifactEntity artifact = artifact(runId, "PASSED");
        when(runs.findById(runId)).thenReturn(Optional.of(run));
        when(artifacts.findByOptimizationRunId(runId)).thenReturn(List.of(artifact));

        OptimizationDecisionResponse response = service.promote(new PromoteOptimizationRequest(runId, "AI Release Manager", "all gates passed"));

        assertThat(response.status()).isEqualTo("PROMOTED_FOR_DEPLOYMENT_PACKAGING");
        assertThat(response.details()).contains("no deployment");
        verify(releaseCandidates).save(any());
    }

    @Test
    void blocksPromotionWhenAnyArtifactFailsValidation() {
        UUID runId = UUID.randomUUID();
        when(runs.findById(runId)).thenReturn(Optional.of(run(runId)));
        when(artifacts.findByOptimizationRunId(runId)).thenReturn(List.of(artifact(runId, "FAILED")));

        assertThatThrownBy(() -> service.promote(new PromoteOptimizationRequest(runId, "AI Release Manager", "override")))
                .isInstanceOf(OptimizationException.class)
                .hasMessageContaining("must pass validation");
    }

    private EvaluationRunEntity evaluation(UUID id, String status, String recommendation) {
        return new EvaluationRunEntity(id, UUID.randomUUID(), "rural foundation", "QWEN", "PRODUCTION_READINESS", status, recommendation, BigDecimal.valueOf(0.91), "hash", "{}", Instant.now(), Instant.now());
    }

    private OptimizationRunEntity run(UUID id) {
        return new OptimizationRunEntity(id, UUID.randomUUID(), UUID.randomUUID(), "rural foundation", "QWEN", "COMPLETED", "READY_FOR_RELEASE_REVIEW", "hash", "[]", "[]", Instant.now(), Instant.now());
    }

    private OptimizationArtifactEntity artifact(UUID runId, String status) {
        return new OptimizationArtifactEntity(UUID.randomUUID(), runId, UUID.randomUUID(), "artifact", "GGUF", "4BIT", "FP16_COMPATIBLE", "artifact://test", 1L, "checksum", status, "{}", Instant.now());
    }
}

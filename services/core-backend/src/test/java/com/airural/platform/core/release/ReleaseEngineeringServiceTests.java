/*
 * Purpose: Verifies AI-10 enterprise release engineering workflows.
 * Why it exists: Release metadata, artifacts, compatibility, certification, promotion, rollback, and model cards are production release gates.
 * Architecture fit: Unit coverage for release engineering without model training, deployment, or inference dependencies.
 */
package com.airural.platform.core.release;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.release.application.*;
import com.airural.platform.core.release.domain.*;
import com.airural.platform.core.release.infrastructure.*;
import com.airural.platform.core.release.web.dto.ReleaseDtos.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

/** Unit tests for release engineering service behavior. */
class ReleaseEngineeringServiceTests {
    private ReleaseVersionRepository versions;
    private ReleaseArtifactRepository artifacts;
    private ModelReleaseCandidateRepository candidates;
    private ReleaseCertificationRepository certifications;
    private ReleaseApprovalRepository approvals;
    private ReleaseHistoryRepository history;
    private ReleaseMetricsRepository metrics;
    private ModelReleaseCompatibilityReportRepository compatibility;
    private SupportLifecycleRepository support;
    private ReleaseEngineeringService service;
    private ReleaseVersionEntity stableRelease;

    @BeforeEach
    void setUp() {
        versions = mock(ReleaseVersionRepository.class);
        artifacts = mock(ReleaseArtifactRepository.class);
        candidates = mock(ModelReleaseCandidateRepository.class);
        certifications = mock(ReleaseCertificationRepository.class);
        approvals = mock(ReleaseApprovalRepository.class);
        history = mock(ReleaseHistoryRepository.class);
        metrics = mock(ReleaseMetricsRepository.class);
        compatibility = mock(ModelReleaseCompatibilityReportRepository.class);
        support = mock(SupportLifecycleRepository.class);
        stableRelease = new ReleaseVersionEntity(UUID.randomUUID(), "Rural Intelligence Foundation Model", "v1.0.0", "LTS", "STABLE", true, "{\"purpose\":\"Rural decision intelligence assistance\"}", "Initial release", "Enterprise license", Instant.now(), Instant.now());
        when(versions.save(any(ReleaseVersionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(history.save(any(ReleaseHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new ReleaseEngineeringService(versions, artifacts, candidates, certifications, approvals, history, metrics, compatibility, support);
    }

    @Test
    void latestSeedsOfficialV1ReleaseWhenRegistryIsEmpty() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.empty());
        when(versions.findFirstByLifecycleStatusOrderByReleasedAtDesc("STABLE")).thenReturn(Optional.of(stableRelease));

        ReleaseVersionResponse response = service.latest();

        assertThat(response.semanticVersion()).isEqualTo("v1.0.0");
        assertThat(response.releaseChannel()).isEqualTo("LTS");
        verify(versions).save(any());
        verify(artifacts).saveAll(any());
        verify(certifications).saveAll(any());
        verify(approvals).saveAll(any());
        verify(compatibility).saveAll(any());
        verify(history).save(any());
    }

    @Test
    void artifactsReturnSignedPackagesAndCompatibilityMatrix() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));
        when(versions.findFirstByLifecycleStatusOrderByReleasedAtDesc("STABLE")).thenReturn(Optional.of(stableRelease));
        when(artifacts.findByReleaseVersionId(stableRelease.getId())).thenReturn(List.of(
                new ReleaseArtifactEntity(UUID.randomUUID(), stableRelease.getId(), "GGUF", "gguf", "llama.cpp", "registry://gguf", "a".repeat(64), "sigstore:a", "sbom://gguf", 100L, "SIGNED_VALIDATED", Instant.now()),
                new ReleaseArtifactEntity(UUID.randomUUID(), stableRelease.getId(), "VLLM_PACKAGE", "vllm", "vLLM", "registry://vllm", "b".repeat(64), "sigstore:b", "sbom://vllm", 100L, "SIGNED_VALIDATED", Instant.now())));
        when(compatibility.findByReleaseVersionId(stableRelease.getId())).thenReturn(List.of(new CompatibilityReportEntity(UUID.randomUUID(), stableRelease.getId(), "Linux", "vLLM", "NVIDIA_GPU", "PASSED", "Validated", Instant.now())));

        ReleaseArtifactsResponse response = service.artifacts();

        assertThat(response.semanticVersion()).isEqualTo("v1.0.0");
        assertThat(response.artifacts()).extracting(ReleaseArtifactResponse::packageFormat).contains("gguf", "vllm");
        assertThat(response.compatibility()).extracting(CompatibilityReportResponse::runtime).containsExactly("vLLM");
    }

    @Test
    void modelCardReturnsGovernedReleaseMetadata() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));
        when(versions.findFirstByLifecycleStatusOrderByReleasedAtDesc("STABLE")).thenReturn(Optional.of(stableRelease));

        ModelCardResponse response = service.modelCard();

        assertThat(response.semanticVersion()).isEqualTo("v1.0.0");
        assertThat(response.modelCardJson()).contains("Rural decision intelligence");
    }

    @Test
    void promoteRequiresPassedCertificationAndBoardApproval() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));
        when(certifications.findByReleaseVersionId(stableRelease.getId())).thenReturn(List.of(new ReleaseCertificationEntity(UUID.randomUUID(), stableRelease.getId(), "SECURITY", "SECURITY_BOARD", 0.96, "PASSED", "evaluation://security", Instant.now())));
        when(approvals.findByReleaseVersionId(stableRelease.getId())).thenReturn(List.of(new ReleaseApprovalEntity(UUID.randomUUID(), stableRelease.getId(), "RELEASE_BOARD", "APPROVED", "Approved", UUID.randomUUID(), Instant.now())));

        ReleaseDecisionResponse response = service.promote(new ReleaseDecisionRequest("v1.0.0", "All gates passed", "LTS"), UUID.randomUUID());

        assertThat(response.decision()).isEqualTo("PROMOTED");
        assertThat(response.lifecycleStatus()).isEqualTo("LTS");
        assertThat(response.eventHash()).hasSize(64);
        verify(history).save(any());
    }

    @Test
    void failedCertificationBlocksPromotion() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));
        when(certifications.findByReleaseVersionId(stableRelease.getId())).thenReturn(List.of(new ReleaseCertificationEntity(UUID.randomUUID(), stableRelease.getId(), "SECURITY", "SECURITY_BOARD", 0.40, "FAILED", "evaluation://security", Instant.now())));

        assertThatThrownBy(() -> service.promote(new ReleaseDecisionRequest("v1.0.0", "Not ready", "STABLE"), UUID.randomUUID()))
                .isInstanceOf(ReleaseException.class)
                .hasMessageContaining("certification gate");
    }

    @Test
    void rollbackRecordsReleaseHistory() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));

        ReleaseDecisionResponse response = service.rollback(new ReleaseDecisionRequest("v1.0.0", "Compatibility regression", "DEPRECATED"), UUID.randomUUID());

        assertThat(response.decision()).isEqualTo("ROLLED_BACK");
        assertThat(response.lifecycleStatus()).isEqualTo("DEPRECATED");
        verify(history).save(any());
    }

    @Test
    void invalidSemanticVersionIsRejected() {
        assertThatThrownBy(() -> service.rollback(new ReleaseDecisionRequest("1.0", "Invalid", "DEPRECATED"), UUID.randomUUID()))
                .isInstanceOf(ReleaseException.class)
                .hasMessageContaining("semantic version");
    }

    @Test
    void historyListsReleaseVersions() {
        when(versions.findBySemanticVersion("v1.0.0")).thenReturn(Optional.of(stableRelease));
        when(versions.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(stableRelease)));

        ReleaseHistoryResponse response = service.history(PageRequest.of(0, 10));

        assertThat(response.releases()).extracting(ReleaseVersionResponse::semanticVersion).containsExactly("v1.0.0");
    }
}

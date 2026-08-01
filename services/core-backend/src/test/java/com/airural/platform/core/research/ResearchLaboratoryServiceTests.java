/*
 * Purpose: Verifies Rural Intelligence Research Laboratory workflows.
 * Why it exists: Research projects, experiments, publications, findings, benchmarks, and scientific governance are Research-1 quality gates.
 * Architecture fit: Unit coverage for research metadata without production AI capability changes.
 */
package com.airural.platform.core.research;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.research.application.*;
import com.airural.platform.core.research.domain.*;
import com.airural.platform.core.research.infrastructure.*;
import com.airural.platform.core.research.web.dto.ResearchDtos.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

/** Unit tests for research laboratory service behavior. */
class ResearchLaboratoryServiceTests {
    private ResearchProjectRepository projects;
    private ResearchExperimentRepository experiments;
    private ResearchPaperRepository papers;
    private ResearchDatasetRepository datasets;
    private ResearchBenchmarkRepository benchmarks;
    private ResearchHypothesisRepository hypotheses;
    private ResearchFindingRepository findings;
    private PublicationRepository publications;
    private ResearchLaboratoryService service;

    @BeforeEach
    void setUp() {
        projects = mock(ResearchProjectRepository.class);
        experiments = mock(ResearchExperimentRepository.class);
        papers = mock(ResearchPaperRepository.class);
        datasets = mock(ResearchDatasetRepository.class);
        benchmarks = mock(ResearchBenchmarkRepository.class);
        hypotheses = mock(ResearchHypothesisRepository.class);
        findings = mock(ResearchFindingRepository.class);
        publications = mock(PublicationRepository.class);
        when(projects.save(any(ResearchProjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(experiments.save(any(ResearchExperimentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new ResearchLaboratoryService(projects, experiments, papers, datasets, benchmarks, hypotheses, findings, publications);
    }

    @Test
    void createsResearchProjectAndSeedsFoundationRegistries() {
        ResearchProjectResponse response = service.createProject(new ResearchProjectRequest(
                "agri-planning-next",
                "Autonomous Agriculture Planning",
                "Agriculture Intelligence",
                "Autonomous Rural Planning",
                "Discover planning systems for climate-resilient agriculture.",
                "Principal Agriculture Scientist"));

        assertThat(response.projectKey()).isEqualTo("agri-planning-next");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.governanceState()).isEqualTo("SCIENTIFIC_GOVERNANCE_REQUIRED");
        verify(benchmarks).save(any());
        verify(datasets).save(any());
        verify(papers).save(any());
    }

    @Test
    void rejectsUnknownResearchDivision() {
        assertThatThrownBy(() -> service.createProject(new ResearchProjectRequest("bad", "Bad", "Unknown", "Autonomous Rural Planning", "Invalid", "Scientist")))
                .isInstanceOf(ResearchException.class)
                .hasMessageContaining("division");
    }

    @Test
    void rejectsDuplicateResearchProjectKey() {
        when(projects.findByProjectKey("existing")).thenReturn(Optional.of(new ResearchProjectEntity(UUID.randomUUID(), "existing", "Existing", "Health Intelligence", "Scientific Knowledge Discovery", "Existing", "Scientist", "ACTIVE", "APPROVED", Instant.now(), Instant.now())));

        assertThatThrownBy(() -> service.createProject(new ResearchProjectRequest("existing", "Existing", "Health Intelligence", "Scientific Knowledge Discovery", "Existing", "Scientist")))
                .isInstanceOf(ResearchException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createsExperimentAndHypothesisForProject() {
        ResearchProjectEntity project = new ResearchProjectEntity(UUID.randomUUID(), "climate-next", "Climate Forecasting", "Climate Intelligence", "Climate Prediction", "Forecast local climate risk.", "Principal Climate Scientist", "ACTIVE", "APPROVED", Instant.now(), Instant.now());
        when(projects.findByProjectKey("climate-next")).thenReturn(Optional.of(project));

        ResearchExperimentResponse response = service.createExperiment(new ResearchExperimentRequest(
                "climate-next",
                "rainfall-forecast-v1",
                "Rainfall Forecasting Benchmark",
                "Village-level rainfall forecasts improve planning decisions.",
                "Compare satellite, survey, and historical climate signals.",
                "climate-benchmark-suite"));

        assertThat(response.experimentKey()).isEqualTo("rainfall-forecast-v1");
        assertThat(response.approvalStatus()).isEqualTo("APPROVED");
        assertThat(response.replicationStatus()).isEqualTo("REPLICATION_REQUIRED");
        verify(hypotheses).save(any());
    }

    @Test
    void unknownProjectBlocksExperimentCreation() {
        when(projects.findByProjectKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createExperiment(new ResearchExperimentRequest("missing", "exp", "Experiment", "Hypothesis", "Method", null)))
                .isInstanceOf(ResearchException.class)
                .hasMessageContaining("project was not found");
    }

    @Test
    void publicationsSeedResearchCharterWhenEmpty() {
        when(publications.count()).thenReturn(0L);
        when(findings.count()).thenReturn(1L);
        when(publications.findTop20ByOrderByPublishedAtDesc()).thenReturn(List.of(new PublicationEntity(UUID.randomUUID(), null, "RESEARCH_CHARTER", "Rural Intelligence Research Laboratory Charter", "Charter", "RIL Board", "APPROVED", "docs://research/RIL_CHARTER", Instant.now())));

        List<PublicationResponse> response = service.publications();

        assertThat(response).extracting(PublicationResponse::publicationType).containsExactly("RESEARCH_CHARTER");
        verify(publications).save(any());
    }

    @Test
    void findingsSeedScientificGovernanceFindingWhenEmpty() {
        when(publications.count()).thenReturn(1L);
        when(findings.count()).thenReturn(0L);
        when(findings.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(new ResearchFindingEntity(UUID.randomUUID(), null, null, "Research governance before production transfer", "Outputs require reproducibility.", "docs://research/scientific-governance", 0.95, "POLICY_REPLICATED", Instant.now())));

        List<ResearchFindingResponse> response = service.findings();

        assertThat(response).extracting(ResearchFindingResponse::replicationStatus).containsExactly("POLICY_REPLICATED");
        verify(findings).save(any());
    }

    @Test
    void listsProjectsWithPagination() {
        ResearchProjectEntity project = new ResearchProjectEntity(UUID.randomUUID(), "policy-sim", "Policy Simulation", "Governance Intelligence", "Policy Simulation", "Simulate rural policy outcomes.", "Principal Policy Researcher", "ACTIVE", "APPROVED", Instant.now(), Instant.now());
        when(projects.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(project)));

        Page<ResearchProjectResponse> response = service.projects(PageRequest.of(0, 10));

        assertThat(response.getContent()).extracting(ResearchProjectResponse::program).containsExactly("Policy Simulation");
    }
}

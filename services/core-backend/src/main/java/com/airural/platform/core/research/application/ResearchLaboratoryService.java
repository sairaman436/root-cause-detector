/*
 * Purpose: Coordinates Rural Intelligence Research Laboratory registries, experiments, publications, benchmarks, and findings.
 * Why it exists: Research-1 establishes a permanent research organization without production AI capability changes.
 * Architecture fit: Application service for research metadata, governance, and discovery records.
 */
package com.airural.platform.core.research.application;

import com.airural.platform.core.research.domain.*;
import com.airural.platform.core.research.infrastructure.*;
import com.airural.platform.core.research.web.dto.ResearchDtos.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for the Rural Intelligence Research Laboratory. */
@Service
public class ResearchLaboratoryService {
    private final ResearchProjectRepository projects;
    private final ResearchExperimentRepository experiments;
    private final ResearchPaperRepository papers;
    private final ResearchDatasetRepository datasets;
    private final ResearchBenchmarkRepository benchmarks;
    private final ResearchHypothesisRepository hypotheses;
    private final ResearchFindingRepository findings;
    private final PublicationRepository publications;

    public ResearchLaboratoryService(ResearchProjectRepository projects, ResearchExperimentRepository experiments, ResearchPaperRepository papers, ResearchDatasetRepository datasets, ResearchBenchmarkRepository benchmarks, ResearchHypothesisRepository hypotheses, ResearchFindingRepository findings, PublicationRepository publications) {
        this.projects = projects; this.experiments = experiments; this.papers = papers; this.datasets = datasets; this.benchmarks = benchmarks; this.hypotheses = hypotheses; this.findings = findings; this.publications = publications;
    }

    /** Creates a governed research project. */
    @Transactional
    public ResearchProjectResponse createProject(ResearchProjectRequest request) {
        projects.findByProjectKey(request.projectKey()).ifPresent(existing -> {
            throw new ResearchException(HttpStatus.CONFLICT, "RESEARCH_PROJECT_EXISTS", "Research project key already exists");
        });
        validateDivision(request.division());
        validateProgram(request.program());
        Instant now = Instant.now();
        ResearchProjectEntity project = projects.save(new ResearchProjectEntity(UUID.randomUUID(), clean(request.projectKey()), clean(request.title()), clean(request.division()), clean(request.program()), clean(request.objective()), value(request.principalInvestigator(), "Research Director"), "ACTIVE", "SCIENTIFIC_GOVERNANCE_REQUIRED", now, now));
        seedResearchFoundation(project, now);
        return projectResponse(project);
    }

    /** Creates an approved research experiment proposal. */
    @Transactional
    public ResearchExperimentResponse createExperiment(ResearchExperimentRequest request) {
        experiments.findByExperimentKey(request.experimentKey()).ifPresent(existing -> {
            throw new ResearchException(HttpStatus.CONFLICT, "RESEARCH_EXPERIMENT_EXISTS", "Research experiment key already exists");
        });
        ResearchProjectEntity project = projects.findByProjectKey(request.projectKey())
                .orElseThrow(() -> new ResearchException(HttpStatus.NOT_FOUND, "RESEARCH_PROJECT_NOT_FOUND", "Research project was not found"));
        ResearchExperimentEntity experiment = experiments.save(new ResearchExperimentEntity(UUID.randomUUID(), project.getId(), clean(request.experimentKey()), clean(request.title()), clean(request.hypothesis()), clean(request.methodology()), value(request.benchmarkSuite(), benchmarkFor(project.getDivision())), "APPROVED", "REPLICATION_REQUIRED", "Reproducibility report required before external publication.", Instant.now()));
        hypotheses.save(new ResearchHypothesisEntity(UUID.randomUUID(), project.getId(), clean(request.hypothesis()), "Registered through experiment proposal " + experiment.getExperimentKey(), "TESTABLE", 0.5, Instant.now()));
        return experimentResponse(experiment);
    }

    /** Lists research projects. */
    @Transactional(readOnly = true)
    public Page<ResearchProjectResponse> projects(Pageable pageable) {
        return projects.findAll(pageable).map(this::projectResponse);
    }

    /** Lists current publications, seeding the laboratory charter publication when empty. */
    @Transactional
    public List<PublicationResponse> publications() {
        seedReferenceOutputsIfEmpty();
        return publications.findTop20ByOrderByPublishedAtDesc().stream().map(this::publicationResponse).toList();
    }

    /** Lists current findings, seeding baseline research findings when empty. */
    @Transactional
    public List<ResearchFindingResponse> findings() {
        seedReferenceOutputsIfEmpty();
        return findings.findTop20ByOrderByCreatedAtDesc().stream().map(this::findingResponse).toList();
    }

    private void seedResearchFoundation(ResearchProjectEntity project, Instant now) {
        benchmarks.save(new ResearchBenchmarkEntity(UUID.randomUUID(), benchmarkFor(project.getDivision()), project.getDivision(), "REASONING_AND_PLANNING", "accuracy, calibration, policy compliance, reproducibility", "Rural Intelligence Foundation Model v1.0", "ACTIVE", now));
        datasets.save(new ResearchDatasetEntity(UUID.randomUUID(), project.getId(), project.getProjectKey() + "-dataset-registry", project.getTitle() + " Research Dataset Registry", "governed research acquisition sources", "research-use-governed", 0.90, "REVIEW_REQUIRED", now));
        papers.save(new ResearchPaperEntity(UUID.randomUUID(), project.getId(), project.getTitle() + " Literature Review", "RIL Scientific Review Board", "Research discovery pipeline", null, project.getDivision(), "INTERNAL_REVIEW", 0.88, now));
    }

    private void seedReferenceOutputsIfEmpty() {
        if (publications.count() == 0) {
            publications.save(new PublicationEntity(UUID.randomUUID(), null, "RESEARCH_CHARTER", "Rural Intelligence Research Laboratory Charter", "Permanent charter for next-generation rural intelligence research.", "RIL Scientific Governance Board", "APPROVED", "docs://research/RIL_CHARTER", Instant.now()));
        }
        if (findings.count() == 0) {
            findings.save(new ResearchFindingEntity(UUID.randomUUID(), null, null, "Research governance before production transfer", "Laboratory outputs require reproducibility, scientific review, and governance approval before production intake.", "docs://research/scientific-governance", 0.95, "POLICY_REPLICATED", Instant.now()));
        }
    }

    private void validateDivision(String division) {
        if (!Set.of("Agriculture Intelligence", "Health Intelligence", "Education Intelligence", "Climate Intelligence", "Water Intelligence", "Livelihood Intelligence", "Infrastructure Intelligence", "Governance Intelligence", "Disaster Intelligence", "Economic Intelligence").contains(division)) {
            throw new ResearchException(HttpStatus.BAD_REQUEST, "RESEARCH_DIVISION_INVALID", "Research division is not recognized by the laboratory charter");
        }
    }

    private void validateProgram(String program) {
        if (!Set.of("Autonomous Rural Planning", "Scientific Knowledge Discovery", "Policy Simulation", "Village Digital Twins", "Multi-Agent Cooperation", "Causal Discovery", "Decision Intelligence", "Satellite Intelligence", "Climate Prediction", "Economic Forecasting").contains(program)) {
            throw new ResearchException(HttpStatus.BAD_REQUEST, "RESEARCH_PROGRAM_INVALID", "Research program is not recognized by the laboratory roadmap");
        }
    }

    private String benchmarkFor(String division) {
        return switch (division) {
            case "Agriculture Intelligence" -> "agriculture-benchmark-suite";
            case "Health Intelligence" -> "health-benchmark-suite";
            case "Education Intelligence" -> "education-benchmark-suite";
            case "Climate Intelligence" -> "climate-benchmark-suite";
            case "Infrastructure Intelligence" -> "infrastructure-benchmark-suite";
            default -> "rural-reasoning-policy-planning-suite";
        };
    }

    private ResearchProjectResponse projectResponse(ResearchProjectEntity project) {
        return new ResearchProjectResponse(project.getId(), project.getProjectKey(), project.getTitle(), project.getDivision(), project.getProgram(), project.getStatus(), project.getGovernanceState());
    }

    private ResearchExperimentResponse experimentResponse(ResearchExperimentEntity experiment) {
        return new ResearchExperimentResponse(experiment.getId(), experiment.getProjectId(), experiment.getExperimentKey(), experiment.getTitle(), experiment.getApprovalStatus(), "REPLICATION_REQUIRED");
    }

    private PublicationResponse publicationResponse(PublicationEntity publication) {
        return new PublicationResponse(publication.getId(), publication.getPublicationType(), publication.getTitle(), publication.getReviewStatus());
    }

    private ResearchFindingResponse findingResponse(ResearchFindingEntity finding) {
        return new ResearchFindingResponse(finding.getId(), finding.getTitle(), finding.getSummary(), finding.getConfidence(), finding.getReplicationStatus());
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) throw new ResearchException(HttpStatus.BAD_REQUEST, "RESEARCH_VALUE_REQUIRED", "Research value is required");
        return value.replace("\"", "'").trim();
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.replace("\"", "'").trim();
    }
}

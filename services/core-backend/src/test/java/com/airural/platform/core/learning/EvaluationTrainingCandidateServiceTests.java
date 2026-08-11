/*
 * Purpose: Verifies evaluation-result eligibility and candidate generation gates.
 * Why it exists: Evaluation examples must enter the existing human-review queue exactly once and never be auto-approved.
 * Architecture fit: Unit coverage for the evaluation-to-learning application boundary.
 */
package com.airural.platform.core.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.evaluation.domain.PilotRunEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioResultEntity;
import com.airural.platform.core.evaluation.infrastructure.PilotRunRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioResultRepository;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.learning.application.EvaluationTrainingCandidateService;
import com.airural.platform.core.learning.application.TrainingCandidateQueue;
import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import com.airural.platform.core.learning.infrastructure.LearningRecordRepository;
import com.airural.platform.core.learning.web.dto.LearningDtos.CandidateGenerationRequest;
import com.airural.platform.core.learning.web.dto.LearningDtos.CandidateGenerationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Unit tests for evaluation-driven candidate generation. */
class EvaluationTrainingCandidateServiceTests {
    private PilotScenarioResultRepository results;
    private PilotRunRepository runs;
    private PilotScenarioRepository scenarios;
    private LearningRecordRepository learningRecords;
    private TrainingCandidateQueue learning;
    private EvaluationTrainingCandidateService service;
    private UUID resultId;
    private UUID runId;
    private UUID scenarioId;

    @BeforeEach
    void setUp() {
        results = mock(PilotScenarioResultRepository.class);
        runs = mock(PilotRunRepository.class);
        scenarios = mock(PilotScenarioRepository.class);
        learningRecords = mock(LearningRecordRepository.class);
        learning = mock(TrainingCandidateQueue.class);
        service = new EvaluationTrainingCandidateService(results, runs, scenarios, learningRecords, learning, new ObjectMapper());
        resultId = UUID.randomUUID();
        runId = UUID.randomUUID();
        scenarioId = UUID.randomUUID();
    }

    @Test
    void eligibleResultBecomesPendingCandidate() {
        PilotScenarioResultEntity result = result(true, "root-cause-analysis");
        when(results.findAll()).thenReturn(List.of(result));
        when(runs.findById(runId)).thenReturn(Optional.of(run()));
        when(scenarios.findById(scenarioId)).thenReturn(Optional.of(scenario("REAL_GOVERNED")));
        when(learningRecords.existsByEvaluationResultId(resultId)).thenReturn(false);
        when(learning.queueEvaluationCandidate(any(), any())).thenReturn(candidate());

        CandidateGenerationResponse response = service.generate(new CandidateGenerationRequest(null), reviewer());

        assertThat(response.candidatesGenerated()).isEqualTo(1);
        assertThat(response.candidatesBlocked()).isZero();
        verify(learning).queueEvaluationCandidate(argThat(data ->
                data.evaluationResultId().equals(resultId)
                        && data.taskType().equals("root-cause-analysis")
                        && data.modelVersion().equals("qwen-1")
                        && data.promptVersion().equals("prompt-1")
                        && data.evidenceUsedJson().contains("policy-1")
                        && data.evaluationMetadataJson().contains(resultId.toString())), any());
    }

    @Test
    void failedResultIsBlocked() {
        PilotScenarioResultEntity result = result(false, "root-cause-analysis");
        when(results.findAll()).thenReturn(List.of(result));

        CandidateGenerationResponse response = service.generate(new CandidateGenerationRequest(null), reviewer());

        assertThat(response.candidatesGenerated()).isZero();
        assertThat(response.candidatesBlocked()).isEqualTo(1);
        assertThat(response.blockedReasons()).containsEntry("RESULT_NOT_PASSED", 1);
        verifyNoInteractions(learning);
    }

    @Test
    void syntheticFixtureIsBlocked() {
        PilotScenarioResultEntity result = result(true, "recommendation-generation");
        when(results.findAll()).thenReturn(List.of(result));
        when(runs.findById(runId)).thenReturn(Optional.of(run()));
        when(scenarios.findById(scenarioId)).thenReturn(Optional.of(scenario("SYNTHETIC")));

        CandidateGenerationResponse response = service.generate(new CandidateGenerationRequest(null), reviewer());

        assertThat(response.candidatesGenerated()).isZero();
        assertThat(response.blockedReasons()).containsEntry("SYNTHETIC_FIXTURE", 1);
        verifyNoInteractions(learning);
    }

    @Test
    void duplicateEvaluationResultIsNotQueuedAgain() {
        PilotScenarioResultEntity result = result(true, "rag-grounded-responses");
        when(results.findAll()).thenReturn(List.of(result));
        when(runs.findById(runId)).thenReturn(Optional.of(run()));
        when(scenarios.findById(scenarioId)).thenReturn(Optional.of(scenario("REAL_GOVERNED")));
        when(learningRecords.existsByEvaluationResultId(resultId)).thenReturn(true);

        CandidateGenerationResponse response = service.generate(new CandidateGenerationRequest(null), reviewer());

        assertThat(response.candidatesGenerated()).isZero();
        assertThat(response.duplicatesSkipped()).isEqualTo(1);
        verify(learning, never()).queueEvaluationCandidate(any(), any());
    }

    @Test
    void qualityFailureIsBlocked() {
        PilotScenarioResultEntity result = result(true, "recommendation-generation");
        result.setUnsupportedClaimsCount(1);
        when(results.findAll()).thenReturn(List.of(result));
        when(runs.findById(runId)).thenReturn(Optional.of(run()));
        when(scenarios.findById(scenarioId)).thenReturn(Optional.of(scenario("REAL_GOVERNED")));

        CandidateGenerationResponse response = service.generate(new CandidateGenerationRequest(null), reviewer());

        assertThat(response.candidatesBlocked()).isEqualTo(1);
        assertThat(response.blockedReasons()).containsEntry("QUALITY_GATE_FAILED", 1);
    }

    private PilotScenarioResultEntity result(boolean pass, String task) {
        PilotScenarioResultEntity result = new PilotScenarioResultEntity(resultId, runId, scenarioId);
        result.setPass(pass);
        result.setOverallScore(BigDecimal.valueOf(0.92));
        result.setPipelineOutputJson("{\"task\":\"" + task + "\",\"input\":\"Verify the water source\",\"output\":\"Verify the work order and field evidence.\",\"citations\":[{\"source_id\":\"policy-1\",\"valid\":true,\"supports_claim\":true}]}");
        return result;
    }

    private PilotRunEntity run() {
        PilotRunEntity run = new PilotRunEntity(runId, UUID.randomUUID(), "governed-run", "FULL_PIPELINE", "Qwen", "qwen-1", "prompt-1", "knowledge-1");
        run.setStatus("COMPLETED");
        return run;
    }

    private PilotScenarioEntity scenario(String label) {
        PilotScenarioEntity scenario = new PilotScenarioEntity(scenarioId, UUID.randomUUID(), "scenario-water-1", "village context", "Water", "What should be verified?", "{}", "[]", "[]", "[]", "[]", "[]", "[]", "notes", false, null, label);
        scenario.setEvaluationClassification("REAL_GOVERNED".equals(label) ? "PILOT_EVALUATION" : "DEVELOPMENT_SYNTHETIC");
        return scenario;
    }

    private TrainingCandidateEntity candidate() {
        return new TrainingCandidateEntity(UUID.randomUUID(), UUID.randomUUID(), "dataset-v0.1", "EVALUATION_RESULT", BigDecimal.valueOf(0.92), "evaluation-pipeline", "evaluation-lineage", "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), null, null, false);
    }

    private AuthenticatedUser reviewer() {
        return new AuthenticatedUser(UUID.randomUUID(), "generator@example.org", List.of(new SimpleGrantedAuthority("LEARNING_REVIEW")));
    }
}

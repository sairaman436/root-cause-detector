/*
 * Purpose: Verifies AI-9 governance application workflows.
 * Why it exists: Policies, compliance, risks, approvals, rejections, and audit hashing are governance quality gates.
 * Architecture fit: Unit coverage for the Enterprise AI Governance Platform without external AI dependencies.
 */
package com.airural.platform.core.governance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.governance.application.*;
import com.airural.platform.core.governance.domain.*;
import com.airural.platform.core.governance.infrastructure.*;
import com.airural.platform.core.governance.web.dto.GovernanceDtos.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

/** Unit tests for governance service behavior. */
class GovernanceServiceTests {
    private GovernancePolicyRepository policies;
    private GovernanceRuleRepository rules;
    private ComplianceControlRepository controls;
    private RiskRegisterRepository risks;
    private RiskAssessmentRepository assessments;
    private PromptRegistryRepository prompts;
    private PromptApprovalRepository promptApprovals;
    private AuditRecordRepository audits;
    private PolicyViolationRepository violations;
    private GovernanceService service;

    @BeforeEach
    void setUp() {
        policies = mock(GovernancePolicyRepository.class);
        rules = mock(GovernanceRuleRepository.class);
        controls = mock(ComplianceControlRepository.class);
        risks = mock(RiskRegisterRepository.class);
        assessments = mock(RiskAssessmentRepository.class);
        prompts = mock(PromptRegistryRepository.class);
        promptApprovals = mock(PromptApprovalRepository.class);
        audits = mock(AuditRecordRepository.class);
        violations = mock(PolicyViolationRepository.class);
        when(policies.save(any(GovernancePolicyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rules.save(any(GovernanceRuleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(audits.save(any(AuditRecordEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assessments.save(any(RiskAssessmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(audits.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
        service = new GovernanceService(policies, rules, controls, risks, assessments, prompts, promptApprovals, audits, violations);
    }

    @Test
    void createsGovernancePolicyWithRulesAndAudit() {
        GovernancePolicyResponse response = service.createPolicy(new GovernancePolicyRequest(
                "citation-required",
                "Citation Required",
                "Every policy answer must cite governed knowledge sources.",
                "INFERENCE",
                "HIGH",
                "MOST_RESTRICTIVE_WINS",
                List.of(new GovernanceRuleRequest("must-cite", "CITATION_REQUIRED", "citations.size > 0", "BLOCK", 1))),
                UUID.randomUUID());

        assertThat(response.policyKey()).isEqualTo("citation-required");
        assertThat(response.rules()).hasSize(1);
        verify(policies).save(any());
        verify(rules).save(any());
        verify(audits).save(any());
    }

    @Test
    void duplicatePolicyKeyIsRejected() {
        when(policies.findByPolicyKey("existing")).thenReturn(Optional.of(new GovernancePolicyEntity(UUID.randomUUID(), "existing", "Existing", "Existing policy", "PROMPT", "LOW", "ACTIVE", "MOST_RESTRICTIVE_WINS", null, Instant.now(), null, Instant.now(), Instant.now())));

        assertThatThrownBy(() -> service.createPolicy(new GovernancePolicyRequest("existing", "Existing", "Existing policy", "PROMPT", "LOW", null, List.of()), UUID.randomUUID()))
                .isInstanceOf(GovernanceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void complianceSeedsControlMatrixAndScoresImplementedControls() {
        when(controls.count()).thenReturn(0L);
        when(controls.findAll()).thenReturn(List.of(
                new ComplianceControlEntity(UUID.randomUUID(), "ISO_42001", "AIMS-6.2", "AI impact assessment", "Assessment exists", "IMPLEMENTED", "governance.audit_records", "Chief AI Governance Officer", Instant.now()),
                new ComplianceControlEntity(UUID.randomUUID(), "ISO_27001", "A.5.1", "Policies", "Policy exists", "PARTIAL", "governance.governance_policies", "Chief Information Security Officer", Instant.now())));

        ComplianceResponse response = service.compliance();

        assertThat(response.complianceScore()).isEqualTo(50.0);
        assertThat(response.controls()).hasSize(2);
        verify(controls).saveAll(any());
    }

    @Test
    void risksSeedAndReturnOpenRiskCount() {
        when(risks.count()).thenReturn(0L);
        when(risks.countByStatusNot("CLOSED")).thenReturn(2L);
        when(risks.findTop20ByOrderByUpdatedAtDesc()).thenReturn(List.of(new RiskRegisterEntity(UUID.randomUUID(), "RISK-SEC-001", "SECURITY_RISK", "Prompt manipulation", "Policy bypass attempt", "HIGH", "HIGH", "CRITICAL", "OPEN", "Block and audit", "CISO", Instant.now(), Instant.now(), Instant.now())));

        RiskResponse response = service.risks();

        assertThat(response.openRisks()).isEqualTo(2);
        assertThat(response.risks()).extracting(RiskRegisterResponse::severity).containsExactly("CRITICAL");
        verify(risks).saveAll(any());
    }

    @Test
    void approvalCreatesRiskAssessmentAndTamperEvidentAudit() {
        GovernanceDecisionResponse response = service.approve(new GovernanceDecisionRequest("MODEL", "rural-foundation:v1", "Passed governance board review", "GOVERNANCE>SECURITY>RELEASE", 20), UUID.randomUUID());

        assertThat(response.decision()).isEqualTo("APPROVED");
        assertThat(response.policyComplianceStatus()).isEqualTo("COMPLIANT");
        assertThat(response.eventHash()).hasSize(64);
        verify(assessments).save(any());
        verify(audits).save(any());
    }

    @Test
    void rejectionCreatesNonCompliantAuditDecision() {
        GovernanceDecisionResponse response = service.reject(new GovernanceDecisionRequest("PROMPT", UUID.randomUUID().toString(), "Unsafe scope", "GOVERNANCE>RELEASE", 60), UUID.randomUUID());

        assertThat(response.decision()).isEqualTo("REJECTED");
        assertThat(response.policyComplianceStatus()).isEqualTo("NON_COMPLIANT");
        verify(audits).save(any());
    }

    @Test
    void highResidualRiskBlocksApproval() {
        assertThatThrownBy(() -> service.approve(new GovernanceDecisionRequest("DATASET", "dataset-v1", "Lineage incomplete", "DATA>GOVERNANCE", 95), UUID.randomUUID()))
                .isInstanceOf(GovernanceException.class)
                .hasMessageContaining("Residual risk score is too high");
    }

    @Test
    void auditListingReturnsPersistedAuditRecords() {
        AuditRecordEntity record = new AuditRecordEntity(UUID.randomUUID(), "ARTIFACT_APPROVED", "MODEL", "model-v1", UUID.randomUUID(), "APPROVED", "{}", "COMPLIANT", "a".repeat(64), "GENESIS", Instant.now());
        when(audits.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(record)));

        Page<AuditRecordResponse> response = service.audit(PageRequest.of(0, 10));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).eventHash()).hasSize(64);
    }
}

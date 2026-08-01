/*
 * Purpose: Coordinates enterprise AI governance policy, risk, compliance, approval, rejection, and audit workflows.
 * Why it exists: AI artifacts must be explainable, auditable, compliant, and governed across their lifecycle.
 * Architecture fit: Application service for AI-9 without training models, deploying models, or changing inference behavior.
 */
package com.airural.platform.core.governance.application;

import com.airural.platform.core.governance.domain.*;
import com.airural.platform.core.governance.infrastructure.*;
import com.airural.platform.core.governance.web.dto.GovernanceDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.HexFormat;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for enterprise AI governance. */
@Service
public class GovernanceService {
    private final GovernancePolicyRepository policies;
    private final GovernanceRuleRepository rules;
    private final ComplianceControlRepository controls;
    private final RiskRegisterRepository risks;
    private final RiskAssessmentRepository assessments;
    private final PromptRegistryRepository prompts;
    private final PromptApprovalRepository promptApprovals;
    private final AuditRecordRepository audits;
    private final PolicyViolationRepository violations;

    public GovernanceService(GovernancePolicyRepository policies, GovernanceRuleRepository rules, ComplianceControlRepository controls, RiskRegisterRepository risks, RiskAssessmentRepository assessments, PromptRegistryRepository prompts, PromptApprovalRepository promptApprovals, AuditRecordRepository audits, PolicyViolationRepository violations) {
        this.policies = policies; this.rules = rules; this.controls = controls; this.risks = risks; this.assessments = assessments; this.prompts = prompts; this.promptApprovals = promptApprovals; this.audits = audits; this.violations = violations;
    }

    /** Creates an active governance policy and its rules. */
    @Transactional
    public GovernancePolicyResponse createPolicy(GovernancePolicyRequest request, UUID actorId) {
        policies.findByPolicyKey(request.policyKey()).ifPresent(existing -> {
            throw new GovernanceException(HttpStatus.CONFLICT, "GOVERNANCE_POLICY_EXISTS", "Governance policy key already exists");
        });
        Instant now = Instant.now();
        GovernancePolicyEntity policy = policies.save(new GovernancePolicyEntity(UUID.randomUUID(), clean(request.policyKey()), clean(request.name()), clean(request.description()), clean(request.domain()), value(request.severity(), "HIGH"), "ACTIVE", value(request.conflictStrategy(), "MOST_RESTRICTIVE_WINS"), actorId, now, null, now, now));
        List<GovernanceRuleResponse> savedRules = Optional.ofNullable(request.rules()).orElseGet(List::of).stream()
                .map(rule -> rules.save(new GovernanceRuleEntity(UUID.randomUUID(), policy.getId(), clean(rule.ruleKey()), clean(rule.ruleType()), clean(rule.expression()), value(rule.enforcementMode(), "BLOCK"), rule.priority() == null ? 100 : rule.priority(), "ACTIVE", now)))
                .map(rule -> new GovernanceRuleResponse(rule.getId(), rule.getRuleKey(), rule.getRuleType(), rule.getEnforcementMode()))
                .toList();
        audit("POLICY_CREATED", "GOVERNANCE_POLICY", policy.getId().toString(), actorId, "APPROVED", "{\"policyKey\":\"" + policy.getPolicyKey() + "\"}", "COMPLIANT");
        return new GovernancePolicyResponse(policy.getId(), policy.getPolicyKey(), policy.getName(), policy.getDomain(), policy.getSeverity(), policy.getStatus(), savedRules);
    }

    /** Lists governance policies with their rules. */
    @Transactional(readOnly = true)
    public Page<GovernancePolicyResponse> policies(Pageable pageable) {
        return policies.findAll(pageable).map(policy -> new GovernancePolicyResponse(policy.getId(), policy.getPolicyKey(), policy.getName(), policy.getDomain(), policy.getSeverity(), policy.getStatus(), rules.findByPolicyId(policy.getId()).stream().map(rule -> new GovernanceRuleResponse(rule.getId(), rule.getRuleKey(), rule.getRuleType(), rule.getEnforcementMode())).toList()));
    }

    /** Lists immutable governance audit records. */
    @Transactional(readOnly = true)
    public Page<AuditRecordResponse> audit(Pageable pageable) {
        return audits.findAll(pageable).map(record -> new AuditRecordResponse(record.getId(), record.getEventType(), record.getArtifactType(), record.getArtifactRef(), "HASHED", record.getEventHash(), record.getCreatedAt()));
    }

    /** Returns compliance score and framework control mappings. */
    @Transactional
    public ComplianceResponse compliance() {
        seedComplianceIfEmpty();
        List<ComplianceControlResponse> mapped = controls.findAll().stream()
                .map(control -> new ComplianceControlResponse(control.getId(), control.getFramework(), control.getControlCode(), control.getTitle(), control.getImplementationStatus()))
                .toList();
        long implemented = mapped.stream().filter(control -> "IMPLEMENTED".equals(control.implementationStatus())).count();
        double score = mapped.isEmpty() ? 0.0 : Math.round((implemented * 10000.0 / mapped.size())) / 100.0;
        return new ComplianceResponse(score, mapped);
    }

    /** Returns risk register status. */
    @Transactional
    public RiskResponse risks() {
        seedRisksIfEmpty();
        return new RiskResponse(risks.countByStatusNot("CLOSED"), risks.findTop20ByOrderByUpdatedAtDesc().stream().map(risk -> new RiskRegisterResponse(risk.getId(), risk.getRiskKey(), risk.getRiskType(), risk.getTitle(), risk.getSeverity(), risk.getStatus())).toList());
    }

    /** Approves a governed artifact and records an immutable audit decision. */
    @Transactional
    public GovernanceDecisionResponse approve(GovernanceDecisionRequest request, UUID actorId) {
        validateRisk(request.residualRiskScore());
        recordPromptDecisionIfNeeded(request, actorId, "APPROVED");
        AuditRecordEntity record = audit("ARTIFACT_APPROVED", clean(request.artifactType()), clean(request.artifactRef()), actorId, "APPROVED", decisionEvidence(request), "COMPLIANT");
        return new GovernanceDecisionResponse(record.getId(), request.artifactType(), request.artifactRef(), "APPROVED", "COMPLIANT", record.getEventHash());
    }

    /** Rejects a governed artifact and records an immutable audit decision. */
    @Transactional
    public GovernanceDecisionResponse reject(GovernanceDecisionRequest request, UUID actorId) {
        recordPromptDecisionIfNeeded(request, actorId, "REJECTED");
        AuditRecordEntity record = audit("ARTIFACT_REJECTED", clean(request.artifactType()), clean(request.artifactRef()), actorId, "REJECTED", decisionEvidence(request), "NON_COMPLIANT");
        return new GovernanceDecisionResponse(record.getId(), request.artifactType(), request.artifactRef(), "REJECTED", "NON_COMPLIANT", record.getEventHash());
    }

    private void recordPromptDecisionIfNeeded(GovernanceDecisionRequest request, UUID actorId, String decision) {
        if (!"PROMPT".equalsIgnoreCase(request.artifactType())) return;
        UUID promptId = parseUuid(request.artifactRef());
        if (promptId == null || prompts.findById(promptId).isEmpty()) return;
        promptApprovals.save(new PromptApprovalEntity(UUID.randomUUID(), promptId, decision, clean(request.rationale()), actorId, value(request.approvalChain(), "AI_GOVERNANCE_REVIEW>RELEASE_BOARD"), Instant.now()));
    }

    private AuditRecordEntity audit(String eventType, String artifactType, String artifactRef, UUID actorId, String decision, String evidenceJson, String complianceStatus) {
        String previous = audits.findTopByOrderByCreatedAtDesc().map(AuditRecordEntity::getEventHash).orElse("GENESIS");
        Instant now = Instant.now();
        String hash = checksum(eventType + "|" + artifactType + "|" + artifactRef + "|" + actorId + "|" + decision + "|" + evidenceJson + "|" + previous + "|" + now);
        return audits.save(new AuditRecordEntity(UUID.randomUUID(), eventType, artifactType, artifactRef, actorId, decision, evidenceJson, complianceStatus, hash, previous, now));
    }

    private void validateRisk(Integer residualRiskScore) {
        if (residualRiskScore != null && residualRiskScore > 70) {
            throw new GovernanceException(HttpStatus.BAD_REQUEST, "GOVERNANCE_RISK_TOO_HIGH", "Residual risk score is too high for automatic approval");
        }
    }

    private void seedComplianceIfEmpty() {
        if (controls.count() > 0) return;
        Instant now = Instant.now();
        controls.saveAll(List.of(
                new ComplianceControlEntity(UUID.randomUUID(), "ISO_42001", "AIMS-6.2", "AI system impact assessment", "Governed artifacts require risk and approval evidence.", "IMPLEMENTED", "governance.audit_records", "Chief AI Governance Officer", now),
                new ComplianceControlEntity(UUID.randomUUID(), "NIST_AI_RMF", "GOVERN-1", "AI governance policies", "Policies define accountability, roles, and enforcement mode.", "IMPLEMENTED", "governance.governance_policies", "Principal Responsible AI Engineer", now),
                new ComplianceControlEntity(UUID.randomUUID(), "OWASP_LLM_TOP_10", "LLM01", "Prompt injection protection", "Serving and governance policies track prompt security controls.", "IMPLEMENTED", "serving.inference_requests", "Principal Security Engineer", now),
                new ComplianceControlEntity(UUID.randomUUID(), "GDPR_READY", "PRIVACY-PII", "PII classification and retention", "Dataset governance tracks classification, consent, retention, and archival policy.", "IMPLEMENTED", "governance.governance_rules", "Principal Privacy Engineer", now)));
    }

    private void seedRisksIfEmpty() {
        if (risks.count() > 0) return;
        Instant now = Instant.now();
        risks.saveAll(List.of(
                new RiskRegisterEntity(UUID.randomUUID(), "RISK-MODEL-001", "MODEL_RISK", "Model output may be over-trusted", "Decision intelligence must expose confidence, evidence, and policy compliance.", "MEDIUM", "HIGH", "HIGH", "OPEN", "Require citation, confidence, and human approval policies for high-impact outputs.", "Principal Responsible AI Engineer", now.plusSeconds(2_592_000), now, now),
                new RiskRegisterEntity(UUID.randomUUID(), "RISK-DATA-001", "DATASET_RISK", "Dataset lineage gaps", "Imported datasets require ownership, lineage, retention, and consent metadata.", "MEDIUM", "HIGH", "HIGH", "OPEN", "Reject promotion when lineage is incomplete.", "Chief Data Governance Officer", now.plusSeconds(2_592_000), now, now),
                new RiskRegisterEntity(UUID.randomUUID(), "RISK-SEC-001", "SECURITY_RISK", "Prompt manipulation", "Prompts and inference requests can attempt policy bypass.", "HIGH", "HIGH", "CRITICAL", "OPEN", "Block prompt injection patterns and audit violations.", "Chief Information Security Officer", now.plusSeconds(1_209_600), now, now)));
    }

    private String decisionEvidence(GovernanceDecisionRequest request) {
        assessments.save(new RiskAssessmentEntity(UUID.randomUUID(), null, clean(request.artifactType()), clean(request.artifactRef()), 50, request.residualRiskScore() == null ? 25 : request.residualRiskScore(), clean(request.rationale()), null, Instant.now()));
        return "{\"rationale\":\"" + clean(request.rationale()) + "\",\"approvalChain\":\"" + value(request.approvalChain(), "AI_GOVERNANCE_REVIEW>RELEASE_BOARD") + "\",\"residualRiskScore\":" + (request.residualRiskScore() == null ? 25 : request.residualRiskScore()) + "}";
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) throw new GovernanceException(HttpStatus.BAD_REQUEST, "GOVERNANCE_VALUE_REQUIRED", "Governance value is required");
        return value.replace("\"", "'").trim();
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text.replace("\"", "'").trim();
    }

    private String checksum(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new GovernanceException(HttpStatus.INTERNAL_SERVER_ERROR, "GOVERNANCE_HASH_FAILED", "Unable to calculate governance audit hash");
        }
    }
}

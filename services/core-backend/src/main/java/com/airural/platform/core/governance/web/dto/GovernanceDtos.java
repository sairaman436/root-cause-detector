/*
 * Purpose: Defines REST contracts for the AI-9 governance platform.
 * Why it exists: Governance clients need stable policy, risk, compliance, audit, approval, and rejection payloads.
 * Architecture fit: DTO boundary for enterprise AI governance APIs.
 */
package com.airural.platform.core.governance.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

/** Container for governance DTO records. */
public final class GovernanceDtos {
    private GovernanceDtos() {}

    /** Request for creating a governance policy. */
    public record GovernancePolicyRequest(@NotBlank String policyKey, @NotBlank String name, @NotBlank String description, @NotBlank String domain, String severity, String conflictStrategy, List<GovernanceRuleRequest> rules) {}

    /** Request for creating a policy rule. */
    public record GovernanceRuleRequest(@NotBlank String ruleKey, @NotBlank String ruleType, @NotBlank String expression, String enforcementMode, Integer priority) {}

    /** Governance policy response. */
    public record GovernancePolicyResponse(UUID id, String policyKey, String name, String domain, String severity, String status, List<GovernanceRuleResponse> rules) {}

    /** Governance rule response. */
    public record GovernanceRuleResponse(UUID id, String ruleKey, String ruleType, String enforcementMode) {}

    /** Governance audit response. */
    public record AuditRecordResponse(UUID id, String eventType, String artifactType, String artifactRef, String policyComplianceStatus, String eventHash, Instant createdAt) {}

    /** Compliance matrix response. */
    public record ComplianceResponse(double complianceScore, List<ComplianceControlResponse> controls) {}

    /** Compliance control response. */
    public record ComplianceControlResponse(UUID id, String framework, String controlCode, String title, String implementationStatus) {}

    /** Risk register response. */
    public record RiskResponse(long openRisks, List<RiskRegisterResponse> risks) {}

    /** Single risk response. */
    public record RiskRegisterResponse(UUID id, String riskKey, String riskType, String title, String severity, String status) {}

    /** Generic governance approval or rejection request. */
    public record GovernanceDecisionRequest(@NotBlank String artifactType, @NotBlank String artifactRef, @NotBlank String rationale, String approvalChain, Integer residualRiskScore) {}

    /** Generic governance approval or rejection response. */
    public record GovernanceDecisionResponse(UUID auditId, String artifactType, String artifactRef, String decision, String policyComplianceStatus, String eventHash) {}
}

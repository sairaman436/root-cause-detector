/*
 * Purpose: Stores executable rules attached to governance policies.
 * Why it exists: Enterprise AI controls such as citation required, human approval required, and restricted topics must be versioned separately from policy metadata.
 * Architecture fit: Child entity for AI-9 policy rule evaluation and auditability.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Governance rule entity. */
@Entity
@Table(name = "governance_rules", schema = "governance")
public class GovernanceRuleEntity {
    @Id private UUID id;
    private UUID policyId;
    private String ruleKey;
    private String ruleType;
    @Column(name = "rule_expression")
    private String expression;
    private String enforcementMode;
    private Integer priority;
    private String status;
    private Instant createdAt;

    protected GovernanceRuleEntity() {}

    /** Creates a governance rule. */
    public GovernanceRuleEntity(UUID id, UUID policyId, String ruleKey, String ruleType, String expression, String enforcementMode, Integer priority, String status, Instant createdAt) {
        this.id = id; this.policyId = policyId; this.ruleKey = ruleKey; this.ruleType = ruleType; this.expression = expression; this.enforcementMode = enforcementMode; this.priority = priority; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPolicyId() { return policyId; }
    public String getRuleKey() { return ruleKey; }
    public String getRuleType() { return ruleType; }
    public String getEnforcementMode() { return enforcementMode; }
}

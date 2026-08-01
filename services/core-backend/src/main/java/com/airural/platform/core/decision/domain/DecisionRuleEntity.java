/*
 * Purpose: Persists configurable decision rules and DSL expressions.
 * Why it exists: Policy, eligibility, constraints, conflict detection, and priority logic must be configurable.
 * Architecture fit: Rule engine catalog entity.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decision rules. */
@Entity
@Table(name = "decision_rules", schema = "decision")
public class DecisionRuleEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 120) private String ruleKey;
    @Column(nullable = false, length = 160) private String name;
    @Column(nullable = false, length = 80) private String ruleType;
    @Column(nullable = false, columnDefinition = "TEXT") private String dslExpression;
    @Column(nullable = false) private Integer priority;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionRuleEntity() {}

    public DecisionRuleEntity(String ruleKey, String name, String ruleType, String dslExpression, Integer priority, String status) {
        this.id = UUID.randomUUID();
        this.ruleKey = ruleKey;
        this.name = name;
        this.ruleType = ruleType;
        this.dslExpression = dslExpression;
        this.priority = priority;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String ruleKey() { return ruleKey; }
    public String name() { return name; }
    public String ruleType() { return ruleType; }
    public String dslExpression() { return dslExpression; }
    public Integer priority() { return priority; }
    public String status() { return status; }
}

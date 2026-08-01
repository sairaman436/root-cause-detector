/*
 * Purpose: Stores why a model and node were selected.
 * Why it exists: Model routing must be explainable across task type, latency, hardware, confidence, language, context size, user role, and policy constraints.
 * Architecture fit: Routing audit entity for the serving gateway.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Routing decision entity. */
@Entity
@Table(name = "routing_decisions", schema = "serving")
public class RoutingDecisionEntity {
    @Id private UUID id;
    private UUID inferenceRequestId;
    private UUID modelDeploymentId;
    private UUID servingNodeId;
    private String selectedModel;
    private String routingPolicy;
    private String fallbackModel;
    private Boolean fallbackEnabled;
    @Column(columnDefinition = "TEXT") private String decisionFactorsJson;
    private Instant createdAt;

    protected RoutingDecisionEntity() {}

    /** Creates a routing decision. */
    public RoutingDecisionEntity(UUID id, UUID inferenceRequestId, UUID modelDeploymentId, UUID servingNodeId, String selectedModel, String routingPolicy, String fallbackModel, Boolean fallbackEnabled, String decisionFactorsJson, Instant createdAt) {
        this.id = id; this.inferenceRequestId = inferenceRequestId; this.modelDeploymentId = modelDeploymentId; this.servingNodeId = servingNodeId; this.selectedModel = selectedModel; this.routingPolicy = routingPolicy; this.fallbackModel = fallbackModel; this.fallbackEnabled = fallbackEnabled; this.decisionFactorsJson = decisionFactorsJson; this.createdAt = createdAt;
    }
}

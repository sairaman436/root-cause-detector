/*
 * Purpose: Verifies MCP tool routing rules.
 * Why it exists: Agents must use governed tool adapters rather than direct service calls.
 * Architecture fit: Unit coverage for the task router and tool permission model.
 */
package com.airural.platform.core.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.agents.application.TaskRouter;
import org.junit.jupiter.api.Test;

/** Unit tests for task routing. */
class TaskRouterTests {
    private final TaskRouter router = new TaskRouter();

    @Test
    void rootCauseAgentRoutesThroughEvidenceKnowledgeRagAndAi() {
        assertThat(router.toolsFor("root-cause-analysis-agent"))
                .containsExactly("evidence.service", "knowledge.service", "rag.service", "ai.foundation");
    }
}

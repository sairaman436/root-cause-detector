/*
 * Purpose: Verifies deterministic agent planning.
 * Why it exists: Milestone 9 requires planner coverage for routing objectives to specialized agents.
 * Architecture fit: Unit coverage for the task planner.
 */
package com.airural.platform.core.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.agents.application.TaskPlanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for task planning. */
class TaskPlannerTests {
    private final TaskPlanner planner = new TaskPlanner(new ObjectMapper());

    @Test
    void plansSpecializedAgentsFromObjective() {
        List<String> agents = planner.planAgents("Find root cause and recommend a policy response from survey data", null);
        assertThat(agents).contains("root-cause-analysis-agent", "recommendation-agent", "policy-knowledge-agent", "survey-intelligence-agent");
    }

    @Test
    void honorsPreferredAgents() {
        assertThat(planner.planAgents("anything", List.of("analytics-agent"))).containsExactly("analytics-agent");
    }
}

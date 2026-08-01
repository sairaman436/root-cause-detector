/*
 * Purpose: Verifies standard MCP-style tool adapter behavior.
 * Why it exists: Tool tests must prove agents receive standardized data and citation outputs through adapters.
 * Architecture fit: Unit coverage for the tool system.
 */
package com.airural.platform.core.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.agents.application.PlatformAgentTool;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for platform tool adapters. */
class PlatformAgentToolTests {
    @Test
    void toolInvocationReturnsDataAndCitations() {
        var result = new PlatformAgentTool("survey.service", "Survey Service").invoke(Map.of("objective", "summarize surveys"));
        assertThat(result.success()).isTrue();
        assertThat(result.data()).containsEntry("toolKey", "survey.service");
        assertThat(result.citations()).isNotEmpty();
    }
}

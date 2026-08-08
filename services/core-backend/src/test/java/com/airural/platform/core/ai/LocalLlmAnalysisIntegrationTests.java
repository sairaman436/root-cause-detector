/*
 * Purpose: Verifies local LLM analysis API integration with the provider-neutral inference service boundary.
 * Why it exists: Root-cause analysis must persist only validated structured output and surface provider failures clearly.
 * Architecture fit: API-level coverage for the backend AI application service without requiring Ollama in CI.
 */
package com.airural.platform.core.ai;

import static org.assertj.core.api.Assertions.*;

import com.airural.platform.core.ai.application.AiException;
import com.airural.platform.core.ai.application.LocalLlmAnalysisService;
import com.airural.platform.core.ai.infrastructure.LlmAnalysisResultRepository;
import com.airural.platform.core.ai.web.dto.AiDtos.LlmAnalysisRequest;
import com.airural.platform.core.identity.domain.UserAccountEntity;
import com.airural.platform.core.identity.infrastructure.OrganizationRepository;
import com.airural.platform.core.identity.infrastructure.RoleRepository;
import com.airural.platform.core.identity.infrastructure.UserAccountRepository;
import com.airural.platform.core.survey.domain.SurveyEntity;
import com.airural.platform.core.survey.infrastructure.SurveyRepository;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/** Integration tests for local LLM analysis. */
@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_local_llm_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class LocalLlmAnalysisIntegrationTests {
    private static final UUID PLATFORM_ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static HttpServer inferenceServer;
    private static volatile String responseMode = "valid";

    @Autowired private LocalLlmAnalysisService service;
    @Autowired private OrganizationRepository organizations;
    @Autowired private RoleRepository roles;
    @Autowired private UserAccountRepository users;
    @Autowired private SurveyRepository surveys;
    @Autowired private LlmAnalysisResultRepository results;

    @BeforeAll
    static void startInferenceServer() throws IOException {
        ensureInferenceServer();
    }

    private static synchronized void ensureInferenceServer() throws IOException {
        if (inferenceServer != null) {
            return;
        }
        inferenceServer = HttpServer.create(new InetSocketAddress(0), 0);
        inferenceServer.createContext("/v1/analysis/root-cause", exchange -> {
            String body = switch (responseMode) {
                case "malformed" -> """
                        {
                          "request_id":"00000000-0000-0000-0000-000000000000",
                          "provider":"ollama",
                          "model":"qwen-test",
                          "prompt_id":"ROOT_CAUSE_ANALYSIS",
                          "prompt_version":"1.0.0",
                          "output":{"summary":"missing required fields"},
                          "latency_ms":12,
                          "tokens_estimate":34,
                          "success":true
                        }
                        """;
                case "provider-error" -> """
                        {"detail":{"code":"OLLAMA_MODEL_UNAVAILABLE","message":"Ollama model 'qwen-test' is not installed."}}
                        """;
                default -> """
                        {
                          "request_id":"00000000-0000-0000-0000-000000000000",
                          "provider":"ollama",
                          "model":"qwen-test",
                          "model_version":"qwen-test",
                          "prompt_id":"ROOT_CAUSE_ANALYSIS",
                          "prompt_version":"1.0.0",
                          "output":{
                            "problem":"Water access failure",
                            "summary":"Water access is constrained by repair delays and weak maintenance capacity.",
                            "contributing_factors":["Repair backlog"],
                            "root_causes":["Weak local maintenance capacity"],
                            "evidence":["Survey answer water_source=well"],
                            "confidence":0.76,
                            "recommendations":["Prioritize accountable maintenance routing"],
                            "limitations":["Requires field validation"]
                          },
                          "latency_ms":12,
                          "tokens_estimate":34,
                          "success":true
                        }
                        """;
            };
            int status = "provider-error".equals(responseMode) ? 503 : 200;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        inferenceServer.start();
    }

    @AfterAll
    static void stopInferenceServer() {
        if (inferenceServer != null) {
            inferenceServer.stop(0);
        }
    }

    @DynamicPropertySource
    static void inferenceProperties(DynamicPropertyRegistry registry) {
        try {
            ensureInferenceServer();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to start fake inference server", ex);
        }
        registry.add("airural.ai.gateway.inference-service-url", () -> "http://localhost:" + inferenceServer.getAddress().getPort());
        registry.add("airural.ai.gateway.default-model", () -> "qwen-test");
    }

    /** Valid provider output is persisted and returned with model and prompt metadata. */
    @Test
    void rootCauseAnalysisPersistsValidatedStructuredOutput() throws Exception {
        responseMode = "valid";
        TestContext context = seedContext("valid");

        var analysis = service.analyzeRootCause(
                new LlmAnalysisRequest(context.surveyId(), null, "Water access failure", "qwen-test", List.of(), List.of()),
                context.userId());

        assertThat(analysis.provider()).isEqualTo("ollama");
        assertThat(analysis.model()).isEqualTo("qwen-test");
        assertThat(analysis.promptId()).isEqualTo("ROOT_CAUSE_ANALYSIS");
        assertThat(analysis.output().rootCauses()).containsExactly("Weak local maintenance capacity");
        assertThat(results.findById(analysis.id())).isPresent();
    }

    /** Malformed model output is rejected before application code can treat it as success. */
    @Test
    void rootCauseAnalysisRejectsMalformedProviderOutput() throws Exception {
        responseMode = "malformed";
        TestContext context = seedContext("malformed");

        assertThatThrownBy(() -> service.analyzeRootCause(
                new LlmAnalysisRequest(context.surveyId(), null, "Water access failure", "qwen-test", List.of(), List.of()),
                context.userId()))
                .isInstanceOf(AiException.class)
                .extracting("code")
                .isEqualTo("AI_INVALID_ANALYSIS_OUTPUT");
    }

    /** Provider operational failures are surfaced as clear API errors instead of deterministic success. */
    @Test
    void rootCauseAnalysisReturnsOperationalProviderError() throws Exception {
        responseMode = "provider-error";
        TestContext context = seedContext("error");

        assertThatThrownBy(() -> service.analyzeRootCause(
                new LlmAnalysisRequest(context.surveyId(), null, "Water access failure", "qwen-test", List.of(), List.of()),
                context.userId()))
                .isInstanceOf(AiException.class)
                .extracting("code")
                .isEqualTo("OLLAMA_MODEL_UNAVAILABLE");

        assertThat(results.findAll()).anySatisfy(result -> {
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.errorCode()).isEqualTo("OLLAMA_MODEL_UNAVAILABLE");
        });
    }

    private TestContext seedContext(String suffix) {
        var organization = organizations.findByCode("PLATFORM").orElseThrow();
        var role = roles.findByName("ADMINISTRATOR").orElseThrow();
        UserAccountEntity user = users.save(new UserAccountEntity(
                organization,
                "llm." + suffix + "." + UUID.randomUUID(),
                "llm." + suffix + "." + UUID.randomUUID() + "@example.gov",
                "LLM Test User",
                null,
                "not-used-in-service-test",
                Set.of(role)));
        SurveyEntity survey = surveys.save(new SurveyEntity(
                null,
                PLATFORM_ORGANIZATION_ID,
                user.id(),
                "Local LLM Survey " + suffix,
                "Local LLM integration test",
                Set.of("llm")));
        return new TestContext(user.id(), survey.id());
    }

    private record TestContext(UUID userId, UUID surveyId) {}
}

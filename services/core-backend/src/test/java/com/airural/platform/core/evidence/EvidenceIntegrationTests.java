/*
 * Purpose: Verifies evidence APIs, storage, persistence, RBAC, search, versioning, and audit behavior end to end.
 * Why it exists: Milestone 4 requires production-grade integration coverage for the evidence module.
 * Architecture fit: API and persistence integration tests against the Spring Boot backend.
 */
package com.airural.platform.core.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.airural.platform.core.identity.application.JwtTokenService;
import com.airural.platform.core.identity.domain.*;
import com.airural.platform.core.identity.infrastructure.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for Enterprise Evidence and Asset Management. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:airural_evidence_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "airural.evidence.local-storage-path=./target/evidence-test-storage",
        "airural.evidence.max-file-size-bytes=1048576"
})
class EvidenceIntegrationTests {
    private static final UUID PLATFORM_ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    /** Evidence APIs support upload, duplicate validation, search, download, metadata updates, delete, restore, versions, and audit. */
    @Test
    void evidenceManagementFlowWorksThroughSecuredApis() throws Exception {
        String token = registerAdmin();

        UUID surveyId = UUID.fromString(json(postJson("/api/v1/surveys", token, """
                {
                  "organizationId": "%s",
                  "name": "Evidence Survey",
                  "tags": ["evidence"]
                }
                """.formatted(PLATFORM_ORGANIZATION_ID))).at("/data/id").asText());

        JsonNode uploaded = json(upload(token, surveyId, "water photo.png", "image/png", "image-data"));
        UUID evidenceId = UUID.fromString(uploaded.at("/data/id").asText());
        assertThat(uploaded.at("/data/evidenceType").asText()).isEqualTo("IMAGE");
        assertThat(uploaded.at("/data/tags/0").asText()).isEqualTo("field");

        uploadExpect(token, surveyId, "duplicate.png", "image/png", "image-data", status().isConflict());

        JsonNode search = json(getJson("/api/v1/evidence?surveyId=" + surveyId + "&tag=field&evidenceType=IMAGE", token));
        assertThat(search.at("/data/content").size()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/evidence/" + evidenceId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes("image-data".getBytes(StandardCharsets.UTF_8)));

        JsonNode updated = json(putJson("/api/v1/evidence/" + evidenceId + "/metadata", token, """
                {
                  "title": "Updated evidence title",
                  "description": "Validated village water source",
                  "customMetadataJson": "{\\"quality\\":\\"reviewed\\"}",
                  "tags": ["field", "reviewed"]
                }
                """));
        assertThat(updated.at("/data/currentVersion").asInt()).isEqualTo(2);

        JsonNode versions = json(getJson("/api/v1/evidence/" + evidenceId + "/versions", token));
        assertThat(versions.at("/data").size()).isEqualTo(2);

        deleteJson("/api/v1/evidence/" + evidenceId, token);
        mockMvc.perform(get("/api/v1/evidence/" + evidenceId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        JsonNode restored = json(postJson("/api/v1/evidence/" + evidenceId + "/restore", token, ""));
        assertThat(restored.at("/data/active").asBoolean()).isTrue();

        JsonNode audit = json(getJson("/api/v1/evidence/" + evidenceId + "/audit", token));
        assertThat(audit.at("/data").size()).isGreaterThanOrEqualTo(5);
    }

    /** Analyst users can read and download evidence, but cannot upload or modify it. */
    @Test
    void evidenceRbacBlocksUnauthorizedMutation() throws Exception {
        String analystToken = createAnalystToken();
        MockMultipartFile file = new MockMultipartFile("file", "analyst.png", "image/png", "content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/evidence")
                        .file(file)
                        .param("organizationId", PLATFORM_ORGANIZATION_ID.toString())
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isForbidden());
    }

    private String upload(String token, UUID surveyId, String fileName, String mimeType, String content) throws Exception {
        return uploadExpect(token, surveyId, fileName, mimeType, content, status().isOk());
    }

    private String uploadExpect(String token, UUID surveyId, String fileName, String mimeType, String content, org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", fileName, mimeType, content.getBytes(StandardCharsets.UTF_8));
        return mockMvc.perform(multipart("/api/v1/evidence")
                        .file(file)
                        .param("organizationId", PLATFORM_ORGANIZATION_ID.toString())
                        .param("surveyId", surveyId.toString())
                        .param("title", "Field evidence")
                        .param("description", "Uploaded during integration testing")
                        .param("tags", "field")
                        .header("Authorization", "Bearer " + token))
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String registerAdmin() throws Exception {
        String payload = """
                {
                  "username": "evidence.admin",
                  "email": "evidence.admin@example.gov",
                  "fullName": "Evidence Admin",
                  "password": "VeryStrongPassword123!",
                  "organizationCode": "PLATFORM"
                }
                """;
        return json(postJson("/api/v1/auth/register", null, payload)).at("/data/accessToken").asText();
    }

    private String createAnalystToken() {
        OrganizationEntity organization = organizationRepository.findByCode("PLATFORM").orElseThrow();
        RoleEntity analyst = roleRepository.findByName("ANALYST").orElseThrow();
        UserAccountEntity user = userRepository.save(new UserAccountEntity(
                organization,
                "evidence.analyst",
                "evidence.analyst@example.gov",
                "Evidence Analyst",
                null,
                passwordEncoder.encode("VeryStrongPassword123!"),
                Set.of(analyst)));
        return jwtTokenService.issue(user).token();
    }

    private String postJson(String path, String token, String payload) throws Exception {
        var request = post(path).contentType(MediaType.APPLICATION_JSON).content(payload);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String putJson(String path, String token, String payload) throws Exception {
        return mockMvc.perform(put(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void deleteJson(String path, String token) throws Exception {
        mockMvc.perform(delete(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String getJson(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}

/*
 * Purpose: Verifies hierarchy, spatial APIs, persistence, RBAC, and integration links end to end.
 * Why it exists: Milestone 5 requires production-grade coverage for geospatial hierarchy and spatial service workflows.
 * Architecture fit: API integration tests against the Spring Boot backend using the secured REST surface.
 */
package com.airural.platform.core.geospatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.airural.platform.core.identity.application.JwtTokenService;
import com.airural.platform.core.identity.domain.*;
import com.airural.platform.core.identity.infrastructure.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for Enterprise Geospatial Intelligence. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_geospatial_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class GeospatialIntegrationTests {
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

    /** Full hierarchy and spatial workflows work through secured APIs. */
    @Test
    void geospatialManagementFlowWorksThroughSecuredApis() throws Exception {
        String token = registerAdmin();

        UUID countryId = id(postJson("/api/v1/geospatial/admin/countries", token, """
                {"code":"IND","name":"India","isoCode":"IND","latitude":20.5937,"longitude":78.9629}
                """));
        UUID stateId = id(adminUnit("/api/v1/geospatial/admin/states", token, countryId, "TS", "Telangana", "17.8749", "78.1000"));
        UUID districtId = id(adminUnit("/api/v1/geospatial/admin/districts", token, stateId, "HYD", "Hyderabad", "17.3850", "78.4867"));
        UUID mandalId = id(adminUnit("/api/v1/geospatial/admin/mandals", token, districtId, "MDL", "Sample Mandal", "17.3900", "78.4900"));
        UUID blockId = id(adminUnit("/api/v1/geospatial/admin/blocks", token, mandalId, "BLK", "Sample Block", "17.4000", "78.5000"));
        UUID gpId = id(adminUnit("/api/v1/geospatial/admin/gram-panchayats", token, blockId, "GP1", "Sample Gram Panchayat", "17.4100", "78.5100"));

        UUID villageId = id(postJson("/api/v1/geospatial/admin/villages", token, """
                {
                  "gramPanchayatId":"%s",
                  "code":"VIL1",
                  "name":"Sample Village",
                  "latitude":17.4200,
                  "longitude":78.5200,
                  "elevationMeters":540.5,
                  "areaSqKm":4.25,
                  "population":2500,
                  "householdCount":550,
                  "geojson":"{\\"type\\":\\"Feature\\",\\"geometry\\":{\\"type\\":\\"Point\\",\\"coordinates\\":[78.52,17.42]}}",
                  "minLatitude":17.4000,
                  "minLongitude":78.5000,
                  "maxLatitude":17.4500,
                  "maxLongitude":78.5500
                }
                """.formatted(gpId)));
        UUID wardId = id(adminUnit("/api/v1/geospatial/admin/wards", token, villageId, "W1", "Ward One", "17.4210", "78.5210"));
        UUID hamletId = id(adminUnit("/api/v1/geospatial/admin/hamlets", token, wardId, "H1", "Hamlet One", "17.4220", "78.5220"));

        UUID assetId = id(postJson("/api/v1/geospatial/infrastructure-assets", token, """
                {
                  "villageId":"%s",
                  "assetType":"PHC",
                  "code":"PHC-001",
                  "name":"Primary Health Center",
                  "description":"Rural health facility",
                  "latitude":17.4230,
                  "longitude":78.5230,
                  "metadataJson":"{\\"beds\\":8}"
                }
                """.formatted(villageId)));
        assertThat(assetId).isNotNull();

        UUID householdId = id(postJson("/api/v1/geospatial/households", token, """
                {
                  "hamletId":"%s",
                  "householdCode":"HH-001",
                  "headOfHousehold":"Asha Rao",
                  "address":"Door 1, Hamlet One",
                  "latitude":17.4240,
                  "longitude":78.5240,
                  "iotMetadataJson":"{\\"meterReady\\":true}"
                }
                """.formatted(hamletId)));

        JsonNode villages = json(getJson("/api/v1/geospatial/admin/villages?name=Sample", token));
        assertThat(villages.at("/data/content").size()).isEqualTo(1);

        JsonNode radius = json(getJson("/api/v1/geospatial/search/villages/radius?latitude=17.4200&longitude=78.5200&radiusKm=2", token));
        assertThat(radius.at("/data/content/0/distanceKm").asDouble()).isLessThan(0.1);

        JsonNode bbox = json(getJson("/api/v1/geospatial/search/villages/bbox?minLatitude=17.0&minLongitude=78.0&maxLatitude=18.0&maxLongitude=79.0", token));
        assertThat(bbox.at("/data/content").size()).isEqualTo(1);

        JsonNode nearest = json(getJson("/api/v1/geospatial/search/assets/nearest?latitude=17.4200&longitude=78.5200&assetType=PHC&radiusKm=5&limit=5", token));
        assertThat(nearest.at("/data/0/name").asText()).isEqualTo("Primary Health Center");

        JsonNode households = json(getJson("/api/v1/geospatial/households?hamletId=" + hamletId, token));
        assertThat(households.at("/data/content/0/id").asText()).isEqualTo(householdId.toString());

        JsonNode hierarchy = json(getJson("/api/v1/geospatial/admin/households/" + householdId + "/hierarchy", token));
        assertThat(hierarchy.at("/data/pathCode").asText()).contains("IND/TS/HYD");

        JsonNode boundary = json(postJson("/api/v1/geospatial/boundaries", token, """
                {
                  "entityType":"VILLAGE",
                  "entityId":"%s",
                  "geojson":"{\\"type\\":\\"FeatureCollection\\",\\"features\\":[]}",
                  "minLatitude":17.4000,
                  "minLongitude":78.5000,
                  "maxLatitude":17.4500,
                  "maxLongitude":78.5500,
                  "areaSqKm":4.25
                }
                """.formatted(villageId)));
        assertThat(boundary.at("/data/entityType").asText()).isEqualTo("VILLAGE");
        UUID boundaryId = UUID.fromString(boundary.at("/data/id").asText());

        JsonNode geographySearch = json(getJson("/api/v1/geography/search?query=Sample", token));
        assertThat(geographySearch.at("/data/content").size()).isEqualTo(1);

        JsonNode geographyNearby = json(getJson("/api/v1/geography/nearby?latitude=17.4200&longitude=78.5200&assetType=PHC&radiusKm=5&limit=5", token));
        assertThat(geographyNearby.at("/data/0/name").asText()).isEqualTo("Primary Health Center");

        JsonNode geographyBoundary = json(getJson("/api/v1/geography/boundary/" + boundaryId, token));
        assertThat(geographyBoundary.at("/data/entityId").asText()).isEqualTo(villageId.toString());

        JsonNode geographyHierarchy = json(getJson("/api/v1/geography/hierarchy?householdId=" + householdId, token));
        assertThat(geographyHierarchy.at("/data/pathName").asText()).contains("Sample Village");

        JsonNode geographyInfrastructure = json(getJson("/api/v1/geography/infrastructure?villageId=" + villageId, token));
        assertThat(geographyInfrastructure.at("/data/content/0/id").asText()).isEqualTo(assetId.toString());

        JsonNode distance = json(getJson("/api/v1/geospatial/search/distance?fromLatitude=17.4200&fromLongitude=78.5200&toLatitude=17.4230&toLongitude=78.5230", token));
        assertThat(distance.at("/data/distanceKm").asDouble()).isGreaterThan(0);

        JsonNode clusters = json(getJson("/api/v1/geospatial/search/villages/clusters?gridSizeDegrees=0.1", token));
        assertThat(clusters.at("/data/0/villageCount").asLong()).isEqualTo(1);
    }

    /** Read-only roles can query geography but cannot mutate hierarchy records. */
    @Test
    void geospatialRbacBlocksUnauthorizedMutation() throws Exception {
        String token = createAnalystToken();

        mockMvc.perform(get("/api/v1/geospatial/search/distance?fromLatitude=17.1&fromLongitude=78.1&toLatitude=17.2&toLongitude=78.2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/geography/search?query=none")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/geospatial/admin/countries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"NPL\",\"name\":\"Nepal\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/geography/countries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BTN\",\"name\":\"Bhutan\"}"))
                .andExpect(status().isForbidden());
    }

    private String registerAdmin() throws Exception {
        return json(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"geo.admin",
                                  "email":"geo.admin@example.gov",
                                  "fullName":"Geo Admin",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).at("/data/accessToken").asText();
    }

    private String createAnalystToken() {
        OrganizationEntity organization = organizationRepository.findByCode("PLATFORM").orElseThrow();
        RoleEntity analyst = roleRepository.findByName("ANALYST").orElseThrow();
        UserAccountEntity user = userRepository.save(new UserAccountEntity(
                organization,
                "geo.analyst",
                "geo.analyst@example.gov",
                "Geo Analyst",
                null,
                passwordEncoder.encode("VeryStrongPassword123!"),
                Set.of(analyst)));
        return jwtTokenService.issue(user).token();
    }

    private String adminUnit(String path, String token, UUID parentId, String code, String name, String latitude, String longitude) throws Exception {
        return postJson(path, token, """
                {"parentId":"%s","code":"%s","name":"%s","latitude":%s,"longitude":%s}
                """.formatted(parentId, code, name, latitude, longitude));
    }

    private String postJson(String path, String token, String payload) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String getJson(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private UUID id(String response) throws Exception {
        return UUID.fromString(json(response).at("/data/id").asText());
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}

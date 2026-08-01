/*
 * Purpose: Configures backend authentication and RBAC enforcement.
 * Why it exists: Identity endpoints need public auth routes, protected user routes, and admin-only management routes.
 * Architecture fit: Implements the approved Spring Security and RBAC foundation.
 */
package com.airural.platform.core.identity.security;

import com.airural.platform.core.common.ErrorResponse;
import com.airural.platform.core.common.RequestIds;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Spring Security configuration for the core backend. */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    /** Configures stateless HTTP security. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/api/v1/openapi/**",
                                "/api/v1/docs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/logout", "/api/v1/users/me")
                        .authenticated()
                        .requestMatchers("/api/v1/admin/users", "/api/v1/admin/users/**")
                        .hasAuthority("USER_MANAGE")
                        .requestMatchers("/api/v1/admin/organizations", "/api/v1/admin/organizations/**")
                        .hasAuthority("ORGANIZATION_MANAGE")
                        .requestMatchers("/api/v1/admin/roles", "/api/v1/admin/roles/**")
                        .hasAuthority("ROLE_MANAGE")
                        .requestMatchers("/api/v1/admin/permissions", "/api/v1/admin/permissions/**")
                        .hasAuthority("PERMISSION_READ")
                        .requestMatchers(HttpMethod.GET, "/api/v1/surveys", "/api/v1/surveys/**", "/api/v1/survey-templates", "/api/v1/survey-templates/**", "/api/v1/question-types")
                        .hasAuthority("SURVEY_READ")
                        .requestMatchers("/api/v1/surveys/*/assignments", "/api/v1/surveys/*/assignments/**")
                        .hasAuthority("SURVEY_ASSIGN")
                        .requestMatchers("/api/v1/surveys/*/workflow", "/api/v1/surveys/*/archive")
                        .hasAuthority("SURVEY_PUBLISH")
                        .requestMatchers("/api/v1/surveys", "/api/v1/surveys/**", "/api/v1/survey-templates", "/api/v1/survey-templates/**")
                        .hasAuthority("SURVEY_MANAGE")
                        .requestMatchers("/api/v1/evidence/*/download", "/api/v1/evidence/*/signed-url")
                        .hasAuthority("EVIDENCE_DOWNLOAD")
                        .requestMatchers(HttpMethod.GET, "/api/v1/evidence", "/api/v1/evidence/**")
                        .hasAuthority("EVIDENCE_READ")
                        .requestMatchers("/api/v1/evidence", "/api/v1/evidence/**")
                        .hasAuthority("EVIDENCE_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/geospatial", "/api/v1/geospatial/**")
                        .hasAuthority("GEO_READ")
                        .requestMatchers("/api/v1/geospatial", "/api/v1/geospatial/**")
                        .hasAuthority("GEO_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/geography", "/api/v1/geography/**")
                        .hasAnyAuthority("GEO_VIEWER", "GEO_READ", "GEO_EDITOR", "GEO_ADMIN")
                        .requestMatchers("/api/v1/geography/infrastructure", "/api/v1/geography/infrastructure/**")
                        .hasAnyAuthority("INFRASTRUCTURE_MANAGE", "GEO_ADMIN", "GEO_MANAGE")
                        .requestMatchers("/api/v1/geography", "/api/v1/geography/**")
                        .hasAnyAuthority("GEO_EDITOR", "GEO_ADMIN", "GEO_MANAGE")
                        .requestMatchers("/api/v1/events/replay")
                        .hasAuthority("EVENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/audit")
                        .hasAnyAuthority("AUDIT_VIEWER", "EVENT_ADMIN", "AUDIT_READ")
                        .requestMatchers(HttpMethod.GET, "/api/v1/events", "/api/v1/events/**")
                        .hasAnyAuthority("EVENT_VIEWER", "EVENT_ADMIN")
                        .requestMatchers("/api/v1/ai/models/register")
                        .hasAuthority("AI_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ai/prompts")
                        .hasAnyAuthority("PROMPT_ENGINEER", "AI_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ai/usage", "/api/v1/ai/inference")
                        .hasAnyAuthority("AI_AUDITOR", "AI_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ai/models", "/api/v1/ai/prompts")
                        .hasAnyAuthority("AI_READ", "AI_OPERATOR", "PROMPT_ENGINEER", "AI_AUDITOR", "AI_ADMIN")
                        .requestMatchers("/api/v1/ai/chat", "/api/v1/ai/embed", "/api/v1/ai/rag/query")
                        .hasAnyAuthority("AI_OPERATOR", "AI_ADMIN")
                        .requestMatchers("/api/v1/agents/feedback")
                        .hasAnyAuthority("AGENT_EXECUTE", "AGENT_ADMIN", "POLICY_REVIEWER")
                        .requestMatchers("/api/v1/agents/chat", "/api/v1/agents/execute")
                        .hasAnyAuthority("AGENT_EXECUTE", "AGENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/agents", "/api/v1/agents/**")
                        .hasAnyAuthority("AGENT_READ", "AGENT_EXECUTE", "AGENT_ADMIN", "AI_AUDITOR", "AI_ADMIN")
                        .requestMatchers("/api/v1/decision/analyze", "/api/v1/decision/root-cause", "/api/v1/decision/recommend")
                        .hasAnyAuthority("DECISION_ANALYZE", "DECISION_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/decision", "/api/v1/decision/**")
                        .hasAnyAuthority("DECISION_READ", "DECISION_REVIEW", "DECISION_ADMIN", "AI_AUDITOR", "AI_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/platform", "/api/v1/platform/**")
                        .hasAnyAuthority("PLATFORM_READ", "PLATFORM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/datasets", "/api/v1/datasets/**")
                        .hasAnyAuthority("DATASET_READ", "DATASET_ENGINEER", "DATASET_REVIEW", "DATASET_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .requestMatchers("/api/v1/datasets", "/api/v1/datasets/**")
                        .hasAnyAuthority("DATASET_ENGINEER", "DATASET_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/knowledge", "/api/v1/knowledge/**", "/knowledge", "/knowledge/**")
                        .hasAnyAuthority("KNOWLEDGE_READ", "KNOWLEDGE_ENGINEER", "KNOWLEDGE_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .requestMatchers("/api/v1/knowledge", "/api/v1/knowledge/**", "/knowledge", "/knowledge/**")
                        .hasAnyAuthority("KNOWLEDGE_ENGINEER", "KNOWLEDGE_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/training", "/api/v1/training/**", "/training", "/training/**")
                        .hasAnyAuthority("TRAINING_READ", "TRAINING_ENGINEER", "MODEL_REGISTRY_READ", "MLOPS_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .requestMatchers("/api/v1/training/checkpoints/restore", "/training/checkpoints/restore")
                        .hasAnyAuthority("CHECKPOINT_RESTORE", "TRAINING_ENGINEER", "MLOPS_ADMIN", "AI_ADMIN")
                        .requestMatchers("/api/v1/training", "/api/v1/training/**", "/training", "/training/**")
                        .hasAnyAuthority("TRAINING_ENGINEER", "TRAINING_ADMIN", "MLOPS_ADMIN", "AI_SCIENTIST", "AI_ADMIN")
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> writeError(
                                response,
                                ErrorResponse.of(
                                        "AUTHENTICATION_REQUIRED",
                                        "Authentication is required",
                                        List.of(),
                                        RequestIds.from(request),
                                        RequestIds.from(request)),
                                HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) -> writeError(
                                response,
                                ErrorResponse.of(
                                        "ACCESS_DENIED",
                                        "Access denied",
                                        List.of(),
                                        RequestIds.from(request),
                                        RequestIds.from(request)),
                                HttpServletResponse.SC_FORBIDDEN)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Provides BCrypt password hashing. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Prevents Spring Boot from provisioning an unused generated development user. */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Username/password authentication is disabled");
        };
    }

    private void writeError(HttpServletResponse response, ErrorResponse error, int status) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}

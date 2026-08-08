/*
 * Purpose: Applies a lightweight per-client HTTP rate limit.
 * Why it exists: RC1 must provide basic abuse protection for authentication, upload, AI, and report APIs without requiring external gateways in local deployments.
 * Architecture fit: Cross-cutting servlet filter that complements, but does not replace, production ingress or API gateway rate limits.
 */
package com.airural.platform.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** In-memory fixed-window rate limiter for local and single-node deployments. */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final int maxRequests;
    private final Duration windowDuration;

    public RateLimitingFilter(
            ObjectMapper objectMapper,
            @Value("${airural.security.rate-limit.enabled:true}") boolean enabled,
            @Value("${airural.security.rate-limit.max-requests:240}") int maxRequests,
            @Value("${airural.security.rate-limit.window-seconds:60}") long windowSeconds) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowDuration = Duration.ofSeconds(windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled || path.startsWith("/actuator") || path.startsWith("/api/v1/openapi") || path.startsWith("/api/v1/docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request);
        Window window = windows.compute(key, (ignored, current) -> nextWindow(current));
        if (window.count > maxRequests) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(windowDuration.toSeconds()));
            String requestId = RequestIds.from(request);
            objectMapper.writeValue(
                    response.getOutputStream(),
                    ErrorResponse.of("RATE_LIMIT_EXCEEDED", "Too many requests", List.of(), requestId, requestId));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Window nextWindow(Window current) {
        Instant now = Instant.now();
        if (current == null || now.isAfter(current.started.plus(windowDuration))) {
            return new Window(now, 1);
        }
        return new Window(current.started, current.count + 1);
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
        return ip + ":" + request.getRequestURI();
    }

    private record Window(Instant started, int count) {}
}

/*
 * Purpose: Authenticates requests that carry JWT bearer tokens.
 * Why it exists: Protected REST APIs require stateless access token validation.
 * Architecture fit: Implements the approved JWT authentication boundary.
 */
package com.airural.platform.core.identity.security;

import com.airural.platform.core.identity.application.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Servlet filter that creates authentication from a valid JWT. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtTokenService.parse(header.substring(7));
                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                Collection<SimpleGrantedAuthority> authorities = authorities(claims);
                AuthenticatedUser principal = new AuthenticatedUser(userId, email, authorities);
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> authorities(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);
        return java.util.stream.Stream.concat(
                        roles == null ? java.util.stream.Stream.empty() : roles.stream().map(role -> "ROLE_" + role),
                        permissions == null ? java.util.stream.Stream.empty() : permissions.stream())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}

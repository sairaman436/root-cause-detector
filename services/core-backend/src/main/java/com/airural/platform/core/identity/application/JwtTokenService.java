/*
 * Purpose: Issues and validates JWT access tokens.
 * Why it exists: REST APIs use stateless bearer authentication with RBAC authorities.
 * Architecture fit: Implements the approved JWT authentication layer.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.identity.domain.PermissionEntity;
import com.airural.platform.core.identity.domain.RoleEntity;
import com.airural.platform.core.identity.domain.UserAccountEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** JWT issuer and validator for access tokens. */
@Service
public class JwtTokenService {
    private final String issuer;
    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtTokenService(
            @Value("${airural.security.jwt.issuer}") String issuer,
            @Value("${airural.security.jwt.secret}") String secret,
            @Value("${airural.security.jwt.access-token-minutes}") long accessTokenMinutes) {
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
    }

    /** Issues an access token for a user. */
    public IssuedAccessToken issue(UserAccountEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenMinutes * 60);
        List<String> roles = user.roles().stream().map(RoleEntity::name).sorted().toList();
        List<String> permissions = user.roles().stream()
                .flatMap(role -> role.permissions().stream())
                .map(PermissionEntity::name)
                .distinct()
                .sorted()
                .toList();

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedAccessToken(token, expiresAt);
    }

    /** Parses and validates a JWT access token. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).build().parseSignedClaims(token).getPayload();
    }

    /** Returns the user ID encoded in a validated token. */
    public UUID subjectUserId(String token) {
        return UUID.fromString(parse(token).getSubject());
    }

    /** Issued access token plus expiry. */
    public record IssuedAccessToken(String token, Instant expiresAt) {
    }
}

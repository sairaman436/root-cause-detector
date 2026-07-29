/*
 * Purpose: Implements registration, login, refresh, and logout workflows.
 * Why it exists: Centralizes credential verification and token lifecycle rules outside controllers.
 * Architecture fit: Application service for the approved authentication module.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.identity.domain.*;
import com.airural.platform.core.events.application.OutboxService;
import com.airural.platform.core.identity.infrastructure.*;
import com.airural.platform.core.identity.web.dto.AuthDtos.*;
import com.airural.platform.shared.events.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for authentication workflows. */
@Service
public class AuthenticationService {
    private static final String DEFAULT_ROLE = "FIELD_SURVEYOR";
    private final UserAccountRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenHashingService tokenHashingService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenDays;

    public AuthenticationService(
            UserAccountRepository userRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            TokenHashingService tokenHashingService,
            AuditService auditService,
            OutboxService outboxService,
            @Value("${airural.security.jwt.refresh-token-days}") long refreshTokenDays) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenHashingService = tokenHashingService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.refreshTokenDays = refreshTokenDays;
    }

    /** Registers a user with the default non-administrative role. */
    @Transactional
    public TokenResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IdentityException("EMAIL_ALREADY_REGISTERED", "Email is already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IdentityException("USERNAME_ALREADY_REGISTERED", "Username is already registered", HttpStatus.CONFLICT);
        }
        OrganizationEntity organization = organizationRepository
                .findByCode(request.organizationCode())
                .orElseThrow(() -> new IdentityException("ORGANIZATION_NOT_FOUND", "Organization was not found", HttpStatus.BAD_REQUEST));
        String roleName = userRepository.count() == 0 ? "ADMINISTRATOR" : DEFAULT_ROLE;
        RoleEntity role = roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new IdentityException("ROLE_NOT_FOUND", "Default role was not found", HttpStatus.INTERNAL_SERVER_ERROR));

        UserAccountEntity user = userRepository.save(new UserAccountEntity(
                organization,
                request.username(),
                request.email(),
                request.fullName(),
                request.phoneNumber(),
                passwordEncoder.encode(request.password()),
                Set.of(role)));
        auditService.record(user.id(), "USER_REGISTERED", AuditOutcome.SUCCESS, ipAddress, userAgent, user.email());
        publishUser(EventTopic.USER_CREATED, user);
        return issueTokens(user);
    }

    /** Authenticates credentials and issues tokens. */
    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        UserAccountEntity user = userRepository
                .findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new IdentityException("AUTHENTICATION_FAILED", "Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (user.status() != AccountStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.passwordHash())) {
            auditService.record(user.id(), "LOGIN_FAILED", AuditOutcome.FAILURE, ipAddress, userAgent, user.email());
            throw new IdentityException("AUTHENTICATION_FAILED", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        user.markLogin();
        auditService.record(user.id(), "LOGIN_SUCCEEDED", AuditOutcome.SUCCESS, ipAddress, userAgent, user.email());
        publishUser(EventTopic.USER_LOGGED_IN, user);
        return issueTokens(user);
    }

    /** Issues new tokens from a valid refresh token and revokes the old token. */
    @Transactional
    public TokenResponse refresh(RefreshRequest request, String ipAddress, String userAgent) {
        RefreshTokenEntity token = refreshTokenRepository
                .findByTokenHash(tokenHashingService.hash(request.refreshToken()))
                .orElseThrow(() -> new IdentityException("REFRESH_TOKEN_INVALID", "Refresh token is invalid", HttpStatus.UNAUTHORIZED));
        if (!token.isUsable(Instant.now())) {
            throw new IdentityException("REFRESH_TOKEN_INVALID", "Refresh token is invalid", HttpStatus.UNAUTHORIZED);
        }
        token.revoke();
        auditService.record(token.user().id(), "TOKEN_REFRESHED", AuditOutcome.SUCCESS, ipAddress, userAgent, token.user().email());
        return issueTokens(token.user());
    }

    /** Revokes a refresh token during logout. */
    @Transactional
    public void logout(LogoutRequest request, String ipAddress, String userAgent) {
        refreshTokenRepository.findByTokenHash(tokenHashingService.hash(request.refreshToken())).ifPresent(token -> {
            token.revoke();
            auditService.record(token.user().id(), "LOGOUT", AuditOutcome.SUCCESS, ipAddress, userAgent, token.user().email());
        });
    }

    private TokenResponse issueTokens(UserAccountEntity user) {
        JwtTokenService.IssuedAccessToken accessToken = jwtTokenService.issue(user);
        String refreshToken = randomToken();
        Instant refreshExpiresAt = Instant.now().plusSeconds(refreshTokenDays * 86_400);
        refreshTokenRepository.save(new RefreshTokenEntity(user, tokenHashingService.hash(refreshToken), refreshExpiresAt));
        long expiresIn = Math.max(0, accessToken.expiresAt().getEpochSecond() - Instant.now().getEpochSecond());
        return new TokenResponse(accessToken.token(), refreshToken, expiresIn, accessToken.expiresAt());
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void publishUser(EventTopic topic, UserAccountEntity user) {
        outboxService.enqueue(
                topic,
                "USER",
                user.id(),
                user.organization().id(),
                user.id(),
                new EventPayloads.UserPayload(user.id(), user.organization().id(), user.username(), user.email(), user.status().name(), Instant.now()));
    }
}

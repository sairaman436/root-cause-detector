/*
 * Purpose: Defines authentication API DTOs.
 * Why it exists: Keeps controller input and output contracts explicit and validated.
 * Architecture fit: Implements approved login, logout, registration, and refresh token REST contracts.
 */
package com.airural.platform.core.identity.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Authentication request and response DTOs. */
public final class AuthDtos {
    private AuthDtos() {
    }

    /** Login request. */
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    /** User registration request. */
    public record RegisterRequest(
            @NotBlank @Size(max = 80) String username,
            @Email @NotBlank String email,
            @NotBlank @Size(max = 180) String fullName,
            @Size(max = 40) String phoneNumber,
            @NotBlank @Size(min = 12, max = 128) String password,
            @NotBlank String organizationCode) {
    }

    /** Refresh token request. */
    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    /** Logout request. */
    public record LogoutRequest(@NotBlank String refreshToken) {
    }

    /** Token response. */
    public record TokenResponse(String accessToken, String refreshToken, long expiresIn, Instant accessTokenExpiresAt) {
    }
}

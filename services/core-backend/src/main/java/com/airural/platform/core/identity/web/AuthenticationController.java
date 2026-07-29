/*
 * Purpose: Exposes authentication API endpoints.
 * Why it exists: Clients need registration, login, refresh, and logout operations.
 * Architecture fit: REST adapter for the approved authentication module.
 */
package com.airural.platform.core.identity.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.application.AuthenticationService;
import com.airural.platform.core.identity.web.dto.AuthDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for authentication workflows. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** Registers a new user and returns tokens. */
    @Operation(summary = "Register user", description = "Registers a user in an existing organization.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        TokenResponse response = authenticationService.register(request, ip(httpRequest), userAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response, RequestIds.from(httpRequest)));
    }

    /** Authenticates user credentials and returns tokens. */
    @Operation(summary = "Login", description = "Authenticates an email and password and returns JWT credentials.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        TokenResponse response = authenticationService.login(request, ip(httpRequest), userAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response, RequestIds.from(httpRequest)));
    }

    /** Refreshes access credentials from a refresh token. */
    @Operation(summary = "Refresh token", description = "Rotates a valid refresh token and issues a new JWT.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        TokenResponse response = authenticationService.refresh(request, ip(httpRequest), userAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response, RequestIds.from(httpRequest)));
    }

    /** Revokes a refresh token during logout. */
    @Operation(summary = "Logout", description = "Revokes a refresh token.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
        authenticationService.logout(request, ip(httpRequest), userAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(null, RequestIds.from(httpRequest)));
    }

    private String ip(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}

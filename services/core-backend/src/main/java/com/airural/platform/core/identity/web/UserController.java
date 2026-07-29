/*
 * Purpose: Exposes current-user identity APIs.
 * Why it exists: Authenticated clients need access to their profile and authorities.
 * Architecture fit: REST adapter for user management.
 */
package com.airural.platform.core.identity.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.application.IdentityManagementService;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.identity.web.dto.IdentityDtos.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for authenticated user APIs. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final IdentityManagementService identityManagementService;

    public UserController(IdentityManagementService identityManagementService) {
        this.identityManagementService = identityManagementService;
    }

    /** Returns the current authenticated user. */
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile, roles, and permissions.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                identityManagementService.currentUser(principal.userId()), RequestIds.from(request)));
    }
}

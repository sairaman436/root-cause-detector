/*
 * Purpose: Exposes administrative identity management APIs.
 * Why it exists: Administrators need governed user, organization, role, and permission management operations.
 * Architecture fit: REST adapter for RBAC-protected identity administration.
 */
package com.airural.platform.core.identity.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.identity.application.IdentityManagementService;
import com.airural.platform.core.identity.web.dto.IdentityDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for administrator identity management. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminIdentityController {
    private final IdentityManagementService identityManagementService;

    public AdminIdentityController(IdentityManagementService identityManagementService) {
        this.identityManagementService = identityManagementService;
    }

    /** Lists users. */
    @Operation(summary = "List users", description = "Lists registered platform users.")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> users(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.listUsers(), RequestIds.from(request)));
    }

    /** Deactivates a user. */
    @Operation(summary = "Deactivate user", description = "Deactivates a user account.")
    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID userId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.deactivateUser(userId), RequestIds.from(request)));
    }

    /** Lists organizations. */
    @Operation(summary = "List organizations", description = "Lists organizations.")
    @GetMapping("/organizations")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> organizations(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.listOrganizations(), RequestIds.from(request)));
    }

    /** Creates an organization. */
    @Operation(summary = "Create organization", description = "Creates an organization.")
    @PostMapping("/organizations")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest createRequest, HttpServletRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(identityManagementService.createOrganization(createRequest), RequestIds.from(request)));
    }

    /** Lists roles. */
    @Operation(summary = "List roles", description = "Lists RBAC roles.")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> roles(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.listRoles(), RequestIds.from(request)));
    }

    /** Creates a role. */
    @Operation(summary = "Create role", description = "Creates an RBAC role from existing permissions.")
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest createRequest, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.createRole(createRequest), RequestIds.from(request)));
    }

    /** Lists permissions. */
    @Operation(summary = "List permissions", description = "Lists RBAC permissions.")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> permissions(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(identityManagementService.listPermissions(), RequestIds.from(request)));
    }
}

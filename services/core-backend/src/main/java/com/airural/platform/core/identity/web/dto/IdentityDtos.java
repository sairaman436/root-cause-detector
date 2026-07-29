/*
 * Purpose: Defines identity management API DTOs.
 * Why it exists: Keeps user, organization, role, and permission contracts stable and documented.
 * Architecture fit: Implements the approved identity platform REST boundary.
 */
package com.airural.platform.core.identity.web.dto;

import com.airural.platform.core.identity.domain.AccountStatus;
import com.airural.platform.core.identity.domain.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Identity management request and response DTOs. */
public final class IdentityDtos {
    private IdentityDtos() {
    }

    /** Public user projection. */
    public record UserResponse(
            UUID id,
            UUID organizationId,
            String organizationCode,
            String username,
            String email,
            String fullName,
            String phoneNumber,
            AccountStatus status,
            List<String> roles,
            List<String> permissions,
            Instant lastLoginAt) {
    }

    /** Organization creation request. */
    public record CreateOrganizationRequest(@NotBlank @Size(max = 160) String name, @NotBlank @Size(max = 64) String code) {
    }

    /** Organization projection. */
    public record OrganizationResponse(UUID id, String name, String code, OrganizationStatus status) {
    }

    /** Role creation request. */
    public record CreateRoleRequest(
            @NotBlank @Size(max = 80) String name, @Size(max = 500) String description, List<String> permissions) {
    }

    /** Role projection. */
    public record RoleResponse(UUID id, String name, String description, List<String> permissions) {
    }

    /** Permission projection. */
    public record PermissionResponse(UUID id, String name, String resource, String action, String description) {
    }
}

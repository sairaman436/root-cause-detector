/*
 * Purpose: Maps identity entities to API DTOs.
 * Why it exists: Keeps persistence models from leaking directly through REST APIs.
 * Architecture fit: Supports Clean Architecture boundaries between domain and web adapters.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.identity.domain.*;
import com.airural.platform.core.identity.web.dto.IdentityDtos.*;
import java.util.List;
import org.springframework.stereotype.Component;

/** DTO mapper for identity entities. */
@Component
public class IdentityMapper {

    /** Converts a user account to a user response. */
    public UserResponse toUser(UserAccountEntity user) {
        List<String> roles = user.roles().stream().map(RoleEntity::name).sorted().toList();
        List<String> permissions = user.roles().stream()
                .flatMap(role -> role.permissions().stream())
                .map(PermissionEntity::name)
                .distinct()
                .sorted()
                .toList();
        return new UserResponse(
                user.id(),
                user.organization().id(),
                user.organization().code(),
                user.username(),
                user.email(),
                user.fullName(),
                user.phoneNumber(),
                user.status(),
                roles,
                permissions,
                user.lastLoginAt());
    }

    /** Converts an organization to a response. */
    public OrganizationResponse toOrganization(OrganizationEntity organization) {
        return new OrganizationResponse(organization.id(), organization.name(), organization.code(), organization.status());
    }

    /** Converts a role to a response. */
    public RoleResponse toRole(RoleEntity role) {
        return new RoleResponse(
                role.id(),
                role.name(),
                role.description(),
                role.permissions().stream().map(PermissionEntity::name).sorted().toList());
    }

    /** Converts a permission to a response. */
    public PermissionResponse toPermission(PermissionEntity permission) {
        return new PermissionResponse(
                permission.id(), permission.name(), permission.resource(), permission.action(), permission.description());
    }
}

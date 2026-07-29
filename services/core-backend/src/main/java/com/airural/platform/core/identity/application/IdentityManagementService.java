/*
 * Purpose: Implements user, organization, role, and permission management workflows.
 * Why it exists: Identity administration needs governed application services behind RBAC-protected controllers.
 * Architecture fit: Application service for the approved user, organization, role, and permission modules.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.events.application.OutboxService;
import com.airural.platform.core.identity.domain.*;
import com.airural.platform.core.identity.infrastructure.*;
import com.airural.platform.core.identity.web.dto.IdentityDtos.*;
import com.airural.platform.shared.events.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for identity management operations. */
@Service
public class IdentityManagementService {
    private final UserAccountRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final IdentityMapper mapper;
    private final OutboxService outboxService;

    public IdentityManagementService(
            UserAccountRepository userRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            IdentityMapper mapper,
            OutboxService outboxService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.mapper = mapper;
        this.outboxService = outboxService;
    }

    /** Returns the authenticated user's profile and authorities. */
    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID userId) {
        return mapper.toUser(userRepository
                .findWithRolesById(userId)
                .orElseThrow(() -> new IdentityException("USER_NOT_FOUND", "User was not found", HttpStatus.NOT_FOUND)));
    }

    /** Lists all users. */
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> userRepository.findWithRolesById(user.id()).orElse(user))
                .map(mapper::toUser)
                .toList();
    }

    /** Deactivates a user account. */
    @Transactional
    public UserResponse deactivateUser(UUID userId) {
        UserAccountEntity user = userRepository
                .findWithRolesById(userId)
                .orElseThrow(() -> new IdentityException("USER_NOT_FOUND", "User was not found", HttpStatus.NOT_FOUND));
        user.deactivate();
        publishUser(EventTopic.USER_UPDATED, user);
        return mapper.toUser(user);
    }

    /** Creates an organization. */
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCode(request.code())) {
            throw new IdentityException("ORGANIZATION_EXISTS", "Organization code already exists", HttpStatus.CONFLICT);
        }
        return mapper.toOrganization(organizationRepository.save(new OrganizationEntity(request.name(), request.code())));
    }

    /** Lists organizations. */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> listOrganizations() {
        return organizationRepository.findAll().stream().map(mapper::toOrganization).toList();
    }

    /** Creates a role with selected permissions. */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new IdentityException("ROLE_EXISTS", "Role already exists", HttpStatus.CONFLICT);
        }
        List<String> requestedPermissions = request.permissions() == null ? List.of() : request.permissions();
        List<PermissionEntity> permissions = permissionRepository.findByNameIn(requestedPermissions);
        if (permissions.size() != requestedPermissions.size()) {
            throw new IdentityException("PERMISSION_NOT_FOUND", "One or more permissions were not found", HttpStatus.BAD_REQUEST);
        }
        RoleEntity role = new RoleEntity(request.name(), request.description(), new HashSet<>(permissions));
        RoleEntity saved = roleRepository.save(role);
        outboxService.enqueue(
                EventTopic.AUDIT_CREATED,
                "ROLE",
                saved.id(),
                null,
                null,
                new EventPayloads.AuditPayload(saved.id(), null, "PERMISSION_CHANGE", AuditOutcome.SUCCESS.name(), Instant.now()));
        return mapper.toRole(saved);
    }

    /** Lists roles. */
    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(mapper::toRole).toList();
    }

    /** Lists permissions. */
    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream().map(mapper::toPermission).toList();
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

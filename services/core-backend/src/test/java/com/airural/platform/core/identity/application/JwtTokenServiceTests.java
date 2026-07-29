/*
 * Purpose: Verifies JWT issuance and validation behavior.
 * Why it exists: Access tokens carry the RBAC claims used by protected APIs.
 * Architecture fit: Unit coverage for the identity authentication layer.
 */
package com.airural.platform.core.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.identity.domain.OrganizationEntity;
import com.airural.platform.core.identity.domain.PermissionEntity;
import com.airural.platform.core.identity.domain.RoleEntity;
import com.airural.platform.core.identity.domain.UserAccountEntity;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit tests for JWT access-token handling. */
class JwtTokenServiceTests {
    private final JwtTokenService jwtTokenService = new JwtTokenService(
            "airural-platform-test",
            "test-secret-for-jwt-token-service-unit-tests-64-bytes-long",
            15);

    /** Issued tokens include subject, identity, role, and permission claims. */
    @Test
    void issueIncludesIdentityAndRbacClaims() {
        PermissionEntity permission = new PermissionEntity("USER_MANAGE", "USER", "MANAGE", "Manage users");
        RoleEntity role = new RoleEntity("ADMINISTRATOR", "Administrator", Set.of(permission));
        UserAccountEntity user = new UserAccountEntity(
                new OrganizationEntity("Platform", "PLATFORM"),
                "admin",
                "admin@example.gov",
                "Admin User",
                null,
                "hash",
                Set.of(role));

        String token = jwtTokenService.issue(user).token();
        Claims claims = jwtTokenService.parse(token);

        assertThat(claims.getSubject()).isEqualTo(user.id().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("admin@example.gov");
        assertThat(claims.get("roles", List.class)).containsExactly("ADMINISTRATOR");
        assertThat(claims.get("permissions", List.class)).containsExactly("USER_MANAGE");
    }
}

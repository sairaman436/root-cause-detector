/*
 * Purpose: Verifies production secret fail-fast behavior.
 * Why it exists: RC1 must prevent production startup with local-development credentials.
 * Architecture fit: Security regression coverage for cross-cutting runtime configuration.
 */
package com.airural.platform.core.common;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/** Unit tests for production secret validation. */
class ProductionSecretValidatorTests {

    @Test
    void allowsLocalEnvironmentWithLocalSecrets() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("airural.platform.environment", "local")
                .withProperty("spring.datasource.password", "airural_local_only")
                .withProperty("airural.security.jwt.secret", "local-development-secret-must-be-rotated-before-production-32-bytes-minimum");

        assertThatCode(() -> new ProductionSecretValidator(environment).run(null)).doesNotThrowAnyException();
    }

    @Test
    void rejectsProductionEnvironmentWithLocalSecrets() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("airural.platform.environment", "production")
                .withProperty("spring.datasource.password", "airural_local_only")
                .withProperty("airural.security.jwt.secret", "local-development-secret-must-be-rotated-before-production-32-bytes-minimum");

        assertThatThrownBy(() -> new ProductionSecretValidator(environment).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsafe production secret");
    }

    @Test
    void allowsProductionEnvironmentWithRotatedSecrets() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("airural.platform.environment", "production")
                .withProperty("spring.datasource.password", "rotated-database-password")
                .withProperty("airural.security.jwt.secret", "rotated-jwt-secret-with-more-than-thirty-two-bytes");

        assertThatCode(() -> new ProductionSecretValidator(environment).run(null)).doesNotThrowAnyException();
    }
}

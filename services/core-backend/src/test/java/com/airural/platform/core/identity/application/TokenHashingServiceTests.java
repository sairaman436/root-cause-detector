/*
 * Purpose: Verifies deterministic refresh-token hashing behavior.
 * Why it exists: Refresh tokens are stored only as hashes and must be stable for lookup.
 * Architecture fit: Unit coverage for the identity token lifecycle.
 */
package com.airural.platform.core.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for refresh-token hashing. */
class TokenHashingServiceTests {
    private final TokenHashingService tokenHashingService = new TokenHashingService();

    /** Hashes are deterministic and do not expose the raw token value. */
    @Test
    void hashIsDeterministicAndOpaque() {
        String first = tokenHashingService.hash("refresh-token-value");
        String second = tokenHashingService.hash("refresh-token-value");

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo("refresh-token-value");
        assertThat(first).hasSize(64);
    }
}

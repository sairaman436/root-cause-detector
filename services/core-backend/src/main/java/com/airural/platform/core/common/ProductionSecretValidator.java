/*
 * Purpose: Blocks production startup with known local-development secrets.
 * Why it exists: Release candidates must fail closed when operators accidentally reuse local defaults.
 * Architecture fit: Cross-cutting runtime guard enforcing the CEOS Security Constitution.
 */
package com.airural.platform.core.common;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Validates critical secrets when the production profile or environment is active. */
@Component
public class ProductionSecretValidator implements ApplicationRunner {
    private static final List<String> LOCAL_ONLY_VALUES = List.of(
            "airural_local_only",
            "local-development-secret-must-be-rotated-before-production-32-bytes-minimum",
            "test-development-secret-must-be-rotated-before-production-32-bytes-minimum");

    private final Environment environment;

    public ProductionSecretValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!productionRuntime()) {
            return;
        }
        Map<String, String> sensitiveValues = new LinkedHashMap<>();
        sensitiveValues.put("spring.datasource.password", environment.getProperty("spring.datasource.password", ""));
        sensitiveValues.put("airural.security.jwt.secret", environment.getProperty("airural.security.jwt.secret", ""));
        for (Map.Entry<String, String> entry : sensitiveValues.entrySet()) {
            if (LOCAL_ONLY_VALUES.contains(entry.getValue()) || entry.getValue().isBlank()) {
                throw new IllegalStateException("Unsafe production secret for " + entry.getKey());
            }
        }
    }

    private boolean productionRuntime() {
        String appEnv = environment.getProperty("airural.platform.environment", "");
        if ("production".equalsIgnoreCase(appEnv) || "prod".equalsIgnoreCase(appEnv)) {
            return true;
        }
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "production".equalsIgnoreCase(profile) || "prod".equalsIgnoreCase(profile));
    }
}

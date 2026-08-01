/*
 * Purpose: Persists benchmark suite registry records.
 * Why it exists: Evaluation runs need versioned benchmark suites for repeatability.
 * Architecture fit: Infrastructure adapter for AI-5 benchmark registry.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.BenchmarkSuiteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for benchmark suites. */
public interface BenchmarkSuiteRepository extends JpaRepository<BenchmarkSuiteEntity, UUID> {
    Optional<BenchmarkSuiteEntity> findBySuiteKey(String suiteKey);
}

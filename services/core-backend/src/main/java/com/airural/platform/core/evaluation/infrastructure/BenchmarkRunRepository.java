/*
 * Purpose: Persists benchmark run results.
 * Why it exists: Benchmark execution must be reproducible by suite and evaluation run.
 * Architecture fit: Infrastructure adapter for AI-5 benchmark execution.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.BenchmarkRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for benchmark runs. */
public interface BenchmarkRunRepository extends JpaRepository<BenchmarkRunEntity, UUID> {
}

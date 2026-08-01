/*
 * Purpose: Persists optimization benchmark records.
 * Why it exists: AI-6 APIs expose performance, memory, utilization, startup, and concurrency measurements.
 * Architecture fit: JPA repository for performance evidence.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.PerformanceBenchmarkEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for performance benchmarks. */
public interface PerformanceBenchmarkRepository extends JpaRepository<PerformanceBenchmarkEntity, UUID> {}

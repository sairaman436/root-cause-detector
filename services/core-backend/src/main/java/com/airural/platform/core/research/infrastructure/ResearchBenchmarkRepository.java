/*
 * Purpose: Persists research benchmark records.
 * Why it exists: Benchmark suites must be stable, queryable, and governed.
 * Architecture fit: JPA adapter for Research-1 benchmark framework.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchBenchmarkEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research benchmarks. */
public interface ResearchBenchmarkRepository extends JpaRepository<ResearchBenchmarkEntity, UUID> {}

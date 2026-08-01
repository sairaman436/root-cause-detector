/*
 * Purpose: Persists red-team attack results.
 * Why it exists: Prompt injection, role confusion, poisoning, tool misuse, and stress tests must be auditable.
 * Architecture fit: Infrastructure adapter for AI-5 red-team framework.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.RedTeamRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for red-team runs. */
public interface RedTeamRunRepository extends JpaRepository<RedTeamRunEntity, UUID> {
}

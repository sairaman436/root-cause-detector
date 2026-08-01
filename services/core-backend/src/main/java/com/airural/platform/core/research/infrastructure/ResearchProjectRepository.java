/*
 * Purpose: Persists research project records.
 * Why it exists: Research APIs need project creation, lookup, and listing.
 * Architecture fit: JPA adapter for the Research-1 project aggregate.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchProjectEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research projects. */
public interface ResearchProjectRepository extends JpaRepository<ResearchProjectEntity, UUID> {
    Optional<ResearchProjectEntity> findByProjectKey(String projectKey);
}

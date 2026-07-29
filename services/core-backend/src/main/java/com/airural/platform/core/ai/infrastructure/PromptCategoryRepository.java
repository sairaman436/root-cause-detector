/*
 * Purpose: Provides persistence access to prompt categories.
 * Why it exists: Prompt templates are grouped for governance and analytics.
 * Architecture fit: Repository adapter for prompt management.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.PromptCategoryEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prompt categories. */
public interface PromptCategoryRepository extends JpaRepository<PromptCategoryEntity, UUID> {
    Optional<PromptCategoryEntity> findByName(String name);
}

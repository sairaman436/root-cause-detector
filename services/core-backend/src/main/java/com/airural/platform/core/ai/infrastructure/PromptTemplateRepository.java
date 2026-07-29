/*
 * Purpose: Provides persistence access to prompt templates.
 * Why it exists: Prompt management APIs require list and unique-name lookup.
 * Architecture fit: Repository adapter for prompt governance.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.PromptTemplateEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prompt templates. */
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    Optional<PromptTemplateEntity> findByName(String name);
    boolean existsByName(String name);
}

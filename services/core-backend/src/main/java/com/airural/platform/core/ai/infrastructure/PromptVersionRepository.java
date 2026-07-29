/*
 * Purpose: Provides persistence access to prompt versions.
 * Why it exists: Prompt history, testing, and approval require immutable version lookup.
 * Architecture fit: Repository adapter for prompt version records.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prompt versions. */
public interface PromptVersionRepository extends JpaRepository<PromptVersionEntity, UUID> {
    List<PromptVersionEntity> findByTemplateOrderByVersionNumberDesc(PromptTemplateEntity template);
}

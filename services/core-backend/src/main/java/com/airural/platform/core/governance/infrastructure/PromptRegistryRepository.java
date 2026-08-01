/*
 * Purpose: Persists governed prompt registry records.
 * Why it exists: Prompt approval and rollback decisions need stable prompt version lookup.
 * Architecture fit: JPA adapter for AI-9 prompt governance.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.PromptRegistryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prompt registry records. */
public interface PromptRegistryRepository extends JpaRepository<PromptRegistryEntity, UUID> {}

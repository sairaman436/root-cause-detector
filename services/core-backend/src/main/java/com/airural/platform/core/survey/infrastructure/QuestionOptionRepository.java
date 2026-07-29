/*
 * Purpose: Provides persistence access to question options.
 * Why it exists: Choice-style questions require ordered option catalogs.
 * Architecture fit: Infrastructure adapter for question options.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.QuestionOptionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for question options. */
public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, UUID> {
    /** Lists options for a question in display order. */
    List<QuestionOptionEntity> findByQuestion_IdOrderByOrderIndexAsc(UUID questionId);
}

/*
 * Purpose: Provides persistence access to survey templates.
 * Why it exists: Template library operations need durable template storage and filtering.
 * Architecture fit: Infrastructure adapter for template management.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyTemplateEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Repository for survey templates. */
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplateEntity, UUID>, JpaSpecificationExecutor<SurveyTemplateEntity> {
    /** Returns whether a template name exists in a category. */
    boolean existsByNameIgnoreCaseAndCategory(String name, com.airural.platform.core.survey.domain.SurveyTemplateCategory category);
}

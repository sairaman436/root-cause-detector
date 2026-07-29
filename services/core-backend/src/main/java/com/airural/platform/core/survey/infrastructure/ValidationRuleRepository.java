/*
 * Purpose: Provides persistence access to survey validation rules.
 * Why it exists: Validation rules are governed configuration for questionnaire quality.
 * Architecture fit: Infrastructure adapter for the validation engine.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.ValidationRuleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for validation rules. */
public interface ValidationRuleRepository extends JpaRepository<ValidationRuleEntity, UUID> {
    /** Lists validation rules for a survey. */
    List<ValidationRuleEntity> findBySurvey_IdOrderByOrderIndexAsc(UUID surveyId);

    /** Lists validation rules for a question. */
    List<ValidationRuleEntity> findByQuestion_IdOrderByOrderIndexAsc(UUID questionId);
}

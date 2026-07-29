/*
 * Purpose: Provides persistence access to survey questions.
 * Why it exists: Questionnaire management requires dynamic question persistence and lookup by code.
 * Architecture fit: Infrastructure adapter for question definitions.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyQuestionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for survey questions. */
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestionEntity, UUID> {
    /** Lists questions for a survey in display order. */
    List<SurveyQuestionEntity> findBySurvey_IdOrderByOrderIndexAsc(UUID surveyId);

    /** Lists questions for a section in display order. */
    List<SurveyQuestionEntity> findBySection_IdOrderByOrderIndexAsc(UUID sectionId);

    /** Finds a question by survey and code. */
    Optional<SurveyQuestionEntity> findBySurvey_IdAndCode(UUID surveyId, String code);
}

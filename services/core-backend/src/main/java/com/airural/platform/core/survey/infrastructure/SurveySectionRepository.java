/*
 * Purpose: Provides persistence access to questionnaire sections.
 * Why it exists: Dynamic questionnaire management requires ordered section persistence.
 * Architecture fit: Infrastructure adapter for the questionnaire engine.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveySectionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for survey sections. */
public interface SurveySectionRepository extends JpaRepository<SurveySectionEntity, UUID> {
    /** Lists sections for a survey in display order. */
    List<SurveySectionEntity> findBySurvey_IdOrderByOrderIndexAsc(UUID surveyId);

    /** Finds a section by survey and code. */
    Optional<SurveySectionEntity> findBySurvey_IdAndCode(UUID surveyId, String code);
}

/*
 * Purpose: Provides persistence access to survey versions.
 * Why it exists: Survey version history must be queryable for audit and reproducibility.
 * Architecture fit: Infrastructure adapter for survey versioning.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyVersionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for survey versions. */
public interface SurveyVersionRepository extends JpaRepository<SurveyVersionEntity, UUID> {
    /** Lists versions for a survey. */
    List<SurveyVersionEntity> findBySurvey_IdOrderByVersionNumberDesc(UUID surveyId);
}

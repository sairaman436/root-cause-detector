/*
 * Purpose: Provides persistence access to survey workflow history.
 * Why it exists: Workflow transitions must be auditable.
 * Architecture fit: Infrastructure adapter for survey governance history.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyStatusHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for survey status history. */
public interface SurveyStatusHistoryRepository extends JpaRepository<SurveyStatusHistoryEntity, UUID> {
    /** Lists status changes for a survey. */
    List<SurveyStatusHistoryEntity> findBySurvey_IdOrderByCreatedAtAsc(UUID surveyId);
}

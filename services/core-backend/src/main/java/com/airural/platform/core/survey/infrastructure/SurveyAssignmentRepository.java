/*
 * Purpose: Provides persistence access to survey assignments.
 * Why it exists: Assignment workflows need durable target records and lookup support.
 * Architecture fit: Infrastructure adapter for survey distribution.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyAssignmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for survey assignments. */
public interface SurveyAssignmentRepository extends JpaRepository<SurveyAssignmentEntity, UUID> {
    /** Lists assignments for a survey. */
    List<SurveyAssignmentEntity> findBySurvey_IdAndIsActiveTrue(UUID surveyId);
}

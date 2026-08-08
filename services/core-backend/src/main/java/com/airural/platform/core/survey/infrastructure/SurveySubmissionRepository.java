/*
 * Purpose: Provides persistence access to survey submissions.
 * Why it exists: Core workflow APIs require durable submitted survey responses.
 * Architecture fit: Infrastructure adapter for the survey submission aggregate.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveySubmissionEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Repository for survey submissions. */
public interface SurveySubmissionRepository extends JpaRepository<SurveySubmissionEntity, UUID> {
    /** Lists submissions for a survey with answers eagerly loaded. */
    @EntityGraph(attributePaths = {"answers"})
    List<SurveySubmissionEntity> findBySurvey_IdOrderBySubmittedAtDesc(UUID surveyId);

    /** Finds a submission with answer data loaded. */
    @EntityGraph(attributePaths = {"answers"})
    @Query("select submission from SurveySubmissionEntity submission where submission.id = :id")
    Optional<SurveySubmissionEntity> findWithAnswersById(UUID id);
}

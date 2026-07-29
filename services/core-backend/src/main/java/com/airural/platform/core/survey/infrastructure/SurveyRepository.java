/*
 * Purpose: Provides persistence access to survey aggregate roots.
 * Why it exists: Survey application services require durable CRUD and search operations.
 * Architecture fit: Infrastructure adapter for the survey module.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

/** Repository for surveys. */
public interface SurveyRepository extends JpaRepository<SurveyEntity, UUID>, JpaSpecificationExecutor<SurveyEntity> {
    /** Finds an active survey with tag data loaded. */
    @EntityGraph(attributePaths = {"tags"})
    Optional<SurveyEntity> findByIdAndIsActiveTrue(UUID id);
}

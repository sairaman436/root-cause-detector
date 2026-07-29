/*
 * Purpose: Builds JPA search specifications for survey search.
 * Why it exists: Survey search must support name, status, tags, organization, creator, and updated date filters.
 * Architecture fit: Infrastructure search adapter for the survey application layer.
 */
package com.airural.platform.core.survey.infrastructure;

import com.airural.platform.core.survey.domain.SurveyEntity;
import com.airural.platform.core.survey.domain.SurveyStatus;
import com.airural.platform.core.survey.domain.SurveyTagEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Factory for survey search specifications. */
public final class SurveySpecifications {
    private SurveySpecifications() {
    }

    /** Restricts results to active surveys. */
    public static Specification<SurveyEntity> activeOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    /** Filters by case-insensitive name fragment. */
    public static Specification<SurveyEntity> nameContains(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    /** Filters by status. */
    public static Specification<SurveyEntity> statusEquals(SurveyStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /** Filters by organization. */
    public static Specification<SurveyEntity> organizationEquals(UUID organizationId) {
        return (root, query, cb) -> organizationId == null ? cb.conjunction() : cb.equal(root.get("organizationId"), organizationId);
    }

    /** Filters by creator. */
    public static Specification<SurveyEntity> createdByEquals(UUID createdByUserId) {
        return (root, query, cb) -> createdByUserId == null ? cb.conjunction() : cb.equal(root.get("createdByUserId"), createdByUserId);
    }

    /** Filters by updated date lower bound. */
    public static Specification<SurveyEntity> updatedAfter(Instant updatedFrom) {
        return (root, query, cb) -> updatedFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom);
    }

    /** Filters by tag. */
    public static Specification<SurveyEntity> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null || tag.isBlank()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<SurveyEntity, SurveyTagEntity> tags = root.join("tags", JoinType.INNER);
            return cb.equal(tags.get("name"), tag.toLowerCase());
        };
    }
}

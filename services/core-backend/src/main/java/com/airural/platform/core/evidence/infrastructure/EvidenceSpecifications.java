/*
 * Purpose: Builds dynamic evidence search filters.
 * Why it exists: Evidence search needs composable filters by survey, uploader, organization, tags, date, and type.
 * Architecture fit: Persistence-specific query helper kept outside application services.
 */
package com.airural.platform.core.evidence.infrastructure;

import com.airural.platform.core.evidence.domain.*;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Specification factory for evidence search. */
public final class EvidenceSpecifications {
    private EvidenceSpecifications() {
    }

    public static Specification<EvidenceEntity> organizationEquals(UUID organizationId) {
        return (root, query, cb) -> organizationId == null ? cb.conjunction() : cb.equal(root.get("organizationId"), organizationId);
    }

    public static Specification<EvidenceEntity> surveyEquals(UUID surveyId) {
        return (root, query, cb) -> surveyId == null ? cb.conjunction() : cb.equal(root.get("surveyId"), surveyId);
    }

    public static Specification<EvidenceEntity> uploaderEquals(UUID uploaderId) {
        return (root, query, cb) -> uploaderId == null ? cb.conjunction() : cb.equal(root.get("uploadedByUserId"), uploaderId);
    }

    public static Specification<EvidenceEntity> createdAfter(Instant uploadedFrom) {
        return (root, query, cb) -> uploadedFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), uploadedFrom);
    }

    public static Specification<EvidenceEntity> typeEquals(EvidenceType evidenceType) {
        return (root, query, cb) -> evidenceType == null ? cb.conjunction() : cb.equal(root.get("evidenceType"), evidenceType);
    }

    public static Specification<EvidenceEntity> activeOnly(boolean includeDeleted) {
        return (root, query, cb) -> includeDeleted ? cb.conjunction() : cb.isTrue(root.get("isActive"));
    }

    public static Specification<EvidenceEntity> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null || tag.isBlank()) {
                return cb.conjunction();
            }
            Join<EvidenceEntity, EvidenceTagEntity> tags = root.join("tags");
            query.distinct(true);
            return cb.equal(tags.get("name"), tag.trim().toLowerCase());
        };
    }
}

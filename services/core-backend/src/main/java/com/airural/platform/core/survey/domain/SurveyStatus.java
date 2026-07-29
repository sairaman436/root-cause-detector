/*
 * Purpose: Defines the approved survey lifecycle states.
 * Why it exists: Survey workflow transitions must be validated consistently.
 * Architecture fit: Domain enum for the Enterprise Survey Management module.
 */
package com.airural.platform.core.survey.domain;

import java.util.Map;
import java.util.Set;

/** Survey lifecycle state. */
public enum SurveyStatus {
    DRAFT,
    REVIEW,
    APPROVED,
    PUBLISHED,
    ACTIVE,
    COMPLETED,
    ARCHIVED,
    DELETED;

    private static final Map<SurveyStatus, Set<SurveyStatus>> ALLOWED_TRANSITIONS = Map.of(
            DRAFT, Set.of(REVIEW, ARCHIVED, DELETED),
            REVIEW, Set.of(DRAFT, APPROVED, ARCHIVED),
            APPROVED, Set.of(PUBLISHED, ARCHIVED),
            PUBLISHED, Set.of(ACTIVE, ARCHIVED),
            ACTIVE, Set.of(COMPLETED, ARCHIVED),
            COMPLETED, Set.of(ARCHIVED),
            ARCHIVED, Set.of(DELETED),
            DELETED, Set.of());

    /** Returns whether this state can move to the requested next state. */
    public boolean canTransitionTo(SurveyStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }
}

/*
 * Purpose: Verifies survey lifecycle transition rules.
 * Why it exists: Milestone 3 requires strict workflow validation from draft through archive.
 * Architecture fit: Unit coverage for the survey domain layer.
 */
package com.airural.platform.core.survey;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.survey.domain.SurveyStatus;
import org.junit.jupiter.api.Test;

/** Unit tests for survey status workflow rules. */
class SurveyWorkflowTests {
    /** Approved workflow transitions are allowed. */
    @Test
    void approvedWorkflowPathIsAllowed() {
        assertThat(SurveyStatus.DRAFT.canTransitionTo(SurveyStatus.REVIEW)).isTrue();
        assertThat(SurveyStatus.REVIEW.canTransitionTo(SurveyStatus.APPROVED)).isTrue();
        assertThat(SurveyStatus.APPROVED.canTransitionTo(SurveyStatus.PUBLISHED)).isTrue();
        assertThat(SurveyStatus.PUBLISHED.canTransitionTo(SurveyStatus.ACTIVE)).isTrue();
        assertThat(SurveyStatus.ACTIVE.canTransitionTo(SurveyStatus.COMPLETED)).isTrue();
        assertThat(SurveyStatus.COMPLETED.canTransitionTo(SurveyStatus.ARCHIVED)).isTrue();
    }

    /** Invalid workflow jumps are rejected. */
    @Test
    void invalidWorkflowJumpIsRejected() {
        assertThat(SurveyStatus.DRAFT.canTransitionTo(SurveyStatus.ACTIVE)).isFalse();
        assertThat(SurveyStatus.PUBLISHED.canTransitionTo(SurveyStatus.DRAFT)).isFalse();
        assertThat(SurveyStatus.DELETED.canTransitionTo(SurveyStatus.DRAFT)).isFalse();
    }
}

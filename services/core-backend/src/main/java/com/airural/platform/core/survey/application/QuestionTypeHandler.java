/*
 * Purpose: Defines an extension point for survey question types.
 * Why it exists: New question types must be addable without modifying existing validation code.
 * Architecture fit: Open/closed interface for the dynamic questionnaire engine.
 */
package com.airural.platform.core.survey.application;

import com.airural.platform.core.survey.web.dto.SurveyDtos.CreateQuestionRequest;

/** Pluggable handler for a question type. */
public interface QuestionTypeHandler {
    /** Canonical question type key. */
    String type();

    /** Validates type-specific question configuration. */
    default void validate(CreateQuestionRequest request) {
    }
}

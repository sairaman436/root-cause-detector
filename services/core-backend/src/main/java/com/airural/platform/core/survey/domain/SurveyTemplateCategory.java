/*
 * Purpose: Defines canonical categories for the survey template library.
 * Why it exists: Template search and governance require categorized reusable questionnaire assets.
 * Architecture fit: Domain vocabulary for survey templates.
 */
package com.airural.platform.core.survey.domain;

/** Template category used by the template library. */
public enum SurveyTemplateCategory {
    HEALTH,
    WATER,
    AGRICULTURE,
    INFRASTRUCTURE,
    EDUCATION,
    LIVELIHOOD,
    GOVERNANCE,
    CUSTOM
}

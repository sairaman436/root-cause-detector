/*
 * Purpose: Defines governance states for reusable survey templates.
 * Why it exists: Template library entries require lifecycle control independent of live surveys.
 * Architecture fit: Domain enum for template publication governance.
 */
package com.airural.platform.core.survey.domain;

/** Reusable survey template status. */
public enum SurveyTemplateStatus {
    DRAFT,
    APPROVED,
    PUBLISHED,
    RETIRED
}

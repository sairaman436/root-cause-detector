/*
 * Purpose: Defines built-in validation rule categories for questionnaire questions.
 * Why it exists: The validation engine requires stable rule names while allowing custom expressions.
 * Architecture fit: Domain vocabulary for survey validation.
 */
package com.airural.platform.core.survey.domain;

/** Supported validation rule families. */
public enum ValidationRuleType {
    REQUIRED,
    REGEX,
    NUMERIC_RANGE,
    DATE_RANGE,
    CROSS_FIELD,
    CUSTOM
}

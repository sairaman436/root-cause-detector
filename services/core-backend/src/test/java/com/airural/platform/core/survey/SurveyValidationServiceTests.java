/*
 * Purpose: Verifies design-time survey validation rule checks.
 * Why it exists: Invalid questionnaire validation rules must fail before publication.
 * Architecture fit: Unit coverage for the survey validation engine.
 */
package com.airural.platform.core.survey;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.airural.platform.core.survey.application.SurveyException;
import com.airural.platform.core.survey.application.SurveyValidationService;
import com.airural.platform.core.survey.domain.ValidationRuleType;
import com.airural.platform.core.survey.web.dto.SurveyDtos.CreateValidationRuleRequest;
import org.junit.jupiter.api.Test;

/** Unit tests for survey validation service behavior. */
class SurveyValidationServiceTests {
    private final SurveyValidationService validationService = new SurveyValidationService();

    /** Valid regex rules are accepted. */
    @Test
    void validRegexRuleIsAccepted() {
        CreateValidationRuleRequest rule = new CreateValidationRuleRequest(null, ValidationRuleType.REGEX, "^[A-Z]+$", "Uppercase only", null, 0);

        assertThatCode(() -> validationService.validateRule(rule)).doesNotThrowAnyException();
    }

    /** Invalid regex rules are rejected. */
    @Test
    void invalidRegexRuleIsRejected() {
        CreateValidationRuleRequest rule = new CreateValidationRuleRequest(null, ValidationRuleType.REGEX, "[", "Bad regex", null, 0);

        assertThatThrownBy(() -> validationService.validateRule(rule)).isInstanceOf(SurveyException.class);
    }
}

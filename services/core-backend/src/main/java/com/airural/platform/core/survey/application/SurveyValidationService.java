/*
 * Purpose: Validates survey definitions and validation-rule configuration.
 * Why it exists: Survey data quality depends on rules being syntactically meaningful before publication.
 * Architecture fit: Application validation engine for survey metadata and questionnaire definitions.
 */
package com.airural.platform.core.survey.application;

import com.airural.platform.core.survey.domain.ValidationRuleType;
import com.airural.platform.core.survey.web.dto.SurveyDtos.CreateValidationRuleRequest;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Validation engine for survey design-time rules. */
@Service
public class SurveyValidationService {
    /** Validates a single validation rule definition. */
    public void validateRule(CreateValidationRuleRequest rule) {
        if (rule.ruleType() == ValidationRuleType.REGEX) {
            if (rule.expression() == null || rule.expression().isBlank()) {
                throw new SurveyException("REGEX_EXPRESSION_REQUIRED", "Regex validation requires an expression", HttpStatus.BAD_REQUEST);
            }
            try {
                Pattern.compile(rule.expression());
            } catch (PatternSyntaxException ex) {
                throw new SurveyException("REGEX_EXPRESSION_INVALID", "Regex validation expression is invalid", HttpStatus.BAD_REQUEST);
            }
        }
        if ((rule.ruleType() == ValidationRuleType.NUMERIC_RANGE || rule.ruleType() == ValidationRuleType.DATE_RANGE)
                && (rule.paramsJson() == null || rule.paramsJson().isBlank())) {
            throw new SurveyException("VALIDATION_PARAMS_REQUIRED", "Range validation requires paramsJson", HttpStatus.BAD_REQUEST);
        }
        if ((rule.ruleType() == ValidationRuleType.CROSS_FIELD || rule.ruleType() == ValidationRuleType.CUSTOM)
                && (rule.expression() == null || rule.expression().isBlank())) {
            throw new SurveyException("VALIDATION_EXPRESSION_REQUIRED", "Cross-field and custom validation require an expression", HttpStatus.BAD_REQUEST);
        }
    }
}

/*
 * Purpose: Provides built-in survey question type validation.
 * Why it exists: Milestone 3 requires a complete baseline type catalog while keeping future types pluggable.
 * Architecture fit: Default QuestionTypeHandler implementation for built-in questionnaire types.
 */
package com.airural.platform.core.survey.application;

import com.airural.platform.core.survey.web.dto.SurveyDtos.CreateQuestionRequest;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Built-in handler for a single supported question type. */
@Component
public class BasicQuestionTypeHandler implements QuestionTypeHandler {
    private static final Set<String> OPTION_TYPES = Set.of("SINGLE_SELECT", "MULTI_SELECT", "MATRIX", "LOOKUP", "RATING");
    private final String type;

    public BasicQuestionTypeHandler() {
        this.type = "__registry_seed__";
    }

    public BasicQuestionTypeHandler(String type) {
        this.type = type;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public void validate(CreateQuestionRequest request) {
        String normalized = request.questionType().trim().toUpperCase();
        if (OPTION_TYPES.contains(normalized) && (request.options() == null || request.options().isEmpty())) {
            throw new SurveyException("QUESTION_OPTIONS_REQUIRED", "Question type requires at least one option", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        if ("CALCULATED_FIELD".equals(normalized) && (request.calculationExpression() == null || request.calculationExpression().isBlank())) {
            throw new SurveyException("CALCULATION_EXPRESSION_REQUIRED", "Calculated fields require a calculation expression", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}

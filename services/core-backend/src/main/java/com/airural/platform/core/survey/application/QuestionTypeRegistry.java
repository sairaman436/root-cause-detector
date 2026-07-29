/*
 * Purpose: Registers supported question type handlers.
 * Why it exists: The questionnaire engine must support adding types through new handlers without modifying existing services.
 * Architecture fit: Application extension registry for dynamic questionnaire definitions.
 */
package com.airural.platform.core.survey.application;

import com.airural.platform.core.survey.web.dto.SurveyDtos.CreateQuestionRequest;
import java.util.*;
import org.springframework.stereotype.Component;

/** Registry for question type handlers. */
@Component
public class QuestionTypeRegistry {
    private static final Set<String> BUILT_IN_TYPES = Set.of(
            "TEXT", "PARAGRAPH", "INTEGER", "DECIMAL", "BOOLEAN", "SINGLE_SELECT", "MULTI_SELECT",
            "DATE", "TIME", "DATETIME", "GPS", "IMAGE", "VIDEO", "FILE_UPLOAD", "RATING", "MATRIX",
            "LOOKUP", "CALCULATED_FIELD");
    private final Map<String, QuestionTypeHandler> handlers = new HashMap<>();

    public QuestionTypeRegistry(List<QuestionTypeHandler> customHandlers) {
        BUILT_IN_TYPES.forEach(type -> handlers.put(type, new BasicQuestionTypeHandler(type)));
        customHandlers.stream()
                .filter(handler -> !"__registry_seed__".equals(handler.type()))
                .forEach(handler -> handlers.put(handler.type().trim().toUpperCase(), handler));
    }

    /** Validates a question type and returns its canonical key. */
    public String validateAndNormalize(CreateQuestionRequest request) {
        String type = request.questionType().trim().toUpperCase();
        QuestionTypeHandler handler = handlers.get(type);
        if (handler == null) {
            throw new SurveyException("QUESTION_TYPE_UNSUPPORTED", "Unsupported question type: " + request.questionType(), org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        handler.validate(request);
        return type;
    }

    /** Lists registered question types. */
    public Set<String> supportedTypes() {
        return Set.copyOf(handlers.keySet());
    }
}

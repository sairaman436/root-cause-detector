/*
 * Purpose: Exposes registered survey question types.
 * Why it exists: Clients need to render supported dynamic questionnaire controls without hard-coding server capabilities.
 * Architecture fit: REST adapter for the question type registry.
 */
package com.airural.platform.core.survey.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.survey.application.SurveyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for supported question types. */
@RestController
@RequestMapping("/api/v1/question-types")
public class QuestionTypeController {
    private final SurveyManagementService surveyManagementService;

    public QuestionTypeController(SurveyManagementService surveyManagementService) {
        this.surveyManagementService = surveyManagementService;
    }

    /** Lists supported question type keys. */
    @Operation(summary = "List question types", description = "Lists registered dynamic questionnaire question types.")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> list(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(surveyManagementService.supportedQuestionTypes(), RequestIds.from(request)));
    }
}

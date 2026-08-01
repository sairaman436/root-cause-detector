/*
 * Purpose: Exposes Rural Intelligence Research Laboratory APIs.
 * Why it exists: Research teams need controlled project, experiment, publication, and finding endpoints.
 * Architecture fit: REST adapter for Research-1 under `/api/v1/research` and `/research`.
 */
package com.airural.platform.core.research.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.research.application.ResearchLaboratoryService;
import com.airural.platform.core.research.web.dto.ResearchDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for the Rural Intelligence Research Laboratory. */
@RestController
@RequestMapping({"/api/v1/research", "/research"})
public class ResearchController {
    private final ResearchLaboratoryService service;

    public ResearchController(ResearchLaboratoryService service) {
        this.service = service;
    }

    /** Creates a research project. */
    @Operation(summary = "Create research project", description = "Creates a governed Rural Intelligence Research Laboratory project.")
    @PostMapping("/project")
    public ResponseEntity<ApiResponse<ResearchProjectResponse>> createProject(@Valid @RequestBody ResearchProjectRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createProject(body), RequestIds.from(request)));
    }

    /** Creates a research experiment. */
    @Operation(summary = "Create research experiment", description = "Creates an approved research experiment proposal and hypothesis record.")
    @PostMapping("/experiment")
    public ResponseEntity<ApiResponse<ResearchExperimentResponse>> createExperiment(@Valid @RequestBody ResearchExperimentRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createExperiment(body), RequestIds.from(request)));
    }

    /** Lists research projects. */
    @Operation(summary = "List research projects", description = "Lists laboratory projects across divisions and long-term programs.")
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<Page<ResearchProjectResponse>>> projects(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.projects(pageable), RequestIds.from(request)));
    }

    /** Lists publications. */
    @Operation(summary = "List research publications", description = "Lists papers, technical reports, experiment reports, benchmarks, whitepapers, RFCs, and reviews.")
    @GetMapping("/publications")
    public ResponseEntity<ApiResponse<List<PublicationResponse>>> publications(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.publications(), RequestIds.from(request)));
    }

    /** Lists research findings. */
    @Operation(summary = "List research findings", description = "Lists scientific findings with confidence and replication status.")
    @GetMapping("/findings")
    public ResponseEntity<ApiResponse<List<ResearchFindingResponse>>> findings(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.findings(), RequestIds.from(request)));
    }
}

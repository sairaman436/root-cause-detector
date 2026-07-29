/*
 * Purpose: Exposes GeoJSON boundary APIs.
 * Why it exists: Administrative boundaries are required for map rendering, spatial filtering, and future analytics.
 * Architecture fit: REST adapter for boundary management in the Geospatial module.
 */
package com.airural.platform.core.geospatial.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.geospatial.application.GeospatialManagementService;
import com.airural.platform.core.geospatial.domain.AdministrativeLevel;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for GeoJSON boundaries. */
@RestController
@RequestMapping("/api/v1/geospatial/boundaries")
public class GeoBoundaryController {
    private final GeospatialManagementService service;

    public GeoBoundaryController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Create boundary", description = "Creates a GeoJSON boundary for an administrative entity.")
    @PostMapping
    public ResponseEntity<ApiResponse<GeoBoundaryResponse>> create(@Valid @RequestBody CreateGeoBoundaryRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createBoundary(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Get boundary", description = "Gets the active boundary for an administrative entity.")
    @GetMapping
    public ResponseEntity<ApiResponse<GeoBoundaryResponse>> get(@RequestParam AdministrativeLevel entityType, @RequestParam UUID entityId, HttpServletRequest request) {
        return service.boundary(entityType, entityId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(result, RequestIds.from(request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

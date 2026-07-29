/*
 * Purpose: Exposes household mapping APIs.
 * Why it exists: Field operations need GPS-addressed households linked to administrative hierarchy, surveys, evidence, and future IoT metadata.
 * Architecture fit: REST adapter for household geospatial mapping.
 */
package com.airural.platform.core.geospatial.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.geospatial.application.GeospatialManagementService;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for household mapping. */
@RestController
@RequestMapping("/api/v1/geospatial/households")
public class HouseholdController {
    private final GeospatialManagementService service;

    public HouseholdController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Create household", description = "Creates a GPS-addressed household with optional survey and evidence linkage.")
    @PostMapping
    public ResponseEntity<ApiResponse<HouseholdResponse>> create(@Valid @RequestBody CreateHouseholdRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createHousehold(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Get household", description = "Gets an active household by ID.")
    @GetMapping("/{householdId}")
    public ResponseEntity<ApiResponse<HouseholdResponse>> get(@PathVariable UUID householdId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.getHousehold(householdId), RequestIds.from(request)));
    }

    @Operation(summary = "Search households", description = "Searches households by hamlet and optional bounding box.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<HouseholdResponse>>> search(@RequestParam(required = false) UUID hamletId, @RequestParam(required = false) BigDecimal minLatitude, @RequestParam(required = false) BigDecimal minLongitude, @RequestParam(required = false) BigDecimal maxLatitude, @RequestParam(required = false) BigDecimal maxLongitude, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.searchHouseholds(hamletId, minLatitude, minLongitude, maxLatitude, maxLongitude, pageable), RequestIds.from(request)));
    }
}

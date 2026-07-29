/*
 * Purpose: Exposes administrative hierarchy management and lookup APIs.
 * Why it exists: Enterprise deployments need governed creation of country-to-hamlet geography before surveys and evidence can be geocoded.
 * Architecture fit: REST adapter for Geospatial hierarchy use cases.
 */
package com.airural.platform.core.geospatial.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.geospatial.application.GeospatialManagementService;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for administrative hierarchy management. */
@RestController
@RequestMapping("/api/v1/geospatial/admin")
public class AdministrativeHierarchyController {
    private final GeospatialManagementService service;

    public AdministrativeHierarchyController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Create country", description = "Creates a country-level administrative unit.")
    @PostMapping("/countries")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createCountry(@Valid @RequestBody CreateCountryRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createCountry(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create state", description = "Creates a state below a country.")
    @PostMapping("/states")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createState(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createState(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create district", description = "Creates a district below a state.")
    @PostMapping("/districts")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createDistrict(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createDistrict(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create mandal", description = "Creates a mandal or sub-district below a district.")
    @PostMapping("/mandals")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createMandal(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createMandal(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create block", description = "Creates a block below a mandal.")
    @PostMapping("/blocks")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createBlock(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createBlock(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create gram panchayat", description = "Creates a gram panchayat below a block.")
    @PostMapping("/gram-panchayats")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createGramPanchayat(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createGramPanchayat(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create village", description = "Creates a village with centroid, population, household count, and optional boundary metadata.")
    @PostMapping("/villages")
    public ResponseEntity<ApiResponse<VillageResponse>> createVillage(@Valid @RequestBody CreateVillageRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createVillage(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create ward", description = "Creates a ward below a village.")
    @PostMapping("/wards")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createWard(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createWard(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create hamlet", description = "Creates a hamlet below a ward.")
    @PostMapping("/hamlets")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createHamlet(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createHamlet(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Search villages", description = "Searches villages by name and gram panchayat.")
    @GetMapping("/villages")
    public ResponseEntity<ApiResponse<Page<VillageResponse>>> villages(@RequestParam(required = false) String name, @RequestParam(required = false) UUID gramPanchayatId, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.searchVillages(name, gramPanchayatId, pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Get household hierarchy", description = "Returns the resolved administrative hierarchy path for a household.")
    @GetMapping("/households/{householdId}/hierarchy")
    public ResponseEntity<ApiResponse<HierarchyPathResponse>> householdHierarchy(@PathVariable UUID householdId, HttpServletRequest request) {
        return service.householdHierarchy(householdId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(result, RequestIds.from(request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

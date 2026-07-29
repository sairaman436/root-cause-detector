/*
 * Purpose: Exposes the Milestone 6 geography API surface requested by enterprise clients.
 * Why it exists: The approved geospatial module already owns the domain model, while clients require /api/v1/geography compatibility routes.
 * Architecture fit: REST adapter that delegates to the Geospatial application service without duplicating business logic.
 */
package com.airural.platform.core.geospatial.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.geospatial.application.GeospatialManagementService;
import com.airural.platform.core.geospatial.domain.InfrastructureAssetType;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for Milestone 6 geography compatibility APIs. */
@RestController
@RequestMapping("/api/v1/geography")
public class GeographyController {
    private final GeospatialManagementService service;

    public GeographyController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Create country", description = "Creates a country geography record.")
    @PostMapping("/countries")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createCountry(@Valid @RequestBody CreateCountryRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createCountry(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create state", description = "Creates a state geography record under a country.")
    @PostMapping("/states")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createState(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createState(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create district", description = "Creates a district geography record under a state.")
    @PostMapping("/districts")
    public ResponseEntity<ApiResponse<AdminUnitResponse>> createDistrict(@Valid @RequestBody CreateAdminUnitRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createDistrict(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Create village", description = "Creates a village geography record under a gram panchayat.")
    @PostMapping("/villages")
    public ResponseEntity<ApiResponse<VillageResponse>> createVillage(@Valid @RequestBody CreateVillageRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createVillage(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Get hierarchy", description = "Returns the resolved administrative hierarchy for a household.")
    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<HierarchyPathResponse>> hierarchy(@RequestParam UUID householdId, HttpServletRequest request) {
        return service.householdHierarchy(householdId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(result, RequestIds.from(request))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search geography", description = "Searches village geography records by name and gram panchayat.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<VillageResponse>>> search(@RequestParam(required = false) String query, @RequestParam(required = false) UUID gramPanchayatId, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.searchVillages(query, gramPanchayatId, pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Nearby infrastructure", description = "Finds nearby infrastructure assets from a GPS coordinate.")
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<InfrastructureAssetResponse>>> nearby(@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude, @RequestParam(required = false) InfrastructureAssetType assetType, @RequestParam(defaultValue = "25") double radiusKm, @RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.nearestAssets(latitude, longitude, assetType, radiusKm, limit), RequestIds.from(request)));
    }

    @Operation(summary = "Get boundary", description = "Gets a GeoJSON boundary by boundary ID.")
    @GetMapping("/boundary/{id}")
    public ResponseEntity<ApiResponse<GeoBoundaryResponse>> boundary(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.boundary(id), RequestIds.from(request)));
    }

    @Operation(summary = "Create infrastructure", description = "Creates a mapped infrastructure asset.")
    @PostMapping("/infrastructure")
    public ResponseEntity<ApiResponse<InfrastructureAssetResponse>> createInfrastructure(@Valid @RequestBody CreateInfrastructureAssetRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createInfrastructureAsset(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Search infrastructure", description = "Searches mapped infrastructure assets.")
    @GetMapping("/infrastructure")
    public ResponseEntity<ApiResponse<Page<InfrastructureAssetResponse>>> infrastructure(@RequestParam(required = false) UUID villageId, @RequestParam(required = false) InfrastructureAssetType assetType, @RequestParam(required = false) String name, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.searchAssets(villageId, assetType, name, pageable), RequestIds.from(request)));
    }
}

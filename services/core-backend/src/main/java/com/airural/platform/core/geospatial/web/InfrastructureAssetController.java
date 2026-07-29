/*
 * Purpose: Exposes infrastructure asset management APIs.
 * Why it exists: Facilities and public assets must be mapped for future decision intelligence and proximity analysis.
 * Architecture fit: REST adapter for infrastructure assets in the Geospatial module.
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
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for infrastructure assets. */
@RestController
@RequestMapping("/api/v1/geospatial/infrastructure-assets")
public class InfrastructureAssetController {
    private final GeospatialManagementService service;

    public InfrastructureAssetController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Create infrastructure asset", description = "Creates a mapped school, hospital, PHC, road, water asset, market, bank, government office, PDS shop, or community facility.")
    @PostMapping
    public ResponseEntity<ApiResponse<InfrastructureAssetResponse>> create(@Valid @RequestBody CreateInfrastructureAssetRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createInfrastructureAsset(body, user.userId()), RequestIds.from(request)));
    }

    @Operation(summary = "Search infrastructure assets", description = "Searches infrastructure assets by village, type, and name.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InfrastructureAssetResponse>>> search(@RequestParam(required = false) UUID villageId, @RequestParam(required = false) InfrastructureAssetType assetType, @RequestParam(required = false) String name, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.searchAssets(villageId, assetType, name, pageable), RequestIds.from(request)));
    }
}

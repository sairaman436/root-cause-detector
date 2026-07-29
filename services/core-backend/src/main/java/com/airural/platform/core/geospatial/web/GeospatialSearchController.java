/*
 * Purpose: Exposes spatial search and calculation APIs.
 * Why it exists: Clients need distance, radius, bounding-box, nearest-facility, and clustering operations.
 * Architecture fit: REST adapter for portable spatial services.
 */
package com.airural.platform.core.geospatial.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.geospatial.application.GeospatialManagementService;
import com.airural.platform.core.geospatial.domain.InfrastructureAssetType;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for spatial search operations. */
@RestController
@RequestMapping("/api/v1/geospatial/search")
public class GeospatialSearchController {
    private final GeospatialManagementService service;

    public GeospatialSearchController(GeospatialManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Radius village search", description = "Finds villages inside a radius from a GPS coordinate.")
    @GetMapping("/villages/radius")
    public ResponseEntity<ApiResponse<Page<VillageResponse>>> villagesInRadius(@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude, @RequestParam(defaultValue = "10") double radiusKm, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.villagesInRadius(latitude, longitude, radiusKm, pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Bounding-box village search", description = "Finds villages inside a bounding box.")
    @GetMapping("/villages/bbox")
    public ResponseEntity<ApiResponse<Page<VillageResponse>>> villagesInBox(@RequestParam BigDecimal minLatitude, @RequestParam BigDecimal minLongitude, @RequestParam BigDecimal maxLatitude, @RequestParam BigDecimal maxLongitude, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.villagesInBoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude, pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Nearest infrastructure assets", description = "Finds nearest infrastructure assets by type and radius.")
    @GetMapping("/assets/nearest")
    public ResponseEntity<ApiResponse<List<InfrastructureAssetResponse>>> nearestAssets(@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude, @RequestParam(required = false) InfrastructureAssetType assetType, @RequestParam(defaultValue = "25") double radiusKm, @RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.nearestAssets(latitude, longitude, assetType, radiusKm, limit), RequestIds.from(request)));
    }

    @Operation(summary = "Calculate distance", description = "Calculates haversine distance between two GPS points.")
    @GetMapping("/distance")
    public ResponseEntity<ApiResponse<DistanceResponse>> distance(@RequestParam BigDecimal fromLatitude, @RequestParam BigDecimal fromLongitude, @RequestParam BigDecimal toLatitude, @RequestParam BigDecimal toLongitude, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.distance(fromLatitude, fromLongitude, toLatitude, toLongitude), RequestIds.from(request)));
    }

    @Operation(summary = "Cluster villages", description = "Clusters villages into grid cells for map rendering and exploratory analysis.")
    @GetMapping("/villages/clusters")
    public ResponseEntity<ApiResponse<List<VillageClusterResponse>>> clusters(@RequestParam(defaultValue = "0.1") double gridSizeDegrees, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.villageClusters(gridSizeDegrees), RequestIds.from(request)));
    }
}

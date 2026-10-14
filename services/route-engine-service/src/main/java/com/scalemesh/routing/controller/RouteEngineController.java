package com.scalemesh.routing.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.common.dto.RouteCalculationRequest;
import com.scalemesh.common.dto.RouteEstimateDto;
import com.scalemesh.routing.service.RouteEngineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteEngineController {

    private final RouteEngineService routeEngineService;

    public RouteEngineController(RouteEngineService routeEngineService) {
        this.routeEngineService = routeEngineService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<RouteEstimateDto>> calculateRoute(
            @Valid @RequestBody RouteCalculationRequest request) {
        RouteEstimateDto estimate = routeEngineService.calculateRoute(request);
        return ResponseEntity.ok(ApiResponse.success(estimate));
    }

    @GetMapping("/estimate")
    public ResponseEntity<ApiResponse<RouteEstimateDto>> estimateRoute(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng) {
        if (originLat < -90.0 || originLat > 90.0 || destLat < -90.0 || destLat > 90.0) {
            throw new IllegalArgumentException("Latitude coordinates must be between -90.0 and 90.0");
        }
        if (originLng < -180.0 || originLng > 180.0 || destLng < -180.0 || destLng > 180.0) {
            throw new IllegalArgumentException("Longitude coordinates must be between -180.0 and 180.0");
        }
        RouteCalculationRequest request = new RouteCalculationRequest(originLat, originLng, destLat, destLng);
        RouteEstimateDto estimate = routeEngineService.calculateRoute(request);
        return ResponseEntity.ok(ApiResponse.success(estimate));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Route Engine Service is healthy and operational");
    }
}

package com.scalemesh.routing.service;

import com.scalemesh.common.dto.RouteCalculationRequest;
import com.scalemesh.common.dto.RouteEstimateDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteEngineServiceTest {

    private RouteEngineService routeEngineService;

    @BeforeEach
    void setUp() {
        routeEngineService = new RouteEngineService(new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("Should accurately calculate route with distance, duration, and waypoints")
    void testCalculateRoute() {
        // Route from Bangalore MG Road (12.9750, 77.6080) to Electronic City (12.8399, 77.6770)
        RouteCalculationRequest request = new RouteCalculationRequest(12.9750, 77.6080, 12.8399, 77.6770);

        RouteEstimateDto result = routeEngineService.calculateRoute(request);

        assertNotNull(result);
        assertTrue(result.getDistanceKm() > 15.0 && result.getDistanceKm() < 30.0,
                "Distance should be between 15 and 30 km, got: " + result.getDistanceKm());
        assertTrue(result.getEstimatedMinutes() > 15.0);
        assertTrue(result.getTrafficMultiplier() >= 1.0);
        assertNotNull(result.getCongestionLevel());
        assertNotNull(result.getWaypoints());
        assertEquals(6, result.getWaypoints().size()); // Start + 4 intermediate + End
    }

    @Test
    @DisplayName("Zero distance for identical origin and destination")
    void testIdenticalCoordinates() {
        RouteCalculationRequest request = new RouteCalculationRequest(12.9750, 77.6080, 12.9750, 77.6080);
        RouteEstimateDto result = routeEngineService.calculateRoute(request);

        assertEquals(0.0, result.getDistanceKm());
        assertEquals(1.0, result.getEstimatedMinutes()); // Minimum 1 min
    }
}

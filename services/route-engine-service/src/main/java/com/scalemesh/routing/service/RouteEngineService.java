package com.scalemesh.routing.service;

import com.scalemesh.common.dto.RouteCalculationRequest;
import com.scalemesh.common.dto.RouteEstimateDto;
import com.scalemesh.common.util.GeoUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RouteEngineService {

    private final Counter routesCalculatedCounter;
    private final Timer calculationTimer;

    public RouteEngineService(MeterRegistry meterRegistry) {
        this.routesCalculatedCounter = Counter.builder("scalemesh.routes.calculated.total")
                .description("Total routes computed by Route Engine")
                .register(meterRegistry);
        this.calculationTimer = Timer.builder("scalemesh.routes.calculation.time")
                .description("Execution latency of route calculations")
                .register(meterRegistry);
    }

    public RouteEstimateDto calculateRoute(RouteCalculationRequest request) {
        long startTime = System.nanoTime();

        double originLat = request.getOriginLat();
        double originLng = request.getOriginLng();
        double destLat = request.getDestLat();
        double destLng = request.getDestLng();

        // Direct Haversine distance
        double directDistanceKm = GeoUtils.haversineDistanceKm(originLat, originLng, destLat, destLng);

        // Urban road networks are typically ~1.25x to 1.35x longer than straight-line distance
        double roadDistanceKm = Math.round(directDistanceKm * 1.28 * 100.0) / 100.0;

        // Deterministic simulated traffic multiplier based on geographic seed
        int coordSeed = Math.abs((int) ((originLat + originLng + destLat + destLng) * 1000.0));
        double trafficMultiplier = 1.0 + ((coordSeed % 40) / 100.0); // 1.00 to 1.39

        String congestionLevel;
        if (trafficMultiplier < 1.10) {
            congestionLevel = "LOW";
        } else if (trafficMultiplier < 1.25) {
            congestionLevel = "MODERATE";
        } else if (trafficMultiplier < 1.35) {
            congestionLevel = "HEAVY";
        } else {
            congestionLevel = "SEVERE";
        }

        double estimatedMinutes = GeoUtils.estimateDurationMinutes(roadDistanceKm, 38.0, trafficMultiplier);

        // Synthesize realistic geometric waypoints along the route
        List<double[]> waypoints = generateWaypoints(originLat, originLng, destLat, destLng, 5);

        routesCalculatedCounter.increment();
        calculationTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

        return new RouteEstimateDto(
                originLat, originLng, destLat, destLng,
                roadDistanceKm, estimatedMinutes,
                Math.round(trafficMultiplier * 100.0) / 100.0,
                congestionLevel,
                waypoints
        );
    }

    private List<double[]> generateWaypoints(double startLat, double startLng, double endLat, double endLng, int steps) {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{startLat, startLng});

        for (int i = 1; i < steps; i++) {
            double fraction = (double) i / steps;
            // Introduce slight realistic road jitter
            double jitterLat = Math.sin(fraction * Math.PI) * 0.002;
            double jitterLng = Math.cos(fraction * Math.PI) * 0.002;

            double intermediateLat = Math.round((startLat + (endLat - startLat) * fraction + jitterLat) * 10000.0) / 10000.0;
            double intermediateLng = Math.round((startLng + (endLng - startLng) * fraction + jitterLng) * 10000.0) / 10000.0;

            points.add(new double[]{intermediateLat, intermediateLng});
        }

        points.add(new double[]{endLat, endLng});
        return points;
    }
}

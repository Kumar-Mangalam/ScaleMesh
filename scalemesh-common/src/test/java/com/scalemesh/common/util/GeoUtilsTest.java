package com.scalemesh.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    @DisplayName("Should accurately calculate distance between London and Paris")
    void testHaversineDistanceLondonToParis() {
        // London: 51.5074, -0.1278
        // Paris: 48.8566, 2.3522
        // Approximate distance: ~343-344 km
        double distance = GeoUtils.haversineDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertTrue(distance > 340.0 && distance < 350.0, "Expected distance between 340 and 350 km, got: " + distance);
    }

    @Test
    @DisplayName("Zero distance for identical coordinates")
    void testHaversineDistanceZero() {
        double distance = GeoUtils.haversineDistanceKm(12.9716, 77.5946, 12.9716, 77.5946);
        assertEquals(0.0, distance, 0.01);
    }

    @Test
    @DisplayName("Should estimate duration with traffic factor")
    void testDurationEstimation() {
        // 35 km at 35 km/h with 1.0 traffic factor = 60 minutes
        double duration = GeoUtils.estimateDurationMinutes(35.0, 35.0, 1.0);
        assertEquals(60.0, duration, 0.5);

        // with 1.5 traffic factor = 90 minutes
        double heavyTrafficDuration = GeoUtils.estimateDurationMinutes(35.0, 35.0, 1.5);
        assertEquals(90.0, heavyTrafficDuration, 0.5);
    }
}

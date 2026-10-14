package com.scalemesh.common.util;

public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    /**
     * Calculates great-circle distance between two points on the Earth's surface using Haversine formula.
     *
     * @param lat1 Latitude of point 1 in decimal degrees
     * @param lon1 Longitude of point 1 in decimal degrees
     * @param lat2 Latitude of point 2 in decimal degrees
     * @param lon2 Longitude of point 2 in decimal degrees
     * @return Distance in kilometers
     */
    public static double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }

    /**
     * Estimates transit duration in minutes assuming average urban transit speed with traffic factor.
     */
    public static double estimateDurationMinutes(double distanceKm, double avgSpeedKmh, double trafficFactor) {
        if (avgSpeedKmh <= 0.0) {
            avgSpeedKmh = 35.0; // Default urban speed
        }
        if (trafficFactor <= 0.0) {
            trafficFactor = 1.0;
        }
        double hours = (distanceKm / avgSpeedKmh) * trafficFactor;
        return Math.max(1.0, Math.round(hours * 60.0 * 10.0) / 10.0);
    }
}

package com.scalemesh.common.dto;

import java.util.List;

public class RouteEstimateDto {

    private double originLat;
    private double originLng;
    private double destLat;
    private double destLng;
    private double distanceKm;
    private double estimatedMinutes;
    private double trafficMultiplier;
    private String congestionLevel;
    private List<double[]> waypoints;

    public RouteEstimateDto() {
    }

    public RouteEstimateDto(double originLat, double originLng, double destLat, double destLng,
                            double distanceKm, double estimatedMinutes, double trafficMultiplier,
                            String congestionLevel, List<double[]> waypoints) {
        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;
        this.distanceKm = distanceKm;
        this.estimatedMinutes = estimatedMinutes;
        this.trafficMultiplier = trafficMultiplier;
        this.congestionLevel = congestionLevel;
        this.waypoints = waypoints;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
    }

    public double getDestLat() {
        return destLat;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public double getDestLng() {
        return destLng;
    }

    public void setDestLng(double destLng) {
        this.destLng = destLng;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(double estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public double getEstimatedDurationMin() {
        return estimatedMinutes;
    }

    public void setEstimatedDurationMin(double estimatedDurationMin) {
        this.estimatedMinutes = estimatedDurationMin;
    }

    public double getTrafficMultiplier() {
        return trafficMultiplier;
    }

    public void setTrafficMultiplier(double trafficMultiplier) {
        this.trafficMultiplier = trafficMultiplier;
    }

    public String getCongestionLevel() {
        return congestionLevel;
    }

    public void setCongestionLevel(String congestionLevel) {
        this.congestionLevel = congestionLevel;
    }

    public List<double[]> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<double[]> waypoints) {
        this.waypoints = waypoints;
    }
}

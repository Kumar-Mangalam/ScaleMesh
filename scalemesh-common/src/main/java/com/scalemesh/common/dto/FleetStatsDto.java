package com.scalemesh.common.dto;

import java.time.Instant;
import java.util.Map;

public class FleetStatsDto {

    private long totalEvents;
    private long activeVehicles;
    private double averageSpeedKmh;
    private double averageBatteryPercent;
    private Map<String, Long> statusBreakdown;
    private Instant lastUpdated;

    public FleetStatsDto() {
        this.lastUpdated = Instant.now();
    }

    public FleetStatsDto(long totalEvents, long activeVehicles, double averageSpeedKmh,
                         double averageBatteryPercent, Map<String, Long> statusBreakdown) {
        this.totalEvents = totalEvents;
        this.activeVehicles = activeVehicles;
        this.averageSpeedKmh = averageSpeedKmh;
        this.averageBatteryPercent = averageBatteryPercent;
        this.statusBreakdown = statusBreakdown;
        this.lastUpdated = Instant.now();
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getActiveVehicles() {
        return activeVehicles;
    }

    public void setActiveVehicles(long activeVehicles) {
        this.activeVehicles = activeVehicles;
    }

    public double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    public void setAverageSpeedKmh(double averageSpeedKmh) {
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public double getAverageBatteryPercent() {
        return averageBatteryPercent;
    }

    public void setAverageBatteryPercent(double averageBatteryPercent) {
        this.averageBatteryPercent = averageBatteryPercent;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

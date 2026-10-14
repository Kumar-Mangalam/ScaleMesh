package com.scalemesh.common.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class RouteCalculationRequest {

    @DecimalMin(value = "-90.0", message = "Origin latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Origin latitude must be <= 90.0")
    private double originLat;

    @DecimalMin(value = "-180.0", message = "Origin longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Origin longitude must be <= 180.0")
    private double originLng;

    @DecimalMin(value = "-90.0", message = "Destination latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Destination latitude must be <= 90.0")
    private double destLat;

    @DecimalMin(value = "-180.0", message = "Destination longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Destination longitude must be <= 180.0")
    private double destLng;

    private boolean trafficOptimization = true;

    public RouteCalculationRequest() {
    }

    public RouteCalculationRequest(double originLat, double originLng, double destLat, double destLng) {
        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;
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

    public boolean isTrafficOptimization() {
        return trafficOptimization;
    }

    public void setTrafficOptimization(boolean trafficOptimization) {
        this.trafficOptimization = trafficOptimization;
    }
}

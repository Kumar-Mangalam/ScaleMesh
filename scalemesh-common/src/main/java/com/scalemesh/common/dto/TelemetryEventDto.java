package com.scalemesh.common.dto;

import com.scalemesh.common.enums.VehicleStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class TelemetryEventDto {

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private double longitude;

    @DecimalMin(value = "0.0", message = "Speed must be non-negative")
    private double speedKmh;

    @DecimalMin(value = "0.0", message = "Battery percent must be >= 0")
    @DecimalMax(value = "100.0", message = "Battery percent must be <= 100")
    private double batteryPercent;

    @DecimalMin(value = "0.0", message = "Odometer must be non-negative")
    private double odometerKm;

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus status;

    private Double tirePressurePsi;
    private Double engineTemperatureC;

    public TelemetryEventDto() {
        this.timestamp = Instant.now();
        this.status = VehicleStatus.IDLE;
    }

    public TelemetryEventDto(String vehicleId, Instant timestamp, double latitude, double longitude,
                             double speedKmh, double batteryPercent, double odometerKm, VehicleStatus status) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.batteryPercent = batteryPercent;
        this.odometerKm = odometerKm;
        this.status = status != null ? status : VehicleStatus.IDLE;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public void setSpeed(double speed) {
        this.speedKmh = speed;
    }

    public double getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(double batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryPercent = batteryLevel;
    }

    public double getOdometerKm() {
        return odometerKm;
    }

    public void setOdometerKm(double odometerKm) {
        this.odometerKm = odometerKm;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Double getTirePressurePsi() {
        return tirePressurePsi;
    }

    public void setTirePressurePsi(Double tirePressurePsi) {
        this.tirePressurePsi = tirePressurePsi;
    }

    public Double getEngineTemperatureC() {
        return engineTemperatureC;
    }

    public void setEngineTemperatureC(Double engineTemperatureC) {
        this.engineTemperatureC = engineTemperatureC;
    }
}

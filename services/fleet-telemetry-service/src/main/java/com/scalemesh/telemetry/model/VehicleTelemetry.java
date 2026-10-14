package com.scalemesh.telemetry.model;

import com.scalemesh.common.enums.VehicleStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "vehicle_telemetry", indexes = {
        @Index(name = "idx_telemetry_vehicle_time", columnList = "vehicleId, timestamp DESC")
})
public class VehicleTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String vehicleId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private double speedKmh;
    private double batteryPercent;
    private double odometerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleStatus status;

    private Double tirePressurePsi;
    private Double engineTemperatureC;

    public VehicleTelemetry() {
        this.timestamp = Instant.now();
        this.status = VehicleStatus.IDLE;
    }

    public VehicleTelemetry(String vehicleId, Instant timestamp, double latitude, double longitude,
                            double speedKmh, double batteryPercent, double odometerKm, VehicleStatus status,
                            Double tirePressurePsi, Double engineTemperatureC) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.batteryPercent = batteryPercent;
        this.odometerKm = odometerKm;
        this.status = status != null ? status : VehicleStatus.IDLE;
        this.tirePressurePsi = tirePressurePsi;
        this.engineTemperatureC = engineTemperatureC;
    }

    public Long getId() {
        return id;
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

    public double getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(double batteryPercent) {
        this.batteryPercent = batteryPercent;
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

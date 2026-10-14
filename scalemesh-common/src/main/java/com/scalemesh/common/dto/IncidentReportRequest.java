package com.scalemesh.common.dto;

import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentReportRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;

    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    @NotNull(message = "Incident severity is required")
    private IncidentSeverity severity;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private double longitude;

    public IncidentReportRequest() {
    }

    public IncidentReportRequest(String vehicleId, IncidentType incidentType, IncidentSeverity severity,
                                 String description, double latitude, double longitude) {
        this.vehicleId = vehicleId;
        this.incidentType = incidentType;
        this.severity = severity;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public IncidentType getType() {
        return incidentType;
    }

    public void setType(IncidentType type) {
        this.incidentType = type;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}

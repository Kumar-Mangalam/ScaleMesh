package com.scalemesh.common.dto;

import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.common.enums.IncidentType;

import java.time.Instant;

public class IncidentResponseDto {

    private String incidentId;
    private String vehicleId;
    private IncidentType incidentType;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String description;
    private double latitude;
    private double longitude;
    private Instant reportedAt;
    private Instant resolvedAt;
    private String resolutionNotes;

    public IncidentResponseDto() {
    }

    public IncidentResponseDto(String incidentId, String vehicleId, IncidentType incidentType,
                               IncidentSeverity severity, IncidentStatus status, String description,
                               double latitude, double longitude, Instant reportedAt) {
        this.incidentId = incidentId;
        this.vehicleId = vehicleId;
        this.incidentType = incidentType;
        this.severity = severity;
        this.status = status;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reportedAt = reportedAt;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
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

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
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

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}

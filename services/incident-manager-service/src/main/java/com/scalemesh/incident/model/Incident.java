package com.scalemesh.incident.model;

import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.common.enums.IncidentType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incident_status", columnList = "status"),
        @Index(name = "idx_incident_severity", columnList = "severity"),
        @Index(name = "idx_incident_vehicle", columnList = "vehicleId")
})
public class Incident {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentStatus status;

    @Column(nullable = false, length = 1000)
    private String description;

    private double latitude;
    private double longitude;

    @Column(nullable = false)
    private Instant reportedAt;

    private Instant resolvedAt;

    @Column(length = 1000)
    private String resolutionNotes;

    public Incident() {
        this.reportedAt = Instant.now();
        this.status = IncidentStatus.OPEN;
    }

    public Incident(String id, String vehicleId, IncidentType incidentType,
                    IncidentSeverity severity, String description,
                    double latitude, double longitude) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.incidentType = incidentType;
        this.severity = severity;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = IncidentStatus.OPEN;
        this.reportedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

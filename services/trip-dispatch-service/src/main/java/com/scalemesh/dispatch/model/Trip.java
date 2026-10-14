package com.scalemesh.dispatch.model;

import com.scalemesh.common.enums.TripPriority;
import com.scalemesh.common.enums.TripStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trips_status", columnList = "status"),
        @Index(name = "idx_trips_passenger", columnList = "passengerId"),
        @Index(name = "idx_trips_vehicle", columnList = "assignedVehicleId")
})
public class Trip {

    @Id
    private String id;

    @Column(nullable = false)
    private String passengerId;

    private String assignedVehicleId;

    @Column(nullable = false)
    private double pickupLat;

    @Column(nullable = false)
    private double pickupLng;

    @Column(nullable = false)
    private double dropoffLat;

    @Column(nullable = false)
    private double dropoffLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TripPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TripStatus status;

    private int passengerCount;
    private double distanceKm;
    private double estimatedDurationMin;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant dispatchedAt;
    private Instant completedAt;

    public Trip() {
        this.createdAt = Instant.now();
        this.status = TripStatus.REQUESTED;
        this.priority = TripPriority.NORMAL;
    }

    public Trip(String id, String passengerId, double pickupLat, double pickupLng,
                double dropoffLat, double dropoffLng, TripPriority priority,
                int passengerCount, double distanceKm, double estimatedDurationMin) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.priority = priority != null ? priority : TripPriority.NORMAL;
        this.passengerCount = passengerCount > 0 ? passengerCount : 1;
        this.distanceKm = distanceKm;
        this.estimatedDurationMin = estimatedDurationMin;
        this.status = TripStatus.REQUESTED;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getAssignedVehicleId() {
        return assignedVehicleId;
    }

    public void setAssignedVehicleId(String assignedVehicleId) {
        this.assignedVehicleId = assignedVehicleId;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public double getDropoffLng() {
        return dropoffLng;
    }

    public void setDropoffLng(double dropoffLng) {
        this.dropoffLng = dropoffLng;
    }

    public TripPriority getPriority() {
        return priority;
    }

    public void setPriority(TripPriority priority) {
        this.priority = priority;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(double estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}

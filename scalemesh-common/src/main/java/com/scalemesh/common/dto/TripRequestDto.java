package com.scalemesh.common.dto;

import com.scalemesh.common.enums.TripPriority;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TripRequestDto {

    @NotBlank(message = "Passenger ID or Cargo ID is required")
    private String passengerId;

    @DecimalMin(value = "-90.0", message = "Pickup latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Pickup latitude must be <= 90.0")
    private double pickupLat;

    @DecimalMin(value = "-180.0", message = "Pickup longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Pickup longitude must be <= 180.0")
    private double pickupLng;

    @DecimalMin(value = "-90.0", message = "Dropoff latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Dropoff latitude must be <= 90.0")
    private double dropoffLat;

    @DecimalMin(value = "-180.0", message = "Dropoff longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Dropoff longitude must be <= 180.0")
    private double dropoffLng;

    @NotNull(message = "Priority is required")
    private TripPriority priority = TripPriority.NORMAL;

    private int passengerCount = 1;

    public TripRequestDto() {
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
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

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }
}

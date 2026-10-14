package com.scalemesh.dispatch.service;

import com.scalemesh.common.dto.TripRequestDto;
import com.scalemesh.common.dto.TripResponseDto;
import com.scalemesh.common.enums.TripStatus;
import com.scalemesh.common.util.GeoUtils;
import com.scalemesh.dispatch.model.Trip;
import com.scalemesh.dispatch.repository.TripRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class TripDispatchService {

    private final TripRepository tripRepository;
    private final Counter tripsCreatedCounter;
    private final Counter tripsDispatchedCounter;
    private final Counter tripsCompletedCounter;

    public TripDispatchService(TripRepository tripRepository, MeterRegistry meterRegistry) {
        this.tripRepository = tripRepository;
        this.tripsCreatedCounter = Counter.builder("scalemesh.trips.created.total")
                .description("Total trips created")
                .register(meterRegistry);
        this.tripsDispatchedCounter = Counter.builder("scalemesh.trips.dispatched.total")
                .description("Total trips dispatched to vehicles")
                .register(meterRegistry);
        this.tripsCompletedCounter = Counter.builder("scalemesh.trips.completed.total")
                .description("Total trips successfully completed")
                .register(meterRegistry);
    }

    @Transactional
    public TripResponseDto createTrip(TripRequestDto request) {
        double distanceKm = GeoUtils.haversineDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng()
        );
        double durationMin = GeoUtils.estimateDurationMinutes(distanceKm, 35.0, 1.15);

        String tripId = "TRIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Trip trip = new Trip(
                tripId,
                request.getPassengerId(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDropoffLat(),
                request.getDropoffLng(),
                request.getPriority(),
                request.getPassengerCount(),
                distanceKm,
                durationMin
        );

        Trip saved = tripRepository.save(trip);
        tripsCreatedCounter.increment();
        return toDto(saved);
    }

    @Transactional
    public TripResponseDto dispatchTrip(String tripId, String vehicleId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + tripId));

        if (trip.getStatus() != TripStatus.REQUESTED) {
            throw new IllegalStateException("Only trips in REQUESTED status can be dispatched. Current status: " + trip.getStatus());
        }

        String assignedVehicle = (vehicleId != null && !vehicleId.isBlank())
                ? vehicleId
                : "AUTO-FLEET-" + Math.abs(tripId.hashCode() % 100);

        trip.setAssignedVehicleId(assignedVehicle);
        trip.setStatus(TripStatus.DISPATCHED);
        trip.setDispatchedAt(Instant.now());

        Trip updated = tripRepository.save(trip);
        tripsDispatchedCounter.increment();
        return toDto(updated);
    }

    @Transactional
    public TripResponseDto updateStatus(String tripId, TripStatus newStatus) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + tripId));

        validateTransition(trip.getStatus(), newStatus);

        trip.setStatus(newStatus);
        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            trip.setCompletedAt(Instant.now());
            if (newStatus == TripStatus.COMPLETED) {
                tripsCompletedCounter.increment();
            }
        }

        Trip updated = tripRepository.save(trip);
        return toDto(updated);
    }

    @Transactional(readOnly = true)
    public TripResponseDto getTrip(String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + tripId));
        return toDto(trip);
    }

    @Transactional(readOnly = true)
    public Page<TripResponseDto> listTrips(TripStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Trip> tripPage = (status != null)
                ? tripRepository.findByStatus(status, pageable)
                : tripRepository.findAll(pageable);
        return tripPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", tripRepository.count());
        for (TripStatus s : TripStatus.values()) {
            stats.put(s.name().toLowerCase(), tripRepository.countByStatus(s));
        }
        return stats;
    }

    private void validateTransition(TripStatus current, TripStatus target) {
        if (current == target) {
            return;
        }
        if (current == TripStatus.COMPLETED || current == TripStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition terminal trip state: " + current);
        }
        if (target == TripStatus.CANCELLED) {
            return; // Can cancel anytime before completion
        }
        if (current == TripStatus.REQUESTED && target != TripStatus.DISPATCHED) {
            throw new IllegalStateException("REQUESTED trips must be DISPATCHED before entering " + target);
        }
        if (current == TripStatus.DISPATCHED && target != TripStatus.IN_TRANSIT) {
            throw new IllegalStateException("DISPATCHED trips must transition to IN_TRANSIT before " + target);
        }
        if (current == TripStatus.IN_TRANSIT && target != TripStatus.COMPLETED) {
            throw new IllegalStateException("IN_TRANSIT trips can only transition to COMPLETED or CANCELLED");
        }
    }

    private TripResponseDto toDto(Trip trip) {
        TripResponseDto dto = new TripResponseDto();
        dto.setTripId(trip.getId());
        dto.setPassengerId(trip.getPassengerId());
        dto.setVehicleId(trip.getAssignedVehicleId());
        dto.setPickupLat(trip.getPickupLat());
        dto.setPickupLng(trip.getPickupLng());
        dto.setDropoffLat(trip.getDropoffLat());
        dto.setDropoffLng(trip.getDropoffLng());
        dto.setPriority(trip.getPriority());
        dto.setStatus(trip.getStatus());
        dto.setDistanceKm(trip.getDistanceKm());
        dto.setEstimatedDurationMin(trip.getEstimatedDurationMin());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setDispatchedAt(trip.getDispatchedAt());
        dto.setCompletedAt(trip.getCompletedAt());
        return dto;
    }
}

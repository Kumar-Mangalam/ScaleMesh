package com.scalemesh.dispatch.service;

import com.scalemesh.common.dto.TripRequestDto;
import com.scalemesh.common.dto.TripResponseDto;
import com.scalemesh.common.enums.TripPriority;
import com.scalemesh.common.enums.TripStatus;
import com.scalemesh.dispatch.model.Trip;
import com.scalemesh.dispatch.repository.TripRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripDispatchServiceTest {

    @Mock
    private TripRepository tripRepository;

    private TripDispatchService tripDispatchService;

    @BeforeEach
    void setUp() {
        tripDispatchService = new TripDispatchService(tripRepository, new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("Should create trip and compute distance and duration")
    void testCreateTrip() {
        TripRequestDto request = new TripRequestDto();
        request.setPassengerId("PASS-999");
        request.setPickupLat(12.9716);
        request.setPickupLng(77.5946);
        request.setDropoffLat(12.9352);
        request.setDropoffLng(77.6245);
        request.setPriority(TripPriority.HIGH);
        request.setPassengerCount(2);

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponseDto result = tripDispatchService.createTrip(request);

        assertNotNull(result);
        assertNotNull(result.getTripId());
        assertEquals("PASS-999", result.getPassengerId());
        assertEquals(TripStatus.REQUESTED, result.getStatus());
        assertTrue(result.getDistanceKm() > 0.0);
        assertTrue(result.getEstimatedDurationMin() > 0.0);
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("Should dispatch trip successfully")
    void testDispatchTrip() {
        Trip trip = new Trip("TRIP-001", "PASS-1", 12.97, 77.59, 12.93, 77.62, TripPriority.NORMAL, 1, 5.0, 15.0);
        when(tripRepository.findById("TRIP-001")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripResponseDto dispatched = tripDispatchService.dispatchTrip("TRIP-001", "FLEET-42");

        assertEquals(TripStatus.DISPATCHED, dispatched.getStatus());
        assertEquals("FLEET-42", dispatched.getVehicleId());
        assertNotNull(dispatched.getDispatchedAt());
    }

    @Test
    @DisplayName("Should prevent invalid state transitions")
    void testInvalidTransition() {
        Trip trip = new Trip("TRIP-001", "PASS-1", 12.97, 77.59, 12.93, 77.62, TripPriority.NORMAL, 1, 5.0, 15.0);
        when(tripRepository.findById("TRIP-001")).thenReturn(Optional.of(trip));

        // Directly transitioning from REQUESTED to COMPLETED must fail
        assertThrows(IllegalStateException.class, () ->
                tripDispatchService.updateStatus("TRIP-001", TripStatus.COMPLETED)
        );
    }
}

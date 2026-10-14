package com.scalemesh.telemetry.service;

import com.scalemesh.common.dto.FleetStatsDto;
import com.scalemesh.common.dto.TelemetryEventDto;
import com.scalemesh.common.enums.VehicleStatus;
import com.scalemesh.telemetry.model.VehicleTelemetry;
import com.scalemesh.telemetry.repository.VehicleTelemetryRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetTelemetryServiceTest {

    @Mock
    private VehicleTelemetryRepository telemetryRepository;

    private FleetTelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new FleetTelemetryService(telemetryRepository, new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("Should successfully ingest single telemetry event")
    void testIngestSingleEvent() {
        TelemetryEventDto dto = new TelemetryEventDto(
                "VEH-101",
                Instant.now(),
                12.9716,
                77.5946,
                45.5,
                88.0,
                12500.0,
                VehicleStatus.EN_ROUTE
        );

        VehicleTelemetry savedEntity = new VehicleTelemetry(
                dto.getVehicleId(),
                dto.getTimestamp(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getSpeedKmh(),
                dto.getBatteryPercent(),
                dto.getOdometerKm(),
                dto.getStatus(),
                32.0,
                85.0
        );

        when(telemetryRepository.save(any(VehicleTelemetry.class))).thenReturn(savedEntity);

        VehicleTelemetry result = telemetryService.ingestEvent(dto);

        assertNotNull(result);
        assertEquals("VEH-101", result.getVehicleId());
        assertEquals(VehicleStatus.EN_ROUTE, result.getStatus());
        verify(telemetryRepository, times(1)).save(any(VehicleTelemetry.class));
    }

    @Test
    @DisplayName("Should calculate fleet statistics accurately")
    void testCalculateFleetStats() {
        VehicleTelemetry v1 = new VehicleTelemetry("V1", Instant.now(), 12.97, 77.59, 40.0, 90.0, 1000.0, VehicleStatus.EN_ROUTE, 32.0, 80.0);
        VehicleTelemetry v2 = new VehicleTelemetry("V2", Instant.now(), 12.98, 77.60, 60.0, 70.0, 2000.0, VehicleStatus.IDLE, 31.0, 75.0);

        when(telemetryRepository.findLatestTelemetryForAllVehicles()).thenReturn(Arrays.asList(v1, v2));
        when(telemetryRepository.count()).thenReturn(150L);

        FleetStatsDto stats = telemetryService.getFleetStats();

        assertEquals(150L, stats.getTotalEvents());
        assertEquals(2L, stats.getActiveVehicles());
        assertEquals(50.0, stats.getAverageSpeedKmh(), 0.1);
        assertEquals(80.0, stats.getAverageBatteryPercent(), 0.1);
        assertTrue(stats.getStatusBreakdown().containsKey("EN_ROUTE"));
        assertTrue(stats.getStatusBreakdown().containsKey("IDLE"));
    }
}

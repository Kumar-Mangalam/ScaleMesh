package com.scalemesh.incident.service;

import com.scalemesh.common.dto.IncidentReportRequest;
import com.scalemesh.common.dto.IncidentResponseDto;
import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.common.enums.IncidentType;
import com.scalemesh.incident.model.Incident;
import com.scalemesh.incident.repository.IncidentRepository;
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
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(incidentRepository, new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("Should report incident and set status to OPEN")
    void testReportIncident() {
        IncidentReportRequest request = new IncidentReportRequest(
                "VEH-501", IncidentType.LOW_BATTERY, IncidentSeverity.MEDIUM,
                "Battery dropped below 15% threshold", 12.9716, 77.5946
        );

        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidentResponseDto response = incidentService.reportIncident(request);

        assertNotNull(response);
        assertNotNull(response.getIncidentId());
        assertEquals("VEH-501", response.getVehicleId());
        assertEquals(IncidentType.LOW_BATTERY, response.getIncidentType());
        assertEquals(IncidentSeverity.MEDIUM, response.getSeverity());
        assertEquals(IncidentStatus.OPEN, response.getStatus());
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    @DisplayName("Should resolve incident and record resolution notes")
    void testResolveIncident() {
        Incident incident = new Incident(
                "INC-001", "VEH-501", IncidentType.BREAKDOWN,
                IncidentSeverity.HIGH, "Engine stall", 12.97, 77.59
        );

        when(incidentRepository.findById("INC-001")).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidentResponseDto resolved = incidentService.updateStatus(
                "INC-001", IncidentStatus.RESOLVED, "Towed to depot and sensor reset"
        );

        assertEquals(IncidentStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
        assertEquals("Towed to depot and sensor reset", resolved.getResolutionNotes());
    }
}

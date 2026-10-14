package com.scalemesh.incident.service;

import com.scalemesh.common.dto.IncidentReportRequest;
import com.scalemesh.common.dto.IncidentResponseDto;
import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.incident.model.Incident;
import com.scalemesh.incident.repository.IncidentRepository;
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
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final Counter incidentsReportedCounter;
    private final Counter incidentsResolvedCounter;

    public IncidentService(IncidentRepository incidentRepository, MeterRegistry meterRegistry) {
        this.incidentRepository = incidentRepository;
        this.incidentsReportedCounter = Counter.builder("scalemesh.incidents.reported.total")
                .description("Total fleet incidents reported")
                .register(meterRegistry);
        this.incidentsResolvedCounter = Counter.builder("scalemesh.incidents.resolved.total")
                .description("Total fleet incidents resolved")
                .register(meterRegistry);
    }

    @Transactional
    public IncidentResponseDto reportIncident(IncidentReportRequest request) {
        String incidentId = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Incident incident = new Incident(
                incidentId,
                request.getVehicleId(),
                request.getIncidentType(),
                request.getSeverity(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude()
        );

        Incident saved = incidentRepository.save(incident);
        incidentsReportedCounter.increment();
        return toDto(saved);
    }

    @Transactional
    public IncidentResponseDto updateStatus(String incidentId, IncidentStatus status, String resolutionNotes) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + incidentId));

        incident.setStatus(status);
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            incident.setResolutionNotes(resolutionNotes);
        }

        if (status == IncidentStatus.RESOLVED || status == IncidentStatus.DISMISSED) {
            incident.setResolvedAt(Instant.now());
            if (status == IncidentStatus.RESOLVED) {
                incidentsResolvedCounter.increment();
            }
        }

        Incident updated = incidentRepository.save(incident);
        return toDto(updated);
    }

    @Transactional(readOnly = true)
    public IncidentResponseDto getIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + incidentId));
        return toDto(incident);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponseDto> listIncidents(IncidentStatus status, IncidentSeverity severity, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Incident> incidentPage;

        if (status != null && severity != null) {
            incidentPage = incidentRepository.findByStatusAndSeverity(status, severity, pageable);
        } else if (status != null) {
            incidentPage = incidentRepository.findByStatus(status, pageable);
        } else if (severity != null) {
            incidentPage = incidentRepository.findBySeverity(severity, pageable);
        } else {
            incidentPage = incidentRepository.findAll(pageable);
        }

        return incidentPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalIncidents", incidentRepository.count());

        Map<String, Long> statusMap = new HashMap<>();
        for (IncidentStatus s : IncidentStatus.values()) {
            statusMap.put(s.name().toLowerCase(), incidentRepository.countByStatus(s));
        }
        stats.put("byStatus", statusMap);

        Map<String, Long> severityMap = new HashMap<>();
        for (IncidentSeverity sev : IncidentSeverity.values()) {
            severityMap.put(sev.name().toLowerCase(), incidentRepository.countBySeverity(sev));
        }
        stats.put("bySeverity", severityMap);

        return stats;
    }

    private IncidentResponseDto toDto(Incident incident) {
        IncidentResponseDto dto = new IncidentResponseDto(
                incident.getId(),
                incident.getVehicleId(),
                incident.getIncidentType(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getDescription(),
                incident.getLatitude(),
                incident.getLongitude(),
                incident.getReportedAt()
        );
        dto.setResolvedAt(incident.getResolvedAt());
        dto.setResolutionNotes(incident.getResolutionNotes());
        return dto;
    }
}

package com.scalemesh.incident.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.common.dto.IncidentReportRequest;
import com.scalemesh.common.dto.IncidentResponseDto;
import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.incident.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponseDto>> reportIncident(
            @Valid @RequestBody IncidentReportRequest request) {
        IncidentResponseDto reported = incidentService.reportIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Incident reported successfully", reported));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponseDto>> getIncident(@PathVariable String id) {
        IncidentResponseDto incident = incidentService.getIncident(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentResponseDto>>> listIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IncidentResponseDto> incidents = incidentService.listIncidents(status, severity, page, size);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentResponseDto>> updateStatus(
            @PathVariable String id,
            @RequestParam IncidentStatus status,
            @RequestParam(required = false) String resolutionNotes) {
        IncidentResponseDto updated = incidentService.updateStatus(id, status, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @RequestMapping(value = "/{id}/resolve", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<IncidentResponseDto>> resolveIncident(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Resolved via operator dashboard") String resolutionNotes) {
        IncidentResponseDto updated = incidentService.updateStatus(id, IncidentStatus.RESOLVED, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = incidentService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Incident Manager Service is healthy and operational");
    }
}

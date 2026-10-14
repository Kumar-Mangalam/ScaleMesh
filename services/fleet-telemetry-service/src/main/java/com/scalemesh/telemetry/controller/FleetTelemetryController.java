package com.scalemesh.telemetry.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.common.dto.BatchTelemetryRequest;
import com.scalemesh.common.dto.FleetStatsDto;
import com.scalemesh.common.dto.TelemetryEventDto;
import com.scalemesh.common.enums.VehicleStatus;
import com.scalemesh.telemetry.model.VehicleTelemetry;
import com.scalemesh.telemetry.service.FleetTelemetryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class FleetTelemetryController {

    private final FleetTelemetryService telemetryService;

    public FleetTelemetryController(FleetTelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleTelemetry>> ingestTelemetry(
            @Valid @RequestBody TelemetryEventDto eventDto) {
        VehicleTelemetry saved = telemetryService.ingestEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Telemetry event ingested successfully", saved));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Integer>> ingestBatch(
            @Valid @RequestBody BatchTelemetryRequest batchRequest) {
        List<VehicleTelemetry> saved = telemetryService.ingestBatch(batchRequest.getEvents());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Batch telemetry ingested successfully", saved.size()));
    }

    @GetMapping("/vehicles/{vehicleId}/latest")
    public ResponseEntity<ApiResponse<VehicleTelemetry>> getLatestForVehicle(
            @PathVariable String vehicleId) {
        return telemetryService.getLatestForVehicle(vehicleId)
                .map(t -> ResponseEntity.ok(ApiResponse.success(t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "No telemetry found for vehicle: " + vehicleId)));
    }

    @GetMapping("/vehicles/{vehicleId}/history")
    public ResponseEntity<ApiResponse<Page<VehicleTelemetry>>> getHistoryForVehicle(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VehicleTelemetry> history = telemetryService.getHistoryForVehicle(vehicleId, page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/vehicles/active")
    public ResponseEntity<ApiResponse<List<VehicleTelemetry>>> getActiveVehicles(
            @RequestParam(required = false) VehicleStatus status) {
        List<VehicleTelemetry> vehicles = telemetryService.getActiveVehicles(status);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FleetStatsDto>> getFleetStats() {
        FleetStatsDto stats = telemetryService.getFleetStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fleet Telemetry Service is healthy and operational");
    }
}

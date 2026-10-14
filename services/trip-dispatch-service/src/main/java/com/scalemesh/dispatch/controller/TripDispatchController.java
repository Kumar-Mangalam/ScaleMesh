package com.scalemesh.dispatch.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.common.dto.TripRequestDto;
import com.scalemesh.common.dto.TripResponseDto;
import com.scalemesh.common.enums.TripStatus;
import com.scalemesh.dispatch.service.TripDispatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripDispatchController {

    private final TripDispatchService tripDispatchService;

    public TripDispatchController(TripDispatchService tripDispatchService) {
        this.tripDispatchService = tripDispatchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponseDto>> createTrip(
            @Valid @RequestBody TripRequestDto request) {
        TripResponseDto created = tripDispatchService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Trip request created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTrip(@PathVariable String id) {
        TripResponseDto trip = tripDispatchService.getTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TripResponseDto>>> listTrips(
            @RequestParam(required = false) TripStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TripResponseDto> trips = tripDispatchService.listTrips(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @RequestMapping(value = "/{id}/dispatch", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<TripResponseDto>> dispatchTrip(
            @PathVariable String id,
            @RequestParam(required = false) String vehicleId) {
        TripResponseDto dispatched = tripDispatchService.dispatchTrip(id, vehicleId);
        return ResponseEntity.ok(ApiResponse.success(dispatched));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TripResponseDto>> updateStatus(
            @PathVariable String id,
            @RequestParam TripStatus status) {
        TripResponseDto updated = tripDispatchService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = tripDispatchService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trip Dispatch Service is healthy and operational");
    }
}

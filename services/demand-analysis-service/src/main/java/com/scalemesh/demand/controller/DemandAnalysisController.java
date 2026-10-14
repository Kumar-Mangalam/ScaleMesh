package com.scalemesh.demand.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.common.dto.DemandComputeRequest;
import com.scalemesh.common.dto.DemandComputeResult;
import com.scalemesh.demand.service.DemandAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demand")
public class DemandAnalysisController {

    private final DemandAnalysisService demandAnalysisService;

    public DemandAnalysisController(DemandAnalysisService demandAnalysisService) {
        this.demandAnalysisService = demandAnalysisService;
    }

    @PostMapping(value = {"/compute", "/calculate"})
    public ResponseEntity<ApiResponse<DemandComputeResult>> computeDemand(
            @Valid @RequestBody DemandComputeRequest request) {
        DemandComputeResult result = demandAnalysisService.computeDemand(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping(value = {"/current", "/latest"})
    public ResponseEntity<ApiResponse<DemandComputeResult>> getCurrentDemand() {
        DemandComputeResult result = demandAnalysisService.getLatestDemand();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        Map<String, Object> metrics = demandAnalysisService.getMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Demand Analysis Service is healthy and operational");
    }
}

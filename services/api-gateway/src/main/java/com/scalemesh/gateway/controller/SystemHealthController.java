package com.scalemesh.gateway.controller;

import com.scalemesh.common.dto.ApiResponse;
import com.scalemesh.gateway.service.GatewayProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemHealthController {

    private final GatewayProxyService proxyService;

    public SystemHealthController(GatewayProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ScaleMesh API Gateway is healthy and running on port 8081");
    }

    @GetMapping("/api/system/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> systemHealth() {
        Map<String, Object> healthMap = proxyService.checkSystemHealth();
        return ResponseEntity.ok(ApiResponse.success(healthMap));
    }
}

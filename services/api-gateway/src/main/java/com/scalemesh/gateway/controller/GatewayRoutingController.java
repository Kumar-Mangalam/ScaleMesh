package com.scalemesh.gateway.controller;

import com.scalemesh.gateway.config.GatewayProperties;
import com.scalemesh.gateway.service.GatewayProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayRoutingController {

    private final GatewayProxyService proxyService;
    private final GatewayProperties gatewayProperties;

    public GatewayRoutingController(GatewayProxyService proxyService, GatewayProperties gatewayProperties) {
        this.proxyService = proxyService;
        this.gatewayProperties = gatewayProperties;
    }

    @RequestMapping(value = {"/api/telemetry/**", "/api/nodes/**"})
    public ResponseEntity<byte[]> routeTelemetry(
            HttpMethod method,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.forwardRequest(
                gatewayProperties.getTelemetry().getUrl(),
                request.getRequestURI(),
                method,
                body,
                request
        );
    }

    @RequestMapping("/api/trips/**")
    public ResponseEntity<byte[]> routeTrips(
            HttpMethod method,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.forwardRequest(
                gatewayProperties.getDispatch().getUrl(),
                request.getRequestURI(),
                method,
                body,
                request
        );
    }

    @RequestMapping("/api/routes/**")
    public ResponseEntity<byte[]> routeRoutes(
            HttpMethod method,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.forwardRequest(
                gatewayProperties.getRouting().getUrl(),
                request.getRequestURI(),
                method,
                body,
                request
        );
    }

    @RequestMapping("/api/demand/**")
    public ResponseEntity<byte[]> routeDemand(
            HttpMethod method,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.forwardRequest(
                gatewayProperties.getDemand().getUrl(),
                request.getRequestURI(),
                method,
                body,
                request
        );
    }

    @RequestMapping("/api/incidents/**")
    public ResponseEntity<byte[]> routeIncidents(
            HttpMethod method,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {
        return proxyService.forwardRequest(
                gatewayProperties.getIncident().getUrl(),
                request.getRequestURI(),
                method,
                body,
                request
        );
    }
}

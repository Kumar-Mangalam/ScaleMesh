package com.scalemesh.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalemesh.common.dto.ErrorResponse;
import com.scalemesh.gateway.config.GatewayProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GatewayProxyService {

    private final RestTemplate restTemplate;
    private final GatewayProperties gatewayProperties;
    private final ObjectMapper objectMapper;
    private final Counter requestsTotal;
    private final Counter downstreamErrors;

    public GatewayProxyService(RestTemplate restTemplate, GatewayProperties gatewayProperties,
                               ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.gatewayProperties = gatewayProperties;
        this.objectMapper = objectMapper;
        this.requestsTotal = Counter.builder("scalemesh.gateway.requests.total")
                .description("Total requests processed by API Gateway")
                .register(meterRegistry);
        this.downstreamErrors = Counter.builder("scalemesh.gateway.downstream.errors.total")
                .description("Total downstream connectivity failures")
                .register(meterRegistry);
    }

    public ResponseEntity<byte[]> forwardRequest(String baseUrl, String path, HttpMethod method,
                                                  byte[] body, HttpServletRequest request) {
        requestsTotal.increment();

        String queryString = request.getQueryString();
        String targetUrl = baseUrl + path + (queryString != null && !queryString.isBlank() ? "?" + queryString : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }
        headers.set("X-Forwarded-Host", request.getServerName() + ":" + request.getServerPort());
        headers.set("X-Forwarded-For", request.getRemoteAddr());

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(targetUrl), method, entity, byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            for (Map.Entry<String, List<String>> header : response.getHeaders().entrySet()) {
                String headerName = header.getKey();
                if (!isHopByHop(headerName)) {
                    responseHeaders.put(headerName, header.getValue());
                }
            }
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (ResourceAccessException ex) {
            downstreamErrors.increment();
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Downstream Service Unavailable",
                    "Failed to connect to backend microservice at: " + baseUrl,
                    path
            );
            try {
                byte[] errorBytes = objectMapper.writeValueAsBytes(error);
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(errorBytes, responseHeaders, HttpStatus.SERVICE_UNAVAILABLE);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        } catch (Exception ex) {
            downstreamErrors.increment();
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Gateway Forwarding Error",
                    ex.getMessage(),
                    path
            );
            try {
                byte[] errorBytes = objectMapper.writeValueAsBytes(error);
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(errorBytes, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> healthMap = new LinkedHashMap<>();
        Map<String, String> servicesStatus = new LinkedHashMap<>();

        servicesStatus.put("api-gateway", "UP");

        Map<String, String> targets = new LinkedHashMap<>();
        targets.put("fleet-telemetry-service", gatewayProperties.getTelemetry().getUrl() + "/actuator/health");
        targets.put("trip-dispatch-service", gatewayProperties.getDispatch().getUrl() + "/actuator/health");
        targets.put("route-engine-service", gatewayProperties.getRouting().getUrl() + "/actuator/health");
        targets.put("demand-analysis-service", gatewayProperties.getDemand().getUrl() + "/actuator/health");
        targets.put("incident-manager-service", gatewayProperties.getIncident().getUrl() + "/actuator/health");

        Map<String, CompletableFuture<String>> futures = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : targets.entrySet()) {
            futures.put(entry.getKey(), CompletableFuture.supplyAsync(() -> probeService(entry.getValue())));
        }

        for (Map.Entry<String, CompletableFuture<String>> entry : futures.entrySet()) {
            try {
                servicesStatus.put(entry.getKey(), entry.getValue().get(3, TimeUnit.SECONDS));
            } catch (Exception e) {
                servicesStatus.put(entry.getKey(), "DOWN");
            }
        }

        boolean allUp = servicesStatus.values().stream().allMatch("UP"::equals);
        healthMap.put("status", allUp ? "HEALTHY" : "DEGRADED");
        healthMap.put("services", servicesStatus);
        healthMap.put("timestamp", new Date());

        return healthMap;
    }

    private String probeService(String healthUrl) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "transfer-encoding", "content-length", "connection", "keep-alive",
            "proxy-authenticate", "proxy-authorization", "te", "trailers", "upgrade"
    );

    private boolean isHopByHop(String headerName) {
        return headerName != null && HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase());
    }
}

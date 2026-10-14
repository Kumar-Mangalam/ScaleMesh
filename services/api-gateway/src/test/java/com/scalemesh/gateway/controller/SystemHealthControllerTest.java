package com.scalemesh.gateway.controller;

import com.scalemesh.gateway.service.GatewayProxyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemHealthController.class)
class SystemHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayProxyService proxyService;

    @Test
    @DisplayName("GET /api/health - returns Gateway operational health string")
    void testGatewayHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("ScaleMesh API Gateway is healthy and running on port 8081"));
    }

    @Test
    @DisplayName("GET /api/system/health - returns aggregate system health")
    void testSystemHealth() throws Exception {
        Map<String, Object> healthMap = new LinkedHashMap<>();
        healthMap.put("status", "HEALTHY");
        healthMap.put("services", Collections.singletonMap("api-gateway", "UP"));

        when(proxyService.checkSystemHealth()).thenReturn(healthMap);

        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("HEALTHY"));
    }
}

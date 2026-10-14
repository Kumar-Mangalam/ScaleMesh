package com.scalemesh.telemetry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalemesh.telemetry.dto.CreateNodeRequest;
import com.scalemesh.telemetry.model.ComputeNode;
import com.scalemesh.telemetry.service.ComputeNodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ComputeNodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComputeNodeService nodeService;

    @Test
    @DisplayName("POST /api/nodes - should create a compute node")
    void testCreateNode() throws Exception {
        CreateNodeRequest request = new CreateNodeRequest("worker-test-1", 4, 16);
        ComputeNode createdNode = new ComputeNode("node-uuid-1", "worker-test-1", 4, 16);

        when(nodeService.createNode(eq("worker-test-1"), eq(4), eq(16))).thenReturn(createdNode);

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("node-uuid-1"))
                .andExpect(jsonPath("$.name").value("worker-test-1"))
                .andExpect(jsonPath("$.status").value("STOPPED"));
    }

    @Test
    @DisplayName("PUT /api/nodes/{id}/start - should transition status to RUNNING")
    void testStartNode() throws Exception {
        ComputeNode runningNode = new ComputeNode("node-uuid-1", "worker-test-1", 4, 16);
        runningNode.setStatus("RUNNING");

        when(nodeService.startNode("node-uuid-1")).thenReturn(runningNode);

        mockMvc.perform(put("/api/nodes/node-uuid-1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    @DisplayName("POST /api/nodes - should reject invalid node parameters")
    void testInvalidNodeValidation() throws Exception {
        CreateNodeRequest invalidRequest = new CreateNodeRequest("", -2, 0);

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

package com.scalemesh.telemetry.controller;

import com.scalemesh.telemetry.dto.CreateNodeRequest;
import com.scalemesh.telemetry.model.ComputeNode;
import com.scalemesh.telemetry.service.ComputeNodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class ComputeNodeController {

    private final ComputeNodeService nodeService;

    public ComputeNodeController(ComputeNodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping
    public ResponseEntity<ComputeNode> createNode(@Valid @RequestBody CreateNodeRequest request) {
        ComputeNode node = nodeService.createNode(
                request.getName(),
                request.getCpuCores(),
                request.getMemoryGb()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }

    @GetMapping
    public List<ComputeNode> getAllNodes() {
        return nodeService.getAllNodes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComputeNode> getNodeById(@PathVariable String id) {
        ComputeNode node = nodeService.getNodeById(id);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComputeNode> updateNode(@PathVariable String id, @Valid @RequestBody CreateNodeRequest request) {
        ComputeNode updatedNode = nodeService.updateNode(
                id,
                request.getName(),
                request.getCpuCores(),
                request.getMemoryGb()
        );
        if (updatedNode == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedNode);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<ComputeNode> startNode(@PathVariable String id) {
        ComputeNode node = nodeService.startNode(id);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }

    @PutMapping("/{id}/stop")
    public ResponseEntity<ComputeNode> stopNode(@PathVariable String id) {
        ComputeNode node = nodeService.stopNode(id);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable String id) {
        boolean deleted = nodeService.deleteNode(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

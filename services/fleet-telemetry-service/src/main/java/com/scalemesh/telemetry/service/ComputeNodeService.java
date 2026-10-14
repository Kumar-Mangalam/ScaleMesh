package com.scalemesh.telemetry.service;

import com.scalemesh.telemetry.model.ComputeNode;
import com.scalemesh.telemetry.repository.ComputeNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ComputeNodeService {

    private final ComputeNodeRepository nodeRepository;

    public ComputeNodeService(ComputeNodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public ComputeNode createNode(String name, int cpuCores, int memoryGb) {
        ComputeNode node = new ComputeNode(
                UUID.randomUUID().toString(),
                name,
                cpuCores,
                memoryGb
        );
        return nodeRepository.save(node);
    }

    public List<ComputeNode> getAllNodes() {
        return nodeRepository.findAll();
    }

    public ComputeNode getNodeById(String id) {
        return nodeRepository.findById(id).orElse(null);
    }

    public ComputeNode updateNode(String id, String name, int cpuCores, int memoryGb) {
        ComputeNode node = getNodeById(id);
        if (node == null) {
            return null;
        }
        node.setName(name);
        node.setCpuCores(cpuCores);
        node.setMemoryGb(memoryGb);
        return nodeRepository.save(node);
    }

    public ComputeNode startNode(String id) {
        ComputeNode node = getNodeById(id);
        if (node == null) {
            return null;
        }
        node.setStatus("RUNNING");
        return nodeRepository.save(node);
    }

    public ComputeNode stopNode(String id) {
        ComputeNode node = getNodeById(id);
        if (node == null) {
            return null;
        }
        node.setStatus("STOPPED");
        return nodeRepository.save(node);
    }

    public boolean deleteNode(String id) {
        if (!nodeRepository.existsById(id)) {
            return false;
        }
        nodeRepository.deleteById(id);
        return true;
    }
}

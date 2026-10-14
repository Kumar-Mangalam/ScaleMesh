package com.scalemesh.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "compute_nodes")
public class ComputeNode {

    @Id
    private String id;

    private String name;
    private String status;
    private int cpuCores;
    private int memoryGb;

    public ComputeNode() {
    }

    public ComputeNode(String id, String name, int cpuCores, int memoryGb) {
        this.id = id;
        this.name = name;
        this.cpuCores = cpuCores;
        this.memoryGb = memoryGb;
        this.status = "STOPPED";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public int getMemoryGb() {
        return memoryGb;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        if ("RUNNING".equals(status) || "STOPPED".equals(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException(
                "Invalid status. Allowed values are RUNNING and STOPPED."
            );
        }
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public void setMemoryGb(int memoryGb) {
        this.memoryGb = memoryGb;
    }
}

package com.scalemesh.telemetry.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateNodeRequest {

    @NotBlank(message = "Node name cannot be blank")
    @Size(min = 3, max = 50, message = "Node name must be between 3 and 50 characters")
    private String name;

    @Min(value = 1, message = "CPU cores must be at least 1")
    @Max(value = 128, message = "CPU cores cannot exceed 128")
    private int cpuCores;

    @Min(value = 1, message = "Memory must be at least 1 GB")
    @Max(value = 1024, message = "Memory cannot exceed 1024 GB")
    private int memoryGb;

    public CreateNodeRequest() {
    }

    public CreateNodeRequest(String name, int cpuCores, int memoryGb) {
        this.name = name;
        this.cpuCores = cpuCores;
        this.memoryGb = memoryGb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getMemoryGb() {
        return memoryGb;
    }

    public void setMemoryGb(int memoryGb) {
        this.memoryGb = memoryGb;
    }
}

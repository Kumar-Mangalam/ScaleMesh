package com.scalemesh.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class DemandComputeRequest {

    @NotBlank(message = "Region ID cannot be blank")
    private String regionId = "metro-central";

    @Min(value = 5, message = "Grid size must be >= 5")
    @Max(value = 500, message = "Grid size must be <= 500")
    private int gridSize = 50;

    @Min(value = 1, message = "Iterations must be >= 1")
    @Max(value = 100000, message = "Iterations must be <= 100000")
    private int iterationCount = 5000;

    @Min(value = 0, message = "CPU burn must be >= 0 ms")
    @Max(value = 5000, message = "CPU burn must be <= 5000 ms")
    private int cpuBurnMs = 50;

    @Min(value = 1, message = "Intensity must be >= 1")
    @Max(value = 10, message = "Intensity must be <= 10")
    private int intensityLevel = 3;

    public DemandComputeRequest() {
    }

    public DemandComputeRequest(String regionId, int gridSize, int iterationCount, int cpuBurnMs, int intensityLevel) {
        this.regionId = regionId;
        this.gridSize = gridSize;
        this.iterationCount = iterationCount;
        this.cpuBurnMs = cpuBurnMs;
        this.intensityLevel = intensityLevel;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getCpuBurnMs() {
        return cpuBurnMs;
    }

    public void setCpuBurnMs(int cpuBurnMs) {
        this.cpuBurnMs = cpuBurnMs;
    }

    public int getIntensityLevel() {
        return intensityLevel;
    }

    public void setIntensityLevel(int intensityLevel) {
        this.intensityLevel = intensityLevel;
    }
}

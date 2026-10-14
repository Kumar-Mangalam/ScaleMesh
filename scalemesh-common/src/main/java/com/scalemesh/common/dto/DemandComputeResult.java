package com.scalemesh.common.dto;

import java.time.Instant;
import java.util.Map;

public class DemandComputeResult {

    private String computationId;
    private String regionId;
    private Instant timestamp;
    private double overallDemandIndex;
    private int recommendedFleetSize;
    private int activeVehiclesNeeded;
    private long executionDurationMs;
    private String processedByHost;
    private Map<String, Double> clusterDemandScores;
    private int iterationsCompleted;

    public DemandComputeResult() {
        this.timestamp = Instant.now();
    }

    public DemandComputeResult(String computationId, String regionId, double overallDemandIndex,
                               int recommendedFleetSize, int activeVehiclesNeeded, long executionDurationMs,
                               String processedByHost, Map<String, Double> clusterDemandScores,
                               int iterationsCompleted) {
        this.computationId = computationId;
        this.regionId = regionId;
        this.timestamp = Instant.now();
        this.overallDemandIndex = overallDemandIndex;
        this.recommendedFleetSize = recommendedFleetSize;
        this.activeVehiclesNeeded = activeVehiclesNeeded;
        this.executionDurationMs = executionDurationMs;
        this.processedByHost = processedByHost;
        this.clusterDemandScores = clusterDemandScores;
        this.iterationsCompleted = iterationsCompleted;
    }

    public String getComputationId() {
        return computationId;
    }

    public void setComputationId(String computationId) {
        this.computationId = computationId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getOverallDemandIndex() {
        return overallDemandIndex;
    }

    public void setOverallDemandIndex(double overallDemandIndex) {
        this.overallDemandIndex = overallDemandIndex;
    }

    public int getRecommendedFleetSize() {
        return recommendedFleetSize;
    }

    public void setRecommendedFleetSize(int recommendedFleetSize) {
        this.recommendedFleetSize = recommendedFleetSize;
    }

    public int getActiveVehiclesNeeded() {
        return activeVehiclesNeeded;
    }

    public void setActiveVehiclesNeeded(int activeVehiclesNeeded) {
        this.activeVehiclesNeeded = activeVehiclesNeeded;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public String getProcessedByHost() {
        return processedByHost;
    }

    public void setProcessedByHost(String processedByHost) {
        this.processedByHost = processedByHost;
    }

    public Map<String, Double> getClusterDemandScores() {
        return clusterDemandScores;
    }

    public void setClusterDemandScores(Map<String, Double> clusterDemandScores) {
        this.clusterDemandScores = clusterDemandScores;
    }

    public int getIterationsCompleted() {
        return iterationsCompleted;
    }

    public void setIterationsCompleted(int iterationsCompleted) {
        this.iterationsCompleted = iterationsCompleted;
    }
}

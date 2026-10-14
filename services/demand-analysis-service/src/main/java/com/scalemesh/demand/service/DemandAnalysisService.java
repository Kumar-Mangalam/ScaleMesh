package com.scalemesh.demand.service;

import com.scalemesh.common.dto.DemandComputeRequest;
import com.scalemesh.common.dto.DemandComputeResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DemandAnalysisService {

    private final Counter computationsCounter;
    private final Timer computationTimer;
    private final AtomicInteger activeConcurrency = new AtomicInteger(0);
    private final String hostName;
    private volatile DemandComputeResult latestResult;

    public DemandAnalysisService(MeterRegistry meterRegistry) {
        this.computationsCounter = Counter.builder("scalemesh.demand.computations.total")
                .description("Total demand analysis computations executed")
                .register(meterRegistry);
        this.computationTimer = Timer.builder("scalemesh.demand.computation.duration")
                .description("Execution latency of demand analysis computations")
                .register(meterRegistry);
        meterRegistry.gauge("scalemesh.demand.active.concurrency", activeConcurrency);

        String detectedHost;
        try {
            String envHost = System.getenv("HOSTNAME");
            detectedHost = (envHost != null && !envHost.isBlank())
                    ? envHost
                    : InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            detectedHost = "demand-worker-" + UUID.randomUUID().toString().substring(0, 6);
        }
        this.hostName = detectedHost;

        // Initialize with default state
        this.latestResult = new DemandComputeResult(
                "INIT", "metro-central", 0.65, 45, 38,
                15, this.hostName, Collections.singletonMap("downtown", 0.75), 1000
        );
    }

    public DemandComputeResult computeDemand(DemandComputeRequest request) {
        activeConcurrency.incrementAndGet();
        long startTime = System.nanoTime();
        String computationId = "COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            int gridSize = request.getGridSize();
            int iterations = request.getIterationCount();
            int cpuBurnMs = request.getCpuBurnMs();
            int intensity = request.getIntensityLevel();

            // 1. Algorithmic Matrix Convolution (Spatial Demand Surface)
            double[][] grid = new double[gridSize][gridSize];
            double totalScore = 0.0;

            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    double distFromCenter = Math.sqrt(Math.pow(i - gridSize / 2.0, 2) + Math.pow(j - gridSize / 2.0, 2));
                    grid[i][j] = Math.exp(-distFromCenter / (gridSize / 4.0)) * intensity;
                    totalScore += grid[i][j];
                }
            }

            // 2. Controlled CPU Burn Loop (Designed for Kubernetes HPA trigger under load)
            long burnNanos = TimeUnit.MILLISECONDS.toNanos(cpuBurnMs);
            long burnStart = System.nanoTime();
            double mathSink = 0.0;
            int completedIterations = 0;

            while ((System.nanoTime() - burnStart) < burnNanos || completedIterations < iterations) {
                mathSink += Math.sin(completedIterations) * Math.cos(completedIterations * 0.5);
                completedIterations++;
                if (completedIterations > 500000) { // Safety ceiling
                    break;
                }
            }

            // 3. Cluster Demand Calculations
            double avgScore = totalScore / (gridSize * gridSize);
            double normalizedDemandIndex = Math.min(1.0, Math.round((avgScore / 10.0) * 100.0) / 100.0);

            Map<String, Double> clusterScores = new LinkedHashMap<>();
            clusterScores.put("downtown-core", Math.round(normalizedDemandIndex * 1.35 * 100.0) / 100.0);
            clusterScores.put("tech-corridor", Math.round(normalizedDemandIndex * 1.15 * 100.0) / 100.0);
            clusterScores.put("airport-suburb", Math.round(normalizedDemandIndex * 0.85 * 100.0) / 100.0);
            clusterScores.put("industrial-dock", Math.round(normalizedDemandIndex * 0.65 * 100.0) / 100.0);

            int recommendedFleet = (int) Math.max(10, Math.round(normalizedDemandIndex * 120.0 * intensity));
            int activeNeeded = (int) Math.round(recommendedFleet * 0.85);

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            DemandComputeResult result = new DemandComputeResult(
                    computationId,
                    request.getRegionId(),
                    normalizedDemandIndex,
                    recommendedFleet,
                    activeNeeded,
                    durationMs,
                    this.hostName,
                    clusterScores,
                    completedIterations
            );

            this.latestResult = result;
            computationsCounter.increment();
            computationTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            return result;
        } finally {
            activeConcurrency.decrementAndGet();
        }
    }

    public DemandComputeResult getLatestDemand() {
        return latestResult;
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalComputations", computationsCounter.count());
        metrics.put("activeConcurrency", activeConcurrency.get());
        metrics.put("hostName", hostName);
        metrics.put("averageLatencyMs", computationTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("maxLatencyMs", computationTimer.max(TimeUnit.MILLISECONDS));
        return metrics;
    }
}

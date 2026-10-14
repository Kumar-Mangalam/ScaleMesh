package com.scalemesh.demand.service;

import com.scalemesh.common.dto.DemandComputeRequest;
import com.scalemesh.common.dto.DemandComputeResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DemandAnalysisServiceTest {

    private DemandAnalysisService demandAnalysisService;

    @BeforeEach
    void setUp() {
        demandAnalysisService = new DemandAnalysisService(new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("Should execute demand computation with valid output scores")
    void testComputeDemand() {
        DemandComputeRequest request = new DemandComputeRequest("downtown-central", 20, 1000, 10, 4);
        DemandComputeResult result = demandAnalysisService.computeDemand(request);

        assertNotNull(result);
        assertNotNull(result.getComputationId());
        assertEquals("downtown-central", result.getRegionId());
        assertTrue(result.getOverallDemandIndex() >= 0.0 && result.getOverallDemandIndex() <= 1.0);
        assertTrue(result.getRecommendedFleetSize() >= 10);
        assertTrue(result.getActiveVehiclesNeeded() > 0);
        assertNotNull(result.getProcessedByHost());
        assertNotNull(result.getClusterDemandScores());
        assertTrue(result.getClusterDemandScores().containsKey("downtown-core"));
    }

    @Test
    @DisplayName("Should update and expose computation metrics")
    void testMetricsUpdate() {
        DemandComputeRequest request = new DemandComputeRequest("tech-park", 15, 500, 5, 2);
        demandAnalysisService.computeDemand(request);

        Map<String, Object> metrics = demandAnalysisService.getMetrics();
        assertNotNull(metrics);
        assertEquals(1.0, (Double) metrics.get("totalComputations"));
        assertEquals(0, (Integer) metrics.get("activeConcurrency"));
        assertNotNull(metrics.get("hostName"));
    }
}

package com.scalemesh.telemetry.service;

import com.scalemesh.common.dto.FleetStatsDto;
import com.scalemesh.common.dto.TelemetryEventDto;
import com.scalemesh.common.enums.VehicleStatus;
import com.scalemesh.telemetry.model.VehicleTelemetry;
import com.scalemesh.telemetry.repository.VehicleTelemetryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FleetTelemetryService {

    private final VehicleTelemetryRepository telemetryRepository;
    private final Counter telemetryEventsCounter;
    private final Counter batchEventsCounter;

    public FleetTelemetryService(VehicleTelemetryRepository telemetryRepository, MeterRegistry meterRegistry) {
        this.telemetryRepository = telemetryRepository;
        this.telemetryEventsCounter = Counter.builder("scalemesh.telemetry.events.total")
                .description("Total telemetry events ingested")
                .register(meterRegistry);
        this.batchEventsCounter = Counter.builder("scalemesh.telemetry.batches.total")
                .description("Total telemetry batches ingested")
                .register(meterRegistry);
    }

    @Transactional
    public VehicleTelemetry ingestEvent(TelemetryEventDto dto) {
        VehicleTelemetry entity = toEntity(dto);
        VehicleTelemetry saved = telemetryRepository.save(entity);
        telemetryEventsCounter.increment();
        return saved;
    }

    @Transactional
    public List<VehicleTelemetry> ingestBatch(List<TelemetryEventDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }
        List<VehicleTelemetry> entities = dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<VehicleTelemetry> saved = telemetryRepository.saveAll(entities);
        telemetryEventsCounter.increment(dtoList.size());
        batchEventsCounter.increment();
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<VehicleTelemetry> getLatestForVehicle(String vehicleId) {
        return telemetryRepository.findTopByVehicleIdOrderByTimestampDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public Page<VehicleTelemetry> getHistoryForVehicle(String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return telemetryRepository.findByVehicleIdOrderByTimestampDesc(vehicleId, pageable);
    }

    @Transactional(readOnly = true)
    public List<VehicleTelemetry> getActiveVehicles(VehicleStatus status) {
        if (status != null) {
            return telemetryRepository.findLatestVehiclesByStatus(status);
        }
        return telemetryRepository.findLatestTelemetryForAllVehicles();
    }

    @Transactional(readOnly = true)
    public FleetStatsDto getFleetStats() {
        List<VehicleTelemetry> latestList = telemetryRepository.findLatestTelemetryForAllVehicles();
        long totalEvents = telemetryRepository.count();

        if (latestList.isEmpty()) {
            return new FleetStatsDto(totalEvents, 0, 0.0, 0.0, Collections.emptyMap());
        }

        double totalSpeed = 0.0;
        double totalBattery = 0.0;
        Map<String, Long> statusMap = new HashMap<>();

        for (VehicleTelemetry t : latestList) {
            totalSpeed += t.getSpeedKmh();
            totalBattery += t.getBatteryPercent();
            String st = t.getStatus().name();
            statusMap.put(st, statusMap.getOrDefault(st, 0L) + 1L);
        }

        double avgSpeed = Math.round((totalSpeed / latestList.size()) * 10.0) / 10.0;
        double avgBattery = Math.round((totalBattery / latestList.size()) * 10.0) / 10.0;

        return new FleetStatsDto(totalEvents, latestList.size(), avgSpeed, avgBattery, statusMap);
    }

    private VehicleTelemetry toEntity(TelemetryEventDto dto) {
        return new VehicleTelemetry(
                dto.getVehicleId(),
                dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getSpeedKmh(),
                dto.getBatteryPercent(),
                dto.getOdometerKm(),
                dto.getStatus() != null ? dto.getStatus() : VehicleStatus.IDLE,
                dto.getTirePressurePsi(),
                dto.getEngineTemperatureC()
        );
    }
}

package com.scalemesh.telemetry.repository;

import com.scalemesh.common.enums.VehicleStatus;
import com.scalemesh.telemetry.model.VehicleTelemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleTelemetryRepository extends JpaRepository<VehicleTelemetry, Long> {

    Optional<VehicleTelemetry> findTopByVehicleIdOrderByTimestampDesc(String vehicleId);

    Page<VehicleTelemetry> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);

    @Query("SELECT DISTINCT t.vehicleId FROM VehicleTelemetry t")
    List<String> findDistinctVehicleIds();

    @Query("SELECT t FROM VehicleTelemetry t WHERE t.id IN (" +
            "SELECT MAX(t2.id) FROM VehicleTelemetry t2 GROUP BY t2.vehicleId)")
    List<VehicleTelemetry> findLatestTelemetryForAllVehicles();

    @Query("SELECT t FROM VehicleTelemetry t WHERE t.id IN (" +
            "SELECT MAX(t2.id) FROM VehicleTelemetry t2 GROUP BY t2.vehicleId) " +
            "AND t.status = :status")
    List<VehicleTelemetry> findLatestVehiclesByStatus(@Param("status") VehicleStatus status);

    long countByTimestampAfter(Instant timestamp);
}

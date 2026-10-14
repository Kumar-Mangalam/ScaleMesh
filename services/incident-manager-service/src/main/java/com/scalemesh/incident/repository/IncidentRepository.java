package com.scalemesh.incident.repository;

import com.scalemesh.common.enums.IncidentSeverity;
import com.scalemesh.common.enums.IncidentStatus;
import com.scalemesh.incident.model.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);

    Page<Incident> findByStatusAndSeverity(IncidentStatus status, IncidentSeverity severity, Pageable pageable);

    List<Incident> findByVehicleId(String vehicleId);

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);
}

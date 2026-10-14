package com.scalemesh.dispatch.repository;

import com.scalemesh.common.enums.TripStatus;
import com.scalemesh.dispatch.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

    Page<Trip> findByStatus(TripStatus status, Pageable pageable);

    List<Trip> findByPassengerId(String passengerId);

    List<Trip> findByAssignedVehicleId(String vehicleId);

    long countByStatus(TripStatus status);
}

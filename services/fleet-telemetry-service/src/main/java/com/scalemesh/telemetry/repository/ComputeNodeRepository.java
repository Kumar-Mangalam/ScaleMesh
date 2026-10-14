package com.scalemesh.telemetry.repository;

import com.scalemesh.telemetry.model.ComputeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputeNodeRepository extends JpaRepository<ComputeNode, String> {
}

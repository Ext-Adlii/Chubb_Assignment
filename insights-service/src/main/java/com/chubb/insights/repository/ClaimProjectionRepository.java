package com.chubb.insights.repository;

import com.chubb.insights.domain.ClaimProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimProjectionRepository extends JpaRepository<ClaimProjection, UUID> {
    List<ClaimProjection> findByAssignedOfficerId(String assignedOfficerId);
    List<ClaimProjection> findByMarket(String market);
}

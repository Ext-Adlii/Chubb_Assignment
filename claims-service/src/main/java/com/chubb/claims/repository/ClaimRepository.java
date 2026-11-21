package com.chubb.claims.repository;

import com.chubb.claims.domain.Claim;
import com.chubb.claims.domain.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByStatusAndAssignedOfficerId(ClaimStatus status, String assignedOfficerId);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByClaimantId(String claimantId);
}

package com.chubb.claims.repository;

import com.chubb.claims.domain.InfoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoRequestRepository extends JpaRepository<InfoRequest, java.util.UUID> {
}

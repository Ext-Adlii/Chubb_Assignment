package com.chubb.claims.repository;

import com.chubb.claims.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, java.util.UUID> {
}

package com.chubb.insights.controller;

import com.chubb.insights.domain.ClaimProjection;
import com.chubb.insights.repository.ClaimProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final ClaimProjectionRepository repository;

    @GetMapping("/workload")
    public ResponseEntity<Map<String, Long>> getWorkload() {
        List<ClaimProjection> claims = repository.findAll();
        Map<String, Long> workload = claims.stream()
                .filter(c -> c.getAssignedOfficerId() != null)
                .collect(Collectors.groupingBy(ClaimProjection::getAssignedOfficerId, Collectors.counting()));
        return ResponseEntity.ok(workload);
    }

    @GetMapping("/exposure")
    public ResponseEntity<Map<String, BigDecimal>> getExposure() {
        List<ClaimProjection> claims = repository.findAll();
        Map<String, BigDecimal> exposure = claims.stream()
                .collect(Collectors.groupingBy(
                        ClaimProjection::getMarket,
                        Collectors.reducing(BigDecimal.ZERO, ClaimProjection::getReserveAmount, BigDecimal::add)
                ));
        return ResponseEntity.ok(exposure);
    }
}

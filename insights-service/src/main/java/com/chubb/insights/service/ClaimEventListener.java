package com.chubb.insights.service;

import com.chubb.insights.domain.ClaimProjection;
import com.chubb.insights.repository.ClaimProjectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimEventListener {

    private final ClaimProjectionRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "claims.events", groupId = "insights-service-group")
    public void handleClaimEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            // Payload structure can be Claim object or specific event DTO
            UUID id = node.has("id") ? UUID.fromString(node.get("id").asText()) : null;
            if (id == null) return;

            String claimNumber = node.has("claimNumber") ? node.get("claimNumber").asText() : "UNKNOWN";
            String market = node.has("market") ? node.get("market").asText() : "DEFAULT";
            String status = node.has("status") ? node.get("status").asText() : "SUBMITTED";
            String assignedOfficerId = node.has("assignedOfficerId") && !node.get("assignedOfficerId").isNull()
                    ? node.get("assignedOfficerId").asText() : null;
            BigDecimal reserveAmount = node.has("reserveAmount") && !node.get("reserveAmount").isNull()
                    ? new BigDecimal(node.get("reserveAmount").asText()) : BigDecimal.ZERO;

            // If decided (SETTLED or REJECTED), we can remove or update exposure
            if ("SETTLED".equals(status) || "REJECTED".equals(status)) {
                repository.deleteById(id);
                log.info("Claim {} decided ({}), removed from exposure projection", claimNumber, status);
                return;
            }

            ClaimProjection projection = repository.findById(id).orElse(ClaimProjection.builder().id(id).build());
            projection.setClaimNumber(claimNumber);
            projection.setMarket(market);
            projection.setStatus(status);
            projection.setAssignedOfficerId(assignedOfficerId);
            projection.setReserveAmount(reserveAmount);

            repository.save(projection);
            log.info("Updated projection for claim {} with status {}", claimNumber, status);
        } catch (Exception e) {
            log.error("Failed to process claims event message: {}", message, e);
        }
    }
}

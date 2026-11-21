package com.chubb.claims.service;

import com.chubb.claims.domain.*;
import com.chubb.claims.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final InfoRequestRepository infoRequestRepository;
    private final DecisionRepository decisionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Claim submitClaim(String idempotencyKey, Claim claim, String claimantId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existingKey = idempotencyKeyRepository.findById(idempotencyKey);
            if (existingKey.isPresent()) {
                return claimRepository.findById(existingKey.get().getClaimId())
                        .orElseThrow(() -> new IllegalStateException("Claim associated with idempotency key not found"));
            }
        }

        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimantId(claimantId);
        Claim savedClaim = claimRepository.save(claim);

        // Record status history
        recordStatusHistory(savedClaim.getId(), null, ClaimStatus.SUBMITTED, claimantId);

        // Record outbox event
        emitEvent(savedClaim.getId(), "CLAIM_SUBMITTED", savedClaim);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyKeyRepository.save(IdempotencyKey.builder()
                    .key(idempotencyKey)
                    .claimId(savedClaim.getId())
                    .build());
        }

        return savedClaim;
    }

    @Transactional
    public Claim assignClaim(UUID claimId, String officerId, String assignedBy) {
        Claim claim = getClaimOrThrow(claimId);
        ClaimStatus oldStatus = claim.getStatus();

        claim.assignTo(officerId);
        Claim updated = claimRepository.save(claim);

        if (oldStatus != updated.getStatus()) {
            recordStatusHistory(claimId, oldStatus, updated.getStatus(), assignedBy);
        }
        emitEvent(claimId, "CLAIM_ASSIGNED", updated);

        return updated;
    }

    @Transactional
    public Claim assessClaim(UUID claimId, BigDecimal reserveAmount, String assessedBy) {
        Claim claim = getClaimOrThrow(claimId);
        claim.recordAssessment(reserveAmount);
        Claim updated = claimRepository.save(claim);

        emitEvent(claimId, "CLAIM_ASSESSED", updated);
        return updated;
    }

    @Transactional
    public InfoRequest requestInfo(UUID claimId, String requestedBy, String question) {
        Claim claim = getClaimOrThrow(claimId);
        ClaimStatus oldStatus = claim.getStatus();

        claim.transitionTo(ClaimStatus.INFO_REQUESTED);
        claimRepository.save(claim);

        recordStatusHistory(claimId, oldStatus, ClaimStatus.INFO_REQUESTED, requestedBy);

        InfoRequest infoRequest = infoRequestRepository.save(InfoRequest.builder()
                .claimId(claimId)
                .requestedBy(requestedBy)
                .question(question)
                .build());

        emitEvent(claimId, "INFO_REQUESTED", infoRequest);
        return infoRequest;
    }

    @Transactional
    public InfoRequest respondToInfo(UUID infoRequestId, String responseText, String respondent) {
        InfoRequest infoRequest = infoRequestRepository.findById(infoRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Info request not found"));
        infoRequest.answer(responseText);
        InfoRequest updated = infoRequestRepository.save(infoRequest);

        Claim claim = getClaimOrThrow(infoRequest.getClaimId());
        ClaimStatus oldStatus = claim.getStatus();
        claim.transitionTo(ClaimStatus.UNDER_REVIEW);
        claimRepository.save(claim);

        recordStatusHistory(claim.getId(), oldStatus, ClaimStatus.UNDER_REVIEW, respondent);
        emitEvent(claim.getId(), "INFO_RESPONDED", updated);

        return updated;
    }

    @Transactional
    public Claim makeDecision(UUID claimId, ClaimStatus decisionStatus, String decidedBy, String rationale, BigDecimal amount) {
        if (decisionStatus != ClaimStatus.SETTLED && decisionStatus != ClaimStatus.REJECTED) {
            throw new IllegalArgumentException("Decision must be SETTLED or REJECTED");
        }

        Claim claim = getClaimOrThrow(claimId);
        ClaimStatus oldStatus = claim.getStatus();
        claim.transitionTo(decisionStatus);
        if (decisionStatus == ClaimStatus.SETTLED && amount != null) {
            claim.setReserveAmount(amount);
        }
        Claim updated = claimRepository.save(claim);

        recordStatusHistory(claimId, oldStatus, decisionStatus, decidedBy);

        Decision decision = decisionRepository.save(Decision.builder()
                .claimId(claimId)
                .decision(decisionStatus)
                .decidedBy(decidedBy)
                .rationale(rationale)
                .amount(amount)
                .build());

        emitEvent(claimId, "CLAIM_DECIDED", decision);
        return updated;
    }

    public Claim getClaimOrThrow(UUID claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
    }

    public List<Claim> getClaimsByQueue(ClaimStatus status, String officerId) {
        if (status != null && officerId != null) {
            return claimRepository.findByStatusAndAssignedOfficerId(status, officerId);
        } else if (status != null) {
            return claimRepository.findByStatus(status);
        }
        return claimRepository.findAll();
    }

    private void recordStatusHistory(UUID claimId, ClaimStatus from, ClaimStatus to, String changedBy) {
        statusHistoryRepository.save(StatusHistory.builder()
                .claimId(claimId)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .build());
    }

    private void emitEvent(UUID aggregateId, String type, Object payloadObj) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payloadObj);
            outboxEventRepository.save(OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .type(type)
                    .payload(jsonPayload)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }
}

package com.chubb.claims.controller;

import com.chubb.claims.domain.Claim;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.ClaimType;
import com.chubb.claims.domain.InfoRequest;
import com.chubb.claims.service.ClaimService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous-claimant") String claimantId,
            @RequestBody @Valid ClaimSubmitRequest request) {

        Claim claim = Claim.builder()
                .claimNumber(request.claimNumber())
                .type(request.type())
                .market(request.market())
                .policyNumber(request.policyNumber())
                .description(request.description())
                .incidentDate(request.incidentDate())
                .reserveAmount(request.reserveAmount())
                .build();

        Claim saved = claimService.submitClaim(idempotencyKey, claim, claimantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClaimResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaim(@PathVariable UUID id) {
        Claim claim = claimService.getClaimOrThrow(id);
        return ResponseEntity.ok(ClaimResponse.from(claim));
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponse>> listClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) String officerId) {
        List<Claim> claims = claimService.getClaimsByQueue(status, officerId);
        return ResponseEntity.ok(claims.stream().map(ClaimResponse::from).toList());
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ClaimResponse> assignClaim(
            @PathVariable UUID id,
            @RequestParam String officerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OFFICER") String role,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        if ("CLAIMANT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Claim updated = claimService.assignClaim(id, officerId, userId);
        return ResponseEntity.ok(ClaimResponse.from(updated));
    }

    @PostMapping("/{id}/assess")
    public ResponseEntity<ClaimResponse> assessClaim(
            @PathVariable UUID id,
            @RequestBody @Valid AssessmentRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "OFFICER") String role,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        if ("CLAIMANT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Claim updated = claimService.assessClaim(id, request.reserveAmount(), userId);
        return ResponseEntity.ok(ClaimResponse.from(updated));
    }

    @PostMapping("/{id}/info-requests")
    public ResponseEntity<InfoRequest> requestInfo(
            @PathVariable UUID id,
            @RequestBody @Valid InfoRequestCreateRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "OFFICER") String role,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        if ("CLAIMANT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        InfoRequest infoRequest = claimService.requestInfo(id, userId, request.question());
        return ResponseEntity.status(HttpStatus.CREATED).body(infoRequest);
    }

    @PostMapping("/info-requests/{infoRequestId}/respond")
    public ResponseEntity<InfoRequest> respondToInfo(
            @PathVariable UUID infoRequestId,
            @RequestBody @Valid InfoRequestResponse request,
            @RequestHeader(value = "X-User-Id", defaultValue = "claimant") String userId) {

        InfoRequest updated = claimService.respondToInfo(infoRequestId, request.response(), userId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<ClaimResponse> makeDecision(
            @PathVariable UUID id,
            @RequestBody @Valid DecisionRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "OFFICER") String role,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {

        if ("CLAIMANT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Claim updated = claimService.makeDecision(id, request.decision(), userId, request.rationale(), request.amount());
        return ResponseEntity.ok(ClaimResponse.from(updated));
    }

    // Records and DTOs
    public record ClaimSubmitRequest(
            @NotBlank String claimNumber,
            @NotNull ClaimType type,
            @NotBlank String market,
            @NotBlank String policyNumber,
            String description,
            @NotNull LocalDate incidentDate,
            BigDecimal reserveAmount
    ) {}

    public record AssessmentRequest(
            @NotNull BigDecimal reserveAmount
    ) {}

    public record InfoRequestCreateRequest(
            @NotBlank String question
    ) {}

    public record InfoRequestResponse(
            @NotBlank String response
    ) {}

    public record DecisionRequest(
            @NotNull ClaimStatus decision,
            String rationale,
            BigDecimal amount
    ) {}

    @Builder
    public record ClaimResponse(
            UUID id,
            String claimNumber,
            ClaimType type,
            String market,
            String claimantId,
            String policyNumber,
            String description,
            LocalDate incidentDate,
            ClaimStatus status,
            String assignedOfficerId,
            BigDecimal reserveAmount,
            Long version
    ) {
        public static ClaimResponse from(Claim claim) {
            return ClaimResponse.builder()
                    .id(claim.getId())
                    .claimNumber(claim.getClaimNumber())
                    .type(claim.getType())
                    .market(claim.getMarket())
                    .claimantId(claim.getClaimantId())
                    .policyNumber(claim.getPolicyNumber())
                    .description(claim.getDescription())
                    .incidentDate(claim.getIncidentDate())
                    .status(claim.getStatus())
                    .assignedOfficerId(claim.getAssignedOfficerId())
                    .reserveAmount(claim.getReserveAmount())
                    .version(claim.getVersion())
                    .build();
        }
    }
}

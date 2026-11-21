package com.chubb.claims.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType type;

    @Column(nullable = false)
    private String market;

    @Column(nullable = false)
    private String claimantId;

    @Column(nullable = false)
    private String policyNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    private String assignedOfficerId;

    private BigDecimal reserveAmount;

    @Version
    private Long version;

    public void transitionTo(ClaimStatus nextStatus) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Illegal status transition from " + status + " to " + nextStatus);
        }
        this.status = nextStatus;
    }

    public void assignTo(String officerId) {
        if (this.status != ClaimStatus.SUBMITTED && this.status != ClaimStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Claim cannot be assigned in status " + status);
        }
        this.assignedOfficerId = officerId;
        if (this.status == ClaimStatus.SUBMITTED) {
            transitionTo(ClaimStatus.UNDER_REVIEW);
        }
    }

    public void recordAssessment(BigDecimal amount) {
        if (this.status != ClaimStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Assessment can only be recorded in UNDER_REVIEW status");
        }
        this.reserveAmount = amount;
    }
}

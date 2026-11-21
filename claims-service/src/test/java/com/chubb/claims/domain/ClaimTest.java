package com.chubb.claims.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {

    @Test
    void testValidStateTransitionFlowToSettled() {
        Claim claim = Claim.builder()
                .claimNumber("CLM-001")
                .type(ClaimType.MOTOR)
                .market("US")
                .claimantId("claimant-123")
                .policyNumber("POL-999")
                .incidentDate(LocalDate.now())
                .status(ClaimStatus.SUBMITTED)
                .build();

        // SUBMITTED -> UNDER_REVIEW via assignment
        claim.assignTo("officer-1");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
        assertThat(claim.getAssignedOfficerId()).isEqualTo("officer-1");

        // UNDER_REVIEW -> INFO_REQUESTED
        claim.transitionTo(ClaimStatus.INFO_REQUESTED);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.INFO_REQUESTED);

        // INFO_REQUESTED -> UNDER_REVIEW
        claim.transitionTo(ClaimStatus.UNDER_REVIEW);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);

        // Record assessment
        claim.recordAssessment(new BigDecimal("12500.00"));
        assertThat(claim.getReserveAmount()).isEqualByComparingTo(new BigDecimal("12500.00"));

        // UNDER_REVIEW -> SETTLED
        claim.transitionTo(ClaimStatus.SETTLED);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.SETTLED);
    }

    @Test
    void testValidStateTransitionFlowToRejected() {
        Claim claim = Claim.builder()
                .claimNumber("CLM-002")
                .type(ClaimType.PROPERTY)
                .market("UK")
                .claimantId("claimant-456")
                .policyNumber("POL-888")
                .incidentDate(LocalDate.now())
                .status(ClaimStatus.SUBMITTED)
                .build();

        claim.transitionTo(ClaimStatus.UNDER_REVIEW);
        claim.transitionTo(ClaimStatus.REJECTED);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.REJECTED);
    }

    @Test
    void testInvalidStateTransitionsThrowException() {
        Claim claim = Claim.builder()
                .claimNumber("CLM-003")
                .type(ClaimType.MOTOR)
                .market("US")
                .claimantId("claimant-789")
                .policyNumber("POL-777")
                .incidentDate(LocalDate.now())
                .status(ClaimStatus.SUBMITTED)
                .build();

        // Cannot jump directly from SUBMITTED to SETTLED
        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.SETTLED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Illegal status transition");

        // Set to SETTLED
        claim.transitionTo(ClaimStatus.UNDER_REVIEW);
        claim.transitionTo(ClaimStatus.SETTLED);

        // Cannot transition out of SETTLED
        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.UNDER_REVIEW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testAssessmentOnlyAllowedInUnderReview() {
        Claim claim = Claim.builder()
                .claimNumber("CLM-004")
                .type(ClaimType.MOTOR)
                .market("US")
                .claimantId("claimant-111")
                .policyNumber("POL-111")
                .incidentDate(LocalDate.now())
                .status(ClaimStatus.SUBMITTED)
                .build();

        assertThatThrownBy(() -> claim.recordAssessment(new BigDecimal("1000.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Assessment can only be recorded in UNDER_REVIEW status");
    }
}

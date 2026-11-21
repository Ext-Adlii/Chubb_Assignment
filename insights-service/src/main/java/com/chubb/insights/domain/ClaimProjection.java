package com.chubb.insights.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claim_projections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimProjection {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String claimNumber;

    @Column(nullable = false)
    private String market;

    @Column(nullable = false)
    private String status;

    private String assignedOfficerId;

    private BigDecimal reserveAmount;
}

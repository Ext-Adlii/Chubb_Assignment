package com.chubb.claims.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID claimId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus decision;

    @Column(nullable = false)
    private String decidedBy;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime decidedAt = OffsetDateTime.now();
}

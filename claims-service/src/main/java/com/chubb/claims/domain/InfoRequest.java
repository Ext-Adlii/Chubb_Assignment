package com.chubb.claims.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "info_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID claimId;

    @Column(nullable = false)
    private String requestedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    private OffsetDateTime respondedAt;

    public void answer(String answerText) {
        if (this.response != null) {
            throw new IllegalStateException("Info request has already been answered.");
        }
        this.response = answerText;
        this.respondedAt = OffsetDateTime.now();
    }
}

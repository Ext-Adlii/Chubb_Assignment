package com.chubb.claims;

import com.chubb.claims.domain.Claim;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.ClaimType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClaimsServiceApplicationTests {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void flywayMigrationsApplyAndJpaValidatesEntity() {
        Claim claim = Claim.builder()
                .claimNumber("CLM-2026-0001")
                .type(ClaimType.MOTOR)
                .market("US")
                .claimantId(UUID.randomUUID().toString())
                .policyNumber("POL-12345")
                .description("Accident on highway")
                .incidentDate(LocalDate.now())
                .status(ClaimStatus.SUBMITTED)
                .reserveAmount(new BigDecimal("5000.00"))
                .build();

        Claim saved = entityManager.persistFlushFind(claim);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getClaimNumber()).isEqualTo("CLM-2026-0001");
        assertThat(saved.getVersion()).isNotNull();
    }
}

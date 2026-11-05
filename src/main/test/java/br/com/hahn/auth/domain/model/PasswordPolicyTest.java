package br.com.hahn.auth.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {
    @Test void disabledPolicyNeverExpires() {
        PasswordPolicy policy = new PasswordPolicy(); policy.setExpirationEnabled(false); policy.setDefaultPolicy(true);
        assertNull(policy.calculateExpirationDate(LocalDateTime.of(2026, 1, 1, 0, 0)));
    }
    @Test void defaultPolicyExpiresAfterNinetyDays() {
        PasswordPolicy policy = new PasswordPolicy(); policy.setExpirationEnabled(true); policy.setDefaultPolicy(true);
        var created = LocalDateTime.of(2026, 1, 1, 9, 15);
        assertEquals(created.plusDays(90), policy.calculateExpirationDate(created));
    }
    @Test void customPolicyExpiresAfterConfiguredMonths() {
        PasswordPolicy policy = new PasswordPolicy(); policy.setExpirationEnabled(true); policy.setDefaultPolicy(false); policy.setExpirationMonths(6);
        var created = LocalDateTime.of(2026, 1, 31, 9, 15);
        assertEquals(created.plusMonths(6), policy.calculateExpirationDate(created));
    }
}

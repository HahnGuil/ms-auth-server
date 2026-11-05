package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password policy of an application.
 *
 * @author HahnGuil
 * <p>When {@code expirationEnabled} is false the application has no policy at all (not even the
 * default one). When true, the password expires after {@link #DEFAULT_EXPIRATION_DAYS} days
 * (default policy) or after {@code expirationMonths} months (custom policy).</p>
 */
@Entity
@Table(name = "password_policy")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordPolicy {

    public static final long DEFAULT_EXPIRATION_DAYS = 90L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "expiration_enabled", nullable = false)
    private boolean expirationEnabled;

    @Column(name = "default_policy", nullable = false)
    private boolean defaultPolicy;

    @Column(name = "expiration_months")
    private Integer expirationMonths;

    /**
     * Calculates when a password created at the given date expires.
     *
     * @param passwordCreateDate the password creation date
     * @return the expiration date, or null when the policy does not expire passwords
     */
    public LocalDateTime calculateExpirationDate(LocalDateTime passwordCreateDate) {
        if (!expirationEnabled) {
            return null;
        }
        return defaultPolicy
                ? passwordCreateDate.plusDays(DEFAULT_EXPIRATION_DAYS)
                : passwordCreateDate.plusMonths(expirationMonths);
    }
}

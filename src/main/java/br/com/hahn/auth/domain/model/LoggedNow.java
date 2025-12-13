package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the current logged-in session of a user.
 *
 * @author HahnGuil
 * This entity maps to the "logged_now" table in the database and stores
 * information about the user's login session, including the user ID,
 * token log ID, login date, and refresh token usage details.
 */
@Entity
@Table(name = "logged_now")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggedNow {

    /**
     * The unique identifier for the logged session.
     * This value is auto-generated using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The unique identifier of the user associated with the logged session.
     * This value is stored in the "user_id" column and cannot be null.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * The unique identifier of the token log associated with the logged session.
     * This value is stored in the "token_log_id" column and cannot be null.
     */
    @Column(name = "token_log_id", nullable = false)
    private UUID tokenLogId;

    /**
     * The date and time when the user logged in.
     * This value is stored in the "date_login" column.
     */
    @Column(name = "date_login")
    private LocalDateTime dateLogin;

    /**
     * Indicates whether the refresh token was used during the session.
     * This value is stored in the "is_use_refresh" column.
     */
    @Column(name = "is_use_refresh")
    private boolean isUseRefresh;

    /**
     * The date and time when the refresh token was used.
     * This value is stored in the "date_refresh" column.
     */
    @Column(name = "date_refresh")
    private LocalDateTime dateRefresh;
}

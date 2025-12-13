package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a password reset request.
 *
 * @author HahnGuil
 * <p>Maps to a database record that holds the recovery code sent to a user,
 * the user's email, and the expiration date of the recovery code.</p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reset_password")
public class ResetPassword {

    /**
     * The primary key of the reset request.
     * This value is auto-generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The recovery code generated for password reset.
     * Stored in the "recover_code" column.
     */
    @Column(name = "recover_code")
    private String recoverCode;

    /**
     * The email address of the user requesting the password reset.
     * Stored in the "user_email" column.
     */
    @Column(name = "user_email")
    private String userEmail;

    /**
     * The date and time when the recovery code expires.
     * Stored in the "expiration_date" column.
     */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
}

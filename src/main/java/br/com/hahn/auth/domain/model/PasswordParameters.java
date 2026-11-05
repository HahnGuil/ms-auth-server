package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores the password of a user for a specific application.
 *
 * @author HahnGuil
 * <p>A user may be registered in several applications, each one with its own password,
 * its own expiration time and its own block status. The pair (user, application) is unique.</p>
 */
@Entity
@Table(name = "password_parameters",
        uniqueConstraints = @UniqueConstraint(name = "uk_password_parameters_user_application", columnNames = {"user_id", "application_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "password_create_date", nullable = false)
    private LocalDateTime passwordCreateDate;

    @Column(name = "block_user")
    private Boolean blockUser = false;
}

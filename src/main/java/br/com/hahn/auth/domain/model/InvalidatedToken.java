package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.TypeInvalidation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an invalidated token entity.
 *
 * @author HahnGuil
 * This class maps to the "invalidated_token" table in the database and stores
 * information about tokens that have been invalidated, including the user and
 * login log associated with the token, the date of invalidation, and the type
 * of invalidation.
 */
@Entity
@Table(name = "invalidated_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {

    /**
     * The unique identifier for the invalidated token.
     * This value is auto-generated using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The unique identifier of the user associated with the invalidated token.
     * This value is stored in the "user_id" column and cannot be null.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * The unique identifier of the login log associated with the invalidated token.
     * This value is stored in the "login_log_id" column and cannot be null.
     */
    @Column(name = "login_log_id", nullable = false)
    private UUID loginLogId;

    /**
     * The date and time when the token was invalidated.
     * This value is stored in the "date_invalidate" column.
     */
    @Column(name = "date_invalidate")
    private LocalDateTime dateInvalidate;

    /**
     * The type of invalidation for the token.
     * This value is stored in the "type_invalidation" column as a string.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_invalidation")
    private TypeInvalidation typeInvalidation;
}

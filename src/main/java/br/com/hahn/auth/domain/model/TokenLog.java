package br.com.hahn.auth.domain.model;


import br.com.hahn.auth.domain.enums.ScopeToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a log entry for a token.
 *
 * @author HahnGuil
 * This entity maps to the "token_log" table in the database and stores
 * information about token usage, such as its scope, creation date, and
 * whether it is active. It also maintains a relationship with the user
 * associated with the token.
 */
@Entity
@Table(name = "token_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenLog {

    /**
     * The unique identifier for the token log entry.
     * This value is auto-generated using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idTokenLog;

    /**
     * The scope of the token.
     * This value is stored as a string in the "scope_token" column.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_token")
    private ScopeToken scopeToken;

    /**
     * The date and time when the token log entry was created.
     * This value is stored in the "create_date" column.
     */
    @Column(name = "create_date")
    private LocalDateTime createDate;

    /**
     * Indicates whether the token is active.
     * This value is stored in the "active_token" column.
     */
    @Column(name = "active_token")
    private boolean activeToken;

    /**
     * The user associated with this token log entry.
     * This is a many-to-one relationship, fetched lazily, and mapped
     * to the "user_id" column.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * The unique identifier of the user associated with this token log entry.
     * This value is stored in the "user_id" column and cannot be null.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
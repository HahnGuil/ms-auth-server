package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a user entity in the system.
 *
 * @author HahnGuil
 * <p>This class maps to the "users" table in the database and stores
 * information about the user, including their credentials, personal details,
 * and relationships with other entities such as token logs and applications.</p>
 */
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    /**
     * The unique identifier for the user.
     * This value is auto-generated using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    /**
     * The username of the user.
     * Stored in the "user_name" column.
     */
    @Column(name = "user_name")
    private String username;

    /**
     * The password of the user.
     * Stored in the "password" column.
     */
    @Column(name = "password")
    private String password;

    /**
     * The date and time when the password was created.
     * Stored in the "password_create_date" column.
     */
    @Column(name = "password_create_date")
    private LocalDateTime passwordCreateDate;

    /**
     * The email address of the user.
     * Stored in the "user_email" column.
     */
    @Column(name = "user_email")
    private String email;

    /**
     * The first name of the user.
     * Stored in the "first_name" column.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * The last name of the user.
     * Stored in the "last_name" column.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * The URL of the user's profile picture.
     * Stored in the "picture_url" column.
     */
    @Column(name = "picture_url")
    private String pictureUrl;

    /**
     * Indicates whether the user is blocked.
     * Stored in the "block_user" column.
     */
    @Column(name = "block_user")
    private Boolean blockUser;

    /**
     * The type of the user.
     * Stored in the "type_user" column as a string.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_user")
    private TypeUser typeUser;

    /**
     * The role of the user.
     * Stored in the "user_role" column as a string.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole role;

    /**
     * The set of token logs associated with the user.
     * This is a one-to-many relationship, fetched lazily.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<TokenLog> tokenLogs;

    /**
     * The set of applications associated with the user.
     * This is a many-to-many relationship, mapped to the "user_application" table.
     */
    @ManyToMany
    @JoinTable(
            name = "user_application",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "application_id")
    )
    private Set<Application> applications;
}

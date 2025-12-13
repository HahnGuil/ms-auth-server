package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Represents an application entity.
 * <p>
 * @author HahnGuil
 * This class maps to the "application" table in the database and contains
 * information about the application, such as its ID, name, and associated users.
 */
@Entity
@Table(name = "application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Application {

    /**
     * The unique identifier for the application.
     * This value is auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the application.
     * This value is stored in the "name_application" column in the database.
     */
    @Column(name = "name_application")
    private String nameApplication;

    /**
     * The set of users associated with this application.
     * This relationship is mapped by the "applications" field in the User entity.
     */
    @ManyToMany(mappedBy = "applications")
    private Set<User> users;
}

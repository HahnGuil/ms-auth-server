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

@Entity
@Table(name = "USERS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(name = "User_name")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "password_create_date")
    private LocalDateTime passwordCreateDate;

    @Column(name = "User_email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Column(name = "user_bloc")
    private Boolean blockUser;

    @Column(name = "typeUser")
    private TypeUser typeUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole role;


    @ManyToMany(mappedBy = "users")
    private Set<LoginLog> loginLogs;

    @ManyToMany
    @JoinTable(
            name = "user_application",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "application_id")
    )
    private Set<Application> applications;

}

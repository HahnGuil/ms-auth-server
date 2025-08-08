package br.com.hahn.auth.domain.model;


import br.com.hahn.auth.domain.enums.ScopeToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "token_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idLoginLog;

    @Column(name = "scope_token")
    private ScopeToken scopeToken;

    @Column(name = "date_login")
    private LocalDateTime dateLogin;

    @ManyToMany
    @JoinTable(
            name = "token_log_user",
            joinColumns = @JoinColumn(name = "token_log_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;




}

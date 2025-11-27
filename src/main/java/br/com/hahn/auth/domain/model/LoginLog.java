package br.com.hahn.auth.domain.model;


import br.com.hahn.auth.domain.enums.ScopeToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    @Column(name = "active_token")
    private boolean activeToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id", nullable = false)
    private UUID userId;






}

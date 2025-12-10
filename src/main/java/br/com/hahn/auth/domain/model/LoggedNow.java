package br.com.hahn.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logged_now")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggedNow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_log_id", nullable = false)
    private UUID tokenLogId;

    @Column(name = "date_login")
    private LocalDateTime dateLogin;

    @Column(name = "is_use_refresh")
    private boolean isUseRefresh;

    @Column(name = "date_refresh")
    private LocalDateTime dateRefresh;
}

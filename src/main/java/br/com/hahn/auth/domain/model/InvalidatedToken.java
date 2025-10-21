package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.TypeInvalidation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invalidated_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "login_log_id", nullable = false)
    private UUID loginLogId;

    @Column(name = "date_invalidate")
    private LocalDateTime dateInvalidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_invalidation")
    private TypeInvalidation typeInvalidation;
}

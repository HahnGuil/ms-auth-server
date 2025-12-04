package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {

    @Modifying
    @Query("UPDATE LoginLog ll SET ll.activeToken = false WHERE ll.userId = :userId")
    void deactivateActiveTokenByUserId(UUID userId);

    @Query("SELECT ll.activeToken FROM LoginLog ll WHERE ll.idLoginLog = :loginLogId")
    boolean findActiveTokenByLoginLogId(UUID loginLogId);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.activeToken = true AND ll.dateLogin < :expirationTime")
    List<LoginLog> findExpiredActiveTokens(LocalDateTime expirationTime);

    LoginLog findTopByUserIdOrderByDateLoginDesc(UUID userId);

}
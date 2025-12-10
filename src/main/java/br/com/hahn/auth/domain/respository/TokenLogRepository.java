package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.TokenLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TokenLogRepository extends JpaRepository<TokenLog, UUID> {

    @Modifying
    @Query("UPDATE TokenLog ll SET ll.activeToken = false WHERE ll.userId = :userId")
    void deactivateActiveTokenByUserId(UUID userId);

    @Query("SELECT ll.activeToken FROM TokenLog ll WHERE ll.createDate = :loginLogId")
    boolean findActiveTokenByLoginLogId(UUID loginLogId);

    @Query("SELECT ll FROM TokenLog ll WHERE ll.activeToken = true AND ll.createDate < :expirationTime")
    List<TokenLog> findExpiredActiveTokens(LocalDateTime expirationTime);

    TokenLog findTopByUserIdOrderByCreateDateDesc(UUID userId);

}
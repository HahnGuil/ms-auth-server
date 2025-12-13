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
    @Query("UPDATE TokenLog tl SET tl.activeToken = false WHERE tl.userId = :userId AND tl.activeToken = true")
    void deactivateActiveTokenByUserId(UUID userId);

    @Query("SELECT CASE WHEN COUNT(tl) > 0 THEN true ELSE false END FROM TokenLog tl WHERE tl.idTokenLog = :loginLogId AND tl.activeToken = true")
    boolean findActiveTokenByLoginLogId(UUID loginLogId);

    @Query("SELECT tl FROM TokenLog tl WHERE tl.activeToken = true AND tl.createDate < :expirationTime")
    List<TokenLog> findExpiredActiveTokens(LocalDateTime expirationTime);

    TokenLog findTopByUserIdOrderByCreateDateDesc(UUID userId);
    
    

}
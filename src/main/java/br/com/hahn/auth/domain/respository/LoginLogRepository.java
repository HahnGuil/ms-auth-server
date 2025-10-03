package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {

    @Modifying
    @Query("UPDATE LoginLog ll SET ll.activeToken = false WHERE EXISTS (SELECT u FROM ll.users u WHERE u.userId = :userId)")
    void deactivateActiveTokenByUserId(UUID userId);


}
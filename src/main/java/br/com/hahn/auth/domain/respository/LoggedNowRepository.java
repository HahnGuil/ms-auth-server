package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.LoggedNow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoggedNowRepository extends JpaRepository<LoggedNow, UUID> {

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM LoginLog ll WHERE ll.userId = :userId")
    void deleteByUserId(UUID userId);
}

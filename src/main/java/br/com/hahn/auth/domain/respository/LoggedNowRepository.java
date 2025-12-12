package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.LoggedNow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoggedNowRepository extends JpaRepository<LoggedNow, UUID> {

    List<LoggedNow> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM LoggedNow ln WHERE ln.userId = :userId")
    void deleteByUserId(UUID userId);


}

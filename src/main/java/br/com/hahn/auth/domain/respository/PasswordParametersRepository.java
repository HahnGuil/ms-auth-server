package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.PasswordParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordParametersRepository extends JpaRepository<PasswordParameters, UUID> {

    @Query("SELECT p FROM PasswordParameters p JOIN FETCH p.application WHERE p.user.userId = :userId AND p.application.publicId = :applicationPublicId")
    Optional<PasswordParameters> findByUserAndApplicationPublicId(@Param("userId") UUID userId, @Param("applicationPublicId") UUID applicationPublicId);

    boolean existsByUserUserIdAndApplicationId(UUID userId, Long applicationId);

    @Query("SELECT p FROM PasswordParameters p JOIN FETCH p.user JOIN FETCH p.application WHERE p.application.id IN :applicationIds")
    List<PasswordParameters> findByApplicationIds(@Param("applicationIds") Collection<Long> applicationIds);
}

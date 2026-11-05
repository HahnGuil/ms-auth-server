package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.ResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {

    Optional<ResetPassword> findByUserEmailAndApplicationId(String userEmail, Long applicationId);

    boolean existsByUserEmailAndApplicationId(String email, Long applicationId);

    int deleteByExpirationDateBefore(LocalDateTime dateTime);


}

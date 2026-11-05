package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, UUID> {

    @Query("SELECT pp FROM PasswordPolicy pp JOIN FETCH pp.application WHERE pp.expirationEnabled = true")
    List<PasswordPolicy> findAllWithExpirationEnabled();
}

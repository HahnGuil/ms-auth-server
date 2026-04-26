package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findApplicationByPublicId(UUID publicId);
}

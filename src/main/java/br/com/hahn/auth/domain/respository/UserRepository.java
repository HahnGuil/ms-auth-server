package br.com.hahn.auth.domain.respository;

import br.com.hahn.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.passwordCreateDate = :passwordCreateDate, u.blockUser = false WHERE u.email = :email AND u.userId = :id")
    void updatePasswordByEmailAndId(@Param("password") String password, @Param("email") String email, @Param("id") UUID id, @Param("passwordCreateDate") LocalDateTime passwordCreateDate);

    @Query("SELECT u FROM User u WHERE u.passwordCreateDate < :dateThreshold")
    List<User> findUsersWithPasswordOlderThan(@Param("dateThreshold") LocalDateTime dateThreshold);

    @Query("SELECT u FROM User u WHERE u.passwordCreateDate > :dateThreshold")
    List<User> findUsersWithPasswordNewerThan(@Param("dateThreshold") LocalDateTime dateThreshold);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.applications WHERE u.email = :email")
    Optional<User> findByEmailWithApplications(@Param("email") String email);

}

package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.respository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updatePassword(String email, UUID id, String newPassword) {
        userRepository.updatePasswordByEmailAndId(newPassword, email, id);
    }

}

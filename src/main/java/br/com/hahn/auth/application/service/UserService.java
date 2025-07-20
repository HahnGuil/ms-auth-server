package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found. Check email and password or register a new user."));
    }

    public User convertToEntity(UserRequestDTO userRequestDTO, String encodePassword){
        User user = new User();

        user.setUsername(userRequestDTO.userName());
        user.setPassword(encodePassword);
        user.setEmail(userRequestDTO.email());
        user.setFirstName(userRequestDTO.firstName());
        user.setLastName(userRequestDTO.lastName());
        user.setPictureUrl(userRequestDTO.pictureUrl());

        return user;
    }

    protected UserRequestDTO convertOAuthUserToRequestDTO(OAuth2User oAuth2User){
        return new UserRequestDTO(
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("email"),
                "",
                oAuth2User.getAttribute("given_name"), // First name
                oAuth2User.getAttribute("family_name"), // Last name
                oAuth2User.getAttribute("picture") // Picture URL
        );
    }

    @Transactional
    public void saveUser(User user){
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String email, UUID id, String newPassword) {
        userRepository.updatePasswordByEmailAndId(newPassword, email, id);
    }

}

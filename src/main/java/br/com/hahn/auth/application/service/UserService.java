package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.ChangePasswordRequestDTO;
import br.com.hahn.auth.application.dto.request.LoginRequestDTO;
import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidOperationExecption;
import br.com.hahn.auth.application.execption.UserAlreadyExistException;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public void existsByEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("User already exists with email, please use other or try to recover your password");
        }
    }

    public LoginResponseDTO userlogin(LoginRequestDTO bodyRequest) {
        User user = findByEmail(bodyRequest.email());

        if (!validadePassword(bodyRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect email or password");
        }

        return new LoginResponseDTO(user.getEmail(), this.tokenService.generateToken(user), this.tokenService.generateRefreshToken(user));
    }

    public String generateRefreshToken(String email){
        User user = findByEmail(email);
        return tokenService.generateRefreshToken(user);
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = convertToEntity(userRequestDTO);
        saveUser(user);
        return new UserResponseDTO(user.getUsername(), user.getEmail());
    }

    public String refreshAccessToken(String refreshToken) {
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        User user = findByEmail(email);
        return tokenService.generateToken(user);
    }

    public User convertToEntity(UserRequestDTO userRequestDTO) {

        User user = new User();
        user.setUsername(userRequestDTO.userName());
        user.setPassword(encodePassword(userRequestDTO.password()));
        user.setEmail(userRequestDTO.email());
        user.setFirstName(userRequestDTO.firstName());
        user.setLastName(userRequestDTO.lastName());
        user.setPictureUrl(userRequestDTO.pictureUrl());

        return user;
    }

    public String processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUserFromOAuth(oAuth2User));
        return generateTokenForUser(user);
    }

    public void updatePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        User user = findByEmail(changePasswordRequestDTO.email());
        user.setPassword(encodePassword(changePasswordRequestDTO.newPassword()));

        try {
            userRepository.save(user);
        } catch (Exception _) {
            throw new InvalidOperationExecption("Operation not allowed, please try again later");
        }
    }

    protected boolean validadePassword(String loginPassword, String userPassword) {
        return passwordEncoder.matches(loginPassword, userPassword);
    }

    protected User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAlreadyExistException("User not found. Check email and password or register a new user."));
    }

    protected void saveUser(User user) {
        userRepository.save(user);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generateTokenForUser(User user) {
        return tokenService.generateToken(user);
    }

    private User createNewUserFromOAuth(OAuth2User oAuth2User) {
        UserRequestDTO userRequestDTO = convertOAuthUserToRequestDTO(oAuth2User);
        User newUser = convertToEntity(userRequestDTO);
        saveUser(newUser);
        return newUser;
    }

    private UserRequestDTO convertOAuthUserToRequestDTO(OAuth2User oAuth2User){
        return new UserRequestDTO(
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("email"),
                "",
                oAuth2User.getAttribute("given_name"), // First name
                oAuth2User.getAttribute("family_name"), // Last name
                oAuth2User.getAttribute("picture") // Picture URL
        );
    }
}

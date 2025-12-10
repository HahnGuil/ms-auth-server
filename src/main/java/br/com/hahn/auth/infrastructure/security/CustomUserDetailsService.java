package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.domain.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username (email).
     * <p>
     * Attempts to find the user in the repository by email. If the user is not found,
     * an error is logged and a {@link UsernameNotFoundException} is thrown.
     *
     * @param username the email (username) of the user to be loaded
     * @return a {@link UserDetails} containing the user's email, password, and an empty list of authorities
     * @throws UsernameNotFoundException if the user is not found in the repository
     * @author HahnGuil
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("CustomUserDetailsService: Find user by username at: {}", Instant.now());
        var user = userRepository.findByEmail(username).orElseThrow(() -> {
            var ex = new UsernameNotFoundException("User " + username + " not found");
            log.error("CustomUserDetailsService: User {} not found at {}", username, Instant.now(), ex);
            return ex;
        });
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}

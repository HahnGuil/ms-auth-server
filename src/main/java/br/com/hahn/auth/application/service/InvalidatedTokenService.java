package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.respository.InvalidatedTokenRepository;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvalidatedTokenService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Saves an invalidated token to the repository.
     * <p>
     * This method logs an informational message with the token ID, user ID, and the
     * current timestamp before saving the token. The operation is transactional to
     * ensure data consistency.
     *
     * @author HahnGuil
     * @param invalidatedToken the invalidated token to be saved
     */
    @Transactional
    public void save(InvalidatedToken invalidatedToken){
        log.info("InvalidTokenService: Save invalidateToken: {} for user id: {}, at: {}", invalidatedToken.getId(), invalidatedToken.getUserId(), DateTimeConverter.formatInstantNow());
        invalidatedTokenRepository.save(invalidatedToken);
    }
}

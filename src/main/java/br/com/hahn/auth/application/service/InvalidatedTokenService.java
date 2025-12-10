package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.respository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvalidatedTokenService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Transactional
    public void save(InvalidatedToken invalidatedToken){
        log.info("InvalidTokenService: Save invalidateToken: {} for user id: {}, at: {}", invalidatedToken.getId(), invalidatedToken.getUserId(), Instant.now());
        invalidatedTokenRepository.save(invalidatedToken);
    }
}

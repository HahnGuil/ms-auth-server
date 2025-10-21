package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.respository.InvalidatedTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class InvalidatedTokenService {

    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Transactional
    public void save(InvalidatedToken invalidatedToken){
        invalidatedTokenRepository.save(invalidatedToken);
    }
}

package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.RecoveryCodeExpiradeException;
import br.com.hahn.auth.application.execption.ResetPasswordNotFoundException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    public ResetPassword findByEmail(String email){
        log.info("ResetPasswordService: Find by email");
        return resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> new ResetPasswordNotFoundException("Not found reset password for this user"));
    }

    public boolean existsByUserEmail(String email){
        return resetPasswordRepository.existsByUserEmail(email);
    }

    public void deleteResetExistingPassword(String email){
        log.info("ResetPasswordService: delete reset existing password");
        ResetPassword resetPassword = findByEmail(email);
        deletebyId(resetPassword.getId());
    }

    public void validateTokenExpiration(String recoverToken){
        log.info("ResetPasswordService: validate token expiration");
        ResetPassword resetPassword = findByEmail(recoverToken);

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            throw new RecoveryCodeExpiradeException("Recover Token is expired");
        }
    }

    public int deleteByExpirationDateBefore(LocalDateTime dataTime){
        log.info("ResetPasswordService: Delete reset password by expiration date");
        return resetPasswordRepository.deleteByExpirationDateBefore(dataTime);
    }

    @Transactional
    public void createResetPassword(User user, String recoverCode){
        log.info("ResetPasswordService: create reset password");
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(recoverCode);
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }

    private void deletebyId(Long id) {
        log.info("ResetPasswordService: delete resete password by id");
        resetPasswordRepository.deleteById(id);
    }


}

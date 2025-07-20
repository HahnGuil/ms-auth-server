package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.RecoveryCodeExpiradeException;
import br.com.hahn.auth.application.execption.ResetPasswordNotFoundException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    public ResetPasswordService(ResetPasswordRepository resetPasswordRepository) {
        this.resetPasswordRepository = resetPasswordRepository;
    }

    public ResetPassword findByEmail(String email){
        return resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> new ResetPasswordNotFoundException("Not found reset password for this user"));
    }

    public Optional<ResetPassword> checkRecoverCodeEmail(String email){
        return Optional.ofNullable(resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> new ResetPasswordNotFoundException("No password reset request found for this user")));
    }

    public ResetPassword checkExistingResetToken(String email) {
        ResetPassword resetPassword = resetPasswordRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResetPasswordNotFoundException("No password reset request found for this user"));

        if (resetPassword.getExpirationDate().isBefore(LocalDateTime.now())) {
            return resetPassword;
        } else {
            throw new RecoveryCodeExpiradeException("Recovery code is expired");
        }
    }

    public void save(ResetPassword resetPassword){
        resetPasswordRepository.save(resetPassword);
    }

    @Transactional
    public void createResetPassword(User user, String recoverCode){
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(recoverCode);
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }


}

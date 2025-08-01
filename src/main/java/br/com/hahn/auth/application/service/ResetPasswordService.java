package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.RecoveryCodeExpiradeException;
import br.com.hahn.auth.application.execption.ResetPasswordNotFoundException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    public ResetPasswordService(ResetPasswordRepository resetPasswordRepository) {
        this.resetPasswordRepository = resetPasswordRepository;
    }

    public ResetPassword findByEmail(String email){
        return resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> new ResetPasswordNotFoundException("Not found reset password for this user"));
    }

    public boolean existsByUserEmail(String email){
        return resetPasswordRepository.existsByUserEmail(email);
    }

    public void deleteResetExistingPassword(String email){
        ResetPassword resetPassword = findByEmail(email);
        deletebyId(resetPassword.getId());
    }

    public void validateTokenExpiration(String recoverToken){
        ResetPassword resetPassword = findByEmail(recoverToken);

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            throw new RecoveryCodeExpiradeException("Recover Token is expired");
        }
    }

    public int deleteByExpirationDateBefore(LocalDateTime dataTime){
        return resetPasswordRepository.deleteByExpirationDateBefore(dataTime);
    }

    @Transactional
    public void createResetPassword(User user, String recoverCode){
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(recoverCode);
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }

    private void deletebyId(Long id) {
        resetPasswordRepository.deleteById(id);
    }


}

package br.com.hahn.auth.application.service;


import br.com.hahn.auth.application.execption.ResourceNotFoundException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    public ResetPasswordService(ResetPasswordRepository resetPasswordRepository) {
        this.resetPasswordRepository = resetPasswordRepository;
    }

    public void saveResetPassword(ResetPassword resetPassword){
        resetPasswordRepository.save(resetPassword);
    }

    public ResetPassword findByEmail(String email){
        return resetPasswordRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No request found for this user."));
    }

}

package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ApplicationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationScheduler.class);
    private final ResetPasswordRepository resetPasswordRepository;

    public ApplicationScheduler(ResetPasswordRepository resetPasswordRepository) {
        this.resetPasswordRepository = resetPasswordRepository;
    }


    @Scheduled(cron = "0 */5 * * * *")
    public void cleanExpiredResetRecoverCodes(){
        logger.info("Stargin routine to delete expired Recover Code");
        int deleteCount = resetPasswordRepository.deleteByExpirationDateBefore(LocalDateTime.now());
        logger.info("Routine completed: Records deleted: {}", deleteCount);
    }
}

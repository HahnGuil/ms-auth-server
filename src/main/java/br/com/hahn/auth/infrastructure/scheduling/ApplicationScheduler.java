package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.application.service.ResetPasswordService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ApplicationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationScheduler.class);
    private final ResetPasswordService resetPasswordService;
    private final EmailService emailService;
    private final UserService userService;

    public ApplicationScheduler(ResetPasswordService resetPasswordService, EmailService emailService, UserService userService) {
        this.resetPasswordService = resetPasswordService;
        this.emailService = emailService;

        this.userService = userService;
    }


    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanExpiredResetRecoverCodes(){
        logger.info("Stargin routine to delete expired Recover Code");
        int deleteCount = resetPasswordService.deleteByExpirationDateBefore(LocalDateTime.now());
        logger.info("Routine completed: Records deleted: {}", deleteCount);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void alerteExpiredUser() {
        logger.info("Sending notification email to users with expiring passwords");

        int[] warningDays = {20, 15, 10, 5};
        for (int days : warningDays) {
            List<User> usersToWarn = userService.getUsersWithPasswordExpiringInDays(days);
            usersToWarn.forEach(user -> {
                String subject = "Your password will expire soon";
                String body = String.format("Hello %s, your password will expire in %d days. Please update it.", user.getFirstName(), days);
                emailService.enviarEmail(user.getEmail(), subject, body).subscribe(
                        success -> logger.info("Email sent to {}", user.getEmail()),
                        error -> logger.error("Failed to send email to {}: {}", user.getEmail(), error.getMessage())
                );
            });
        }
        logger.info("Notification routine completed");
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void blockUser(){
        userService.findUserToBlock();
    }
}

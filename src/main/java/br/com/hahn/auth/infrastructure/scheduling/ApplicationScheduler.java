package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.application.service.LoginLogService;
import br.com.hahn.auth.application.service.ResetPasswordService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class ApplicationScheduler {

    private final ResetPasswordService resetPasswordService;
    private final EmailService emailService;
    private final UserService userService;
    private final LoginLogService loginLogService;


    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanExpiredResetRecoverCodes(){
        log.info("ApllicationScheduler: Stargin routine to delete expired Recover Code");
        int deleteCount = resetPasswordService.deleteByExpirationDateBefore(LocalDateTime.now());
        log.info("ApllicationScheduler: Routine completed: Records deleted: {}", deleteCount);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void alerteExpiredUser() {
        log.info("ApplicationScheduler: Sending notification email to users with expiring passwords");

        int[] warningDays = {20, 15, 10, 5};
        for (int days : warningDays) {
            List<User> usersToWarn = userService.getUsersWithPasswordExpiringInDays(days);
            usersToWarn.forEach(user -> {
                if(user.getRole() == UserRole.USER_NORMAL){
                    String subject = "Your password will expire soon";
                    String body = String.format("Hello %s, your password will expire in %d days. Please update it.", user.getFirstName(), days);
                    emailService.sendEmail(user.getEmail(), subject, body).subscribe(
                            success -> log.info("ApllicationScheduler: Email sent to {}", user.getEmail()),
                            error -> log.error("ApllicationScheduler: Failed to send email to {}: {}", user.getEmail(), error.getMessage())
                    );
                }
            });
        }
        log.info("ApllicationScheduler: Notification routine completed");
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void blockUser(){
        log.info("ApplicationScheduler: Start block user rotine");
        userService.findUserToBlock();
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void invalidTokenSchaduler() {
        log.info("ApplicationScheduler: Starting routine to invalidate expired tokens.");
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        List<LoginLog> expiredTokens = loginLogService.findExpiredActiveTokens(expirationTime);

        for (LoginLog loginLog : expiredTokens) {
            loginLogService.deactivateActiveToken(loginLog.getUserId(), TypeInvalidation.EXPIRATION_TIME);
            log.info("ApplicationScheduler: Expired token invalidated for user.: {}", loginLog.getUserId());
        }
        log.info("ApplicationScheduler: Routine for invalidating expired tokens completed.");
    }

}

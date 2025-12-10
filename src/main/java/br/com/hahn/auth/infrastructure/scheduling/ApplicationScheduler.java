package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.application.service.TokenLogService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.SignalType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationScheduler {

    private final EmailService emailService;
    private final UserService userService;
    private final TokenLogService tokenLogService;
    private final PasswordService passwordService;


    /**
     * Cleans up expired password reset and recover codes.
     * <p>
     * This scheduled method runs periodically and performs the following steps:
     * - Logs the start of the routine with the current timestamp.
     * - Deletes all expired reset/recover codes older than the current time
     *   by invoking PasswordService#deleteByExpirationDateBefore.
     * - Logs the number of deleted records and the completion timestamp.
     * <p>
     * Executed within a transactional context to ensure database integrity.
     *
     * @author HahnGuil
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanExpiredResetRecoverCodes(){
        log.info("ApplicationScheduler: Starting routine to delete expired Recover Code at: {}", Instant.now());
        int deleteCount = passwordService.deleteByExpirationDateBefore(LocalDateTime.now());
        log.info("ApplicationScheduler: Routine completed: Records deleted: {} at: {}", deleteCount, Instant.now());
    }

    /**
     * Sends notification emails to users whose passwords are about to expire.
     * <p>
     * This scheduled method runs daily at midnight and performs the following steps:
     * - Logs the start of the notification routine with the current timestamp.
     * - Iterates through a predefined set of warning days (20, 15, 10, 5).
     * - For each warning day, retrieves the list of users whose passwords will expire in that number of days.
     * - Sends an email notification to each user with the expiration details, if the user has a normal role.
     * - Logs success or failure for each email sent.
     * - Logs the completion of the notification routine.
     * <p>
     * Reactive programming is used to handle email sending, with error handling and completion logging.
     *
     * @author HahnGuil
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void alertExpiredUser() {
        log.info("ApplicationScheduler: Sending notification email to users with expiring passwords at :{}", Instant.now());

        int[] warningDays = {20, 15, 10, 5};
        for (int days : warningDays) {
            List<User> usersToWarn = userService.getUsersWithPasswordExpiringInDays(days);
            usersToWarn.forEach(user -> {
                if(user.getRole() == UserRole.USER_NORMAL){
                    String subject = "Your password will expire soon";
                    String body = String.format("Hello %s, your password will expire in %d days. Please update it.", user.getFirstName(), days);
                    emailService.sendEmail(user.getEmail(), subject, body)
                            .doOnError(_ -> log.error("ApplicationScheduler: Failed to send email to {}: at: {}", user.getEmail(), Instant.now()))
                            .doFinally(signal -> {
                                if (signal == SignalType.ON_COMPLETE) {
                                    log.info("ApplicationScheduler: Email sent to {} at: {}", user.getEmail(), Instant.now());
                                }
                            })
                            .subscribe();
                }
            });
        }
        log.info("ApplicationScheduler: Notification routine completed at: {}", Instant.now());
    }

    /**
     * Blocks users based on specific criteria.
     * <p>
     * This scheduled method runs daily at midnight and performs the following steps:
     * - Logs the start of the user blocking routine.
     * - Invokes UserService#findUserToBlock to identify and block users who meet the criteria.
     * <p>
     * Executed within a transactional context to ensure database integrity.
     *
     * @author HahnGuil
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void blockUser(){
        log.info("ApplicationScheduler: Start block user routine at: {}", Instant.now());
        userService.findUserToBlock();
    }

    /**
     * Invalidates expired tokens.
     * <p>
     * This scheduled method runs every minute and performs the following steps:
     * - Logs the start of the token invalidation routine with the current timestamp.
     * - Calculates the expiration time as 15 minutes before the current time.
     * - Retrieves a list of expired active tokens using TokenLogService#findExpiredActiveTokens.
     * - Iterates through the expired tokens and deactivates each one by invoking
     *   TokenLogService#deactivateActiveToken with the appropriate invalidation type.
     * - Logs the invalidation of each token and the associated user ID.
     * - Logs the completion of the token invalidation routine.
     * <p>
     * Executed within a transactional context to ensure database integrity.
     *
     * @author HahnGuil
     */
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void invalidTokenScheduler() {
        log.info("ApplicationScheduler: Starting routine to invalidate expired tokens at: {}", Instant.now());
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        List<TokenLog> expiredTokens = tokenLogService.findExpiredActiveTokens(expirationTime);

        for (TokenLog tokenLog : expiredTokens) {
            tokenLogService.deactivateActiveToken(tokenLog.getUserId(), TypeInvalidation.EXPIRATION_TIME);
            log.info("ApplicationScheduler: Expired token invalidated for user.: {} at: {}", tokenLog.getUserId(), Instant.now()) ;
        }
        log.info("ApplicationScheduler: Routine for invalidating expired tokens completed at: {}", Instant.now());
    }
}

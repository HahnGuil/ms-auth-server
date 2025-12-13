package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.*;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordService {

    private final Random random = new Random();

    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordRepository resetPasswordRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenLogService tokenLogService;

    /**
     * Changes the password of a user.
     * This method performs the following steps:
     * - Logs the start of the password change process.
     * - Retrieves the user by their email.
     * - Checks if the user is an OAuth user (password change is not allowed for OAuth users).
     * - Validates the old password of the user.
     * - Updates the user's password with the new password.
     * - Disables the token used to change the password.
     *
     * @author HahnGuil
     * @param changePasswordRequest the request object containing the user's email, old password, and new password
     * @throws UserNotFoundException if the user is not found by the provided email
     * @throws UserCanNotChangePasswordException if the user is an OAuth user
     * @throws InvalidCredentialsException if the old password is invalid
     */
    public void changePassword(ChangePasswordRequest changePasswordRequest){
        log.info("PasswordService: Starting change password for user with email: {} at: {}", changePasswordRequest.getEmail(), Instant.now());

        log.info("PasswordService: Call User service to find the user by email: {} at: {}", changePasswordRequest.getEmail(), Instant.now());
        var user = userService.findByEmail(changePasswordRequest.getEmail());

        log.info("PasswordService: Check if the user: {} is OAuth at: {}", user.getUserId(), Instant.now());
        checkIfItIsAuthUser(user);

        log.info("PasswordService: Call AuthService, to validate the oldPassword for user: {}, at: {}", user.getUserId(), Instant.now());
        authService.validateOldPassword(user, changePasswordRequest.getOldPassword());

        log.info("PasswordService: Call UserService to update user password to user: {} at: {}", user.getUserId(), Instant.now());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordEncoder.encode(changePasswordRequest.getNewPassword()), LocalDateTime.now());

        log.info("PasswordService: Deactivate the token use for do the change password of user: {}, at: {}", user.getUserId(), Instant.now());
        authService.doLogOff(user.getUserId(), TypeInvalidation.CHANGE_PASSWORD);
    }

    /**
     * Handles the process of requesting a validation code for password reset.
     * This method performs the following steps:
     * - Logs the start of the validation code request process.
     * - Finds the user by their email.
     * - Checks if there is an existing reset password request for the email and deletes it if found.
     * - Generates a new recovery code for the user.
     * - Creates a reset password entry for the user with the encoded recovery code.
     * - Sends the validation code to the user's email.
     * - Returns a success response indicating the operation was completed.
     *
     * @author HahnGuil
     * @param passwordResetRequest the request object containing the user's email
     * @return SuccessResponse indicating the result of the operation
     * @throws UserNotFoundException if the user is not found by the provided email
     * @throws InvalidOperationExecption if there is a failure in sending the email
     */
    public SuccessResponse requestValidateCode(PasswordResetRequest passwordResetRequest){
        log.info("PasswordService: Starting requesting for a validate code for email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());

        log.info("PasswordService: Find user by email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());
        var user = userService.findByEmail(passwordResetRequest.getEmail());

        log.info("PasswordService: Check user: {} is OAuth at: {}", user.getUserId(), Instant.now());
        checkIfItIsAuthUser(user);

        log.info("PasswordService: Verify if already exists one change reset password for this email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());
        findAnDeleteResetPassword(passwordResetRequest);
        
        log.info("PasswordService: Creating recover code for user: {}, at: {}", user.getUserId(), Instant.now());
        var recoverCode = generateRecoverCode();
        
        log.info("PasswordService: Creating reset password for the user: {}, at: {}", user.getUserId(), Instant.now());
        createResetPassword(user, passwordEncoder.encode(recoverCode));
        
        log.info("PasswordService: Send validation code to user email: {}, at: {}", user.getEmail(), Instant.now());
        sendEmail(user.getEmail(), buildResetEmailBody(user.getFirstName() + " " +  user.getLastName(), recoverCode));

        return generateResponse();
    }

    /**
     * Validates a recovery code for resetting a user's password.
     * This method performs the following steps:
     * - Logs the start of the recovery code validation process.
     * - Retrieves the reset password entry associated with the user's email.
     * - Validates the recovery code and checks if it has expired.
     * - Creates a token log for the recovery request.
     * - Generates and returns a recovery token.
     *
     * @author HahnGuil
     * @param validateCodeRequest the request object containing the user's email and recovery code
     * @return ValidateCodeResponse containing the generated recovery token
     * @throws NotFoundResetPasswordRequestForUser if no reset password entry is found for the user's email
     * @throws InvalidRecoverCodeException if the recovery code is invalid or expired
     */
    public ValidateCodeResponse validateResetCode(ValidateCodeRequest validateCodeRequest){
        log.info("PasswordService: Starting validating recover code for user with email: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());

        log.info("PasswordService: Find recover code for user email: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());
        var resetPassword = findResetPasswordByEmail(validateCodeRequest.getEmail());

        log.info("PasswordService: Validate if the recovery code of user email: {}, is not expired, at: {}", validateCodeRequest.getEmail(), Instant.now());
        validateRecoverCodeValues(resetPassword, validateCodeRequest);

        log.info("PasswordService: Create TokenLog for request of user: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());
        var tokenLog = tokenLogService.saveTokenLog(userService.findByEmail(resetPassword.getUserEmail()), ScopeToken.RECOVER_CODE, LocalDateTime.now());

        log.info("PasswordService: Generate Recover token for user: {}, at: {}", resetPassword.getUserEmail(), Instant.now());
        return generateRecoveryToken(tokenService.generateRecoverToken(resetPassword, tokenLog));
    }

    /**
     * Resets the password of a user based on the provided JWT and new password request.
     * This method performs the following steps:
     * - Logs the start of the password reset process.
     * - Validates the scope of the recovery token from the JWT.
     * - Extracts the user ID and email from the JWT.
     * - Updates the user's password using the UserService.
     * - Deactivates the received token so it cannot be used again.
     *
     * @author HahnGuil
     * @param jwt the JWT containing the user's information and recovery token scope
     * @param newPasswordRequest the request object containing the new password
     * @throws InvalidRecoverTokenException if the recovery token scope is invalid
     */
    public void resetUserPassword(Jwt jwt, NewPasswordRequest newPasswordRequest){
        log.info("PasswordService: Starting reset password for user with email: {}, at: {}", jwt.getSubject(), Instant.now());
        validateScopeFromRecoveryToken(jwt.getClaim("scope"));

        log.info("PasswordService: Extract user id and email from jwt token at: {}", Instant.now());
        var userId = UUID.fromString(jwt.getClaim("user_id"));
        var userEmail = jwt.getSubject();

        log.info("PasswordService: User id: {}, and user email: {}, were successfully extracted at: {}", userId, userEmail, Instant.now());

        log.info("PasswordService: Call UserService for reset password from User: {} at: {}", userId, Instant.now());
        userService.updatePassword(userEmail, userId, passwordEncoder.encode(newPasswordRequest.getNewPassword()), LocalDateTime.now());

        log.info("PasswordService: Deactivated recover token for user: {}, at: {}", userId, Instant.now());
        tokenLogService.deactivateActiveToken(userId, TypeInvalidation.RESET_PASSWORD);
    }

    /**
     * Deletes reset password entries that have an expiration date before the specified date and time.
     * This method performs the following steps:
     * - Logs the start of the deletion process.
     * - Deletes all reset password entries from the repository with an expiration date earlier than the provided date.
     *
     * @author HahnGuil
     * @param dataTime the cutoff date and time; entries with an expiration date before this will be deleted
     * @return the number of entries deleted
     */
    public int deleteByExpirationDateBefore(LocalDateTime dataTime){
        log.info("ResetPasswordService: Delete reset password by expiration date");
        return resetPasswordRepository.deleteByExpirationDateBefore(dataTime);
    }

    /**
     * Validates the token for changing a user's password.
     * <p>
     * This method performs the following steps:
     * - Extracts the token log ID from the JWT.
     * - Finds the token log entry by its ID.
     * - Validates if the token log has the expected scope.
     * - Checks if the token log is valid.
     * </p>
     *
     * @author HahnGuil
     * @param jwt the JWT containing the token log ID and user information
     * @throws InvalidTokenException if the token log is invalid or has an unexpected scope
     */
    public void validateTokenForChangePassword(Jwt jwt){
        log.info("PasswordServe: Extract tokens id and user and for jwt at: {}", Instant.now());
        var tokensId = UUID.fromString(jwt.getClaim("token_log_id"));

        log.info("PasswordService: Find token by id: {} at: {}", tokensId, Instant.now());
        var token = tokenLogService.findById(tokensId);

        tokenLogService.isExpectedScopeToken(token);
        tokenLogService.isTokenLogValid(token);
    }

    /**
     * Validates the scope of the recovery token.
     * This method checks if the provided scope token matches the expected value for a recovery token.
     * If the scope token is invalid, an InvalidRecoverTokenException is thrown.
     *
     * @author HahnGuil
     * @param scopeToken the scope token to be validated
     * @throws InvalidRecoverTokenException if the scope token is not valid for password reset
     */
    private void validateScopeFromRecoveryToken(String scopeToken){
        if(!ScopeToken.RECOVER_CODE.getValue().equals(scopeToken)){
            log.info("PasswordService: Scope Token is not valid for reset password. Throw InvalidRecoverTokenException at: {}", Instant.now());
            throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_RECOVERY_CODE.getMessage());
        }
    }

    /**
     * Generates a recovery token response.
     * This method creates a new instance of ValidateCodeResponse,
     * sets the provided recovery token, and returns the response object.
     *
     * @author HahnGuil
     * @param recoveryToken the recovery token to be included in the response
     * @return ValidateCodeResponse containing the provided recovery token
     */
    private ValidateCodeResponse generateRecoveryToken(String recoveryToken){
        ValidateCodeResponse validateCodeResponse = new ValidateCodeResponse();
        validateCodeResponse.setRecoverToken(recoveryToken);
        return validateCodeResponse;
    }

    /**
     * Validates the recovery code values for a password reset request.
     * This method performs the following steps:
     * - Logs the start of the recovery code validation process.
     * - Checks if the provided recovery code matches the stored encoded recovery code.
     * - Throws an exception if the recovery code does not match.
     * - Validates the expiration time of the recovery code.
     *
     * @author HahnGuil
     * @param resetPassword the reset password entry containing the stored recovery code
     * @param validateCodeRequest the request object containing the user's email and recovery code
     * @throws InvalidRecoverCodeException if the recovery code does not match or is invalid
     */
    private void validateRecoverCodeValues(ResetPassword resetPassword, ValidateCodeRequest validateCodeRequest){
        log.info("PasswordService: Validate if the recover code of request of the user: {}, matches, at: {}", validateCodeRequest.getEmail(), Instant.now());
        if(!passwordEncoder.matches(validateCodeRequest.getRecoveryCode() , resetPassword.getRecoverCode())){
            log.error("PasswordService: Recovery code of user: {}, not Match. Throw InvalidRecoverCodeException at: {}", validateCodeRequest.getEmail(), Instant.now());
            throw new InvalidRecoverCodeException(ErrorsResponses.INVALID_RECOVERY_CODE.getMessage());
        }
        validateRecoverCodeExpirationTime(resetPassword);
    }

    /**
     * Validates the expiration time of a recovery code.
     * This method checks if the expiration date of the recovery code has passed.
     * If the recovery code is expired, an InvalidRecoverCodeException is thrown.
     *
     * @author HahnGuil
     * @param resetPassword the reset password entry containing the recovery code and its expiration date
     * @throws InvalidRecoverCodeException if the recovery code is expired
     */
    private void validateRecoverCodeExpirationTime(ResetPassword resetPassword){
        log.info("PasswordService: Validate recover code expiration time, for user email: {}, at: {}", resetPassword.getUserEmail() , Instant.now());
        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            log.error("PasswordService: Recovery Code of user {}, is expired. Throw InvalidRecoverCodeException at: {}", resetPassword.getUserEmail(), Instant.now());
            throw new InvalidRecoverCodeException(ErrorsResponses.EXPIRED_RECOVERY_CODE.getMessage());
        }
    }

    /**
     * Creates a reset password entry for a user.
     * This method performs the following steps:
     * - Logs the creation of the reset password entry.
     * - Initializes a new ResetPassword object.
     * - Sets the recovery code, user email, and expiration date for the reset password entry.
     * - Saves the reset password entry to the repository.
     *
     * @author HahnGuil
     * @param user the user for whom the reset password entry is being created
     * @param recoverCode the recovery code to be associated with the reset password entry
     */
    private void createResetPassword(User user, String recoverCode){
        log.info("ResetPasswordService: Create reset password for user: {}, at: {}", user.getUserId(), Instant.now());
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(recoverCode);
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }

    /**
     * Checks if the user is an OAuth user.
     * This method verifies if the user's type is OAUTH_USER.
     * If the user is an OAuth user, a UserCanNotChangePasswordException is thrown.
     *
     * @author HahnGuil
     * @param user the user whose type is being checked
     * @throws UserCanNotChangePasswordException if the user is an OAuth user
     */
    private void checkIfItIsAuthUser(User user){
        if (user.getTypeUser().equals(TypeUser.OAUTH_USER)){
            log.error("PasswordService: User: {}, is OUATH_USER: {}. Throw the UserCanNotChangePasswordException at: {}", user.getUserId(), user.getTypeUser(), Instant.now());
            throw new UserCanNotChangePasswordException(ErrorsResponses.CHANGE_PASSWORD_NOT_ALLOWED_FOR_OAUTH_USER.getMessage());
        }
    }

    /**
     * Deletes an existing reset password entry for a user, if it exists.
     * This method performs the following steps:
     * - Checks if a reset password entry exists for the provided email.
     * - If found, retrieves the reset password entry and deletes it by its ID.
     *
     * @author HahnGuil
     * @param passwordResetRequest the request object containing the user's email
     */
    private void findAnDeleteResetPassword(PasswordResetRequest passwordResetRequest){
        if(resetPasswordRepository.existsByUserEmail(passwordResetRequest.getEmail())){
            var resetPassword = findResetPasswordByEmail(passwordResetRequest.getEmail());
            resetPasswordRepository.deleteById(resetPassword.getId());
        }
    }

    /**
     * Generates a recovery code for password reset.
     * This method creates a random 6-digit number and returns it as a string.
     *
     * @author HahnGuil
     * @return a string representation of the generated 6-digit recovery code
     */
    private String generateRecoverCode() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * Generates a success response for the recovery code email operation.
     * This method performs the following steps:
     * - Logs the creation of the success response.
     * - Initializes a new SuccessResponse object.
     * - Sets the message indicating the recovery code was sent to the email.
     * - Returns the constructed SuccessResponse object.
     *
     * @author HahnGuil
     * @return SuccessResponse containing the success message
     */
    private SuccessResponse generateResponse(){
        log.info("PasswordService: Build success response at: {}", Instant.now());
        SuccessResponse response = new SuccessResponse();
        response.setMessage(SuccessResponses.SEND_RECOVERY_CODE_TO_EMAIL.getMessage());
        return response;
    }

    /**
     * Sends an email with a validation code for password reset.
     * This method performs the following steps:
     * - Logs the start of the email sending process.
     * - Attempts to send the email using the EmailService.
     * - If an exception occurs, logs the failure and throws an InvalidOperationExecption.
     *
     * @author HahnGuil
     * @param email the recipient's email address
     * @param htmlBody the HTML content of the email
     * @throws InvalidOperationExecption if the email fails to send
     */
    private void sendEmail(String email, String htmlBody) {
        log.info("PasswordService: Send email with validate code for user email: {}, at: {}", email, Instant.now());
        try {
            emailService.sendEmail(email, "Password Reset Request", htmlBody).block();
        } catch (Exception _) {
            log.error("PasswordService: Fail to send email to user email: {}. Throw InvalidOperationException at: {}", email, Instant.now());
            log.error("AuthService: Failed to send email");
            throw new InvalidOperationExecption(ErrorsResponses.FAIL_TO_SEND_EMAIL.getMessage());
        }
    }

    /**
     * Finds the reset password entry for a user by their email.
     * This method performs the following steps:
     * - Logs the start of the search process.
     * - Attempts to retrieve the reset password entry associated with the provided email.
     * - If no entry is found, logs the error and throws a NotFoundResetPasswordRequestForUser exception.
     *
     * @author HahnGuil
     * @param email the email address of the user
     * @return the ResetPassword object associated with the user's email
     * @throws NotFoundResetPasswordRequestForUser if no reset password entry is found for the provided email
     */
    private ResetPassword findResetPasswordByEmail(String email){
        log.info("PasswordService: Find resetPassword for user email: {}, at: {}", email, Instant.now());
        return resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> {
            log.error("PasswordService: Not found validation code to reset password for the user with email: {}. Throw NotFoundResetPasswordRequestForUser at: {}", email, Instant.now());
            return new NotFoundResetPasswordRequestForUser("There is no registered user for this email address. Check email and password or register a new user.");
        });
    }

    /**
     * Builds the HTML body for the password reset email.
     * This method formats the email content with the user's name and recovery code.
     * The email includes a greeting, the recovery code, and an expiration notice.
     *
     * @author HahnGuil
     * @param username the name of the user to include in the email greeting
     * @param recoverCode the recovery code to include in the email body
     * @return the formatted HTML string for the email body
     */
    private String buildResetEmailBody(String username, String recoverCode) {
        log.error("PasswordService: Build email body to user name: {}, at: {}", username, Instant.now());
        return String.format("<p>Hello %s,</p><p>Your password reset code is: <strong>%s</strong></p><p>This code will expire in 30 minutes.</p>", username, recoverCode);
    }
}
package br.com.hahn.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public enum ErrorsResponses {

    EMAIL_ALREADY_REGISTER_ERROR("Email already registered. Please log in or recover your password."),
    INVALID_CREDENTIALS("Invalid email or password"),
    USER_NOT_FOUD("User not found. Check email and password or register a new user."),
    USER_ALREADY_LOGGED("The user is already logged in at this time."),
    USER_BLOCK("The user is locked out. Use the link to reset your password to log in again."),
    INVALID_RECOVERY_CODE("The validation code provided is not valid."),
    EXPIRED_RECOVERY_CODE("The validation code provided is expired"),
    CHANGE_PASSWORD_NOT_ALLOWED_FOR_OAUTH_USER("Password changes are not allowed for users registered via Gmail."),
    USER_OAUTH_CAN_NOT_LOGIN_DIRECT("The user was registered via Gmail; login via this request is not permitted. Please use the Gmail login link."),
    FAIL_TO_SEND_EMAIL("Failed to send email. Please try again later."),
    NOT_VALID_SCOPE_TOKEN("The token passed in the request is not valid for this type of operation or is not inform."),
    EXPIRED_REFRESH_TOKEN("Refresh Token expired, please log in again."),
    KEY_ROTATION_ERROR("Failed to rotate keys"),
    GENERATE_RECOVER_TOKEN_ERROR("Error while creating RecoverToken"),
    GENERATE_TOKEN_ERROR("Error while creating Token"),
    GENERATE_REFRESH_TOKEN_ERROR("Error while creating Refresh"),
    PRIVATE_KEY_NOT_AVAILABLE("Private key not available"),
    ID_PRIVATE_KEY_NOT_AVAILABLE("Private key ID not available"),
    INVALID_TOKEN("Invalid token, please log in to continue."),
    INVALID_EMAIL_FORMAT_TYPE("This email is in an unauthorized format."),
    INVALID_PASSWORD_FORMAT_TYPE("Invalid password. The password must contain 8 to 12 characters, including numbers, special characters, and uppercase letters."),
    INVALID_FORMAT_ON_REQUEST("One of the values passed in the request is in the wrong format. Review the values and try again");

    private final String message;

}

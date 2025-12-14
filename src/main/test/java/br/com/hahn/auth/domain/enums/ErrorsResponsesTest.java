package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorsResponsesTest {

    @Test
    void shouldHaveCorrectMessageForEmailAlreadyRegisterError() {
        ErrorsResponses error = ErrorsResponses.EMAIL_ALREADY_REGISTER_ERROR;

        assertEquals("Email already registered. Please log in or recover your password.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidCredentials() {
        ErrorsResponses error = ErrorsResponses.INVALID_CREDENTIALS;

        assertEquals("Invalid email or password", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForUserNotFound() {
        ErrorsResponses error = ErrorsResponses.USER_NOT_FOUD;

        assertEquals("User not found. Check email and password or register a new user.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForUserAlreadyLogged() {
        ErrorsResponses error = ErrorsResponses.USER_ALREADY_LOGGED;

        assertEquals("The user is already logged in at this time.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForUserBlock() {
        ErrorsResponses error = ErrorsResponses.USER_BLOCK;

        assertEquals("The user is locked out. Use the link to reset your password to log in again.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidRecoveryCode() {
        ErrorsResponses error = ErrorsResponses.INVALID_RECOVERY_CODE;

        assertEquals("The validation code provided is not valid.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForExpiredRecoveryCode() {
        ErrorsResponses error = ErrorsResponses.EXPIRED_RECOVERY_CODE;

        assertEquals("The validation code provided is expired", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForChangePasswordNotAllowedForOAuthUser() {
        ErrorsResponses error = ErrorsResponses.CHANGE_PASSWORD_NOT_ALLOWED_FOR_OAUTH_USER;

        assertEquals("Password changes are not allowed for users registered via Gmail.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForUserOAuthCanNotLoginDirect() {
        ErrorsResponses error = ErrorsResponses.USER_OAUTH_CAN_NOT_LOGIN_DIRECT;

        assertEquals("The user was registered via Gmail; login via this request is not permitted. Please use the Gmail login link.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForFailToSendEmail() {
        ErrorsResponses error = ErrorsResponses.FAIL_TO_SEND_EMAIL;

        assertEquals("Failed to send email. Please try again later.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForNotValidScopeToken() {
        ErrorsResponses error = ErrorsResponses.NOT_VALID_SCOPE_TOKEN;

        assertEquals("The token passed in the request is not valid for this type of operation or is not inform.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForExpiredRefreshToken() {
        ErrorsResponses error = ErrorsResponses.EXPIRED_REFRESH_TOKEN;

        assertEquals("Refresh Token expired, please log in again.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForKeyRotationError() {
        ErrorsResponses error = ErrorsResponses.KEY_ROTATION_ERROR;

        assertEquals("Failed to rotate keys", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForGenerateRecoverTokenError() {
        ErrorsResponses error = ErrorsResponses.GENERATE_RECOVER_TOKEN_ERROR;

        assertEquals("Error while creating RecoverToken", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForGenerateTokenError() {
        ErrorsResponses error = ErrorsResponses.GENERATE_TOKEN_ERROR;

        assertEquals("Error while creating Token", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForGenerateRefreshTokenError() {
        ErrorsResponses error = ErrorsResponses.GENERATE_REFRESH_TOKEN_ERROR;

        assertEquals("Error while creating Refresh", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForPrivateKeyNotAvailable() {
        ErrorsResponses error = ErrorsResponses.PRIVATE_KEY_NOT_AVAILABLE;

        assertEquals("Private key not available", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForIdPrivateKeyNotAvailable() {
        ErrorsResponses error = ErrorsResponses.ID_PRIVATE_KEY_NOT_AVAILABLE;

        assertEquals("Private key ID not available", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidToken() {
        ErrorsResponses error = ErrorsResponses.INVALID_TOKEN;

        assertEquals("Invalid token, please log in to continue.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForTokenMustBeRefresh() {
        ErrorsResponses error = ErrorsResponses.TOKEN_MUST_BE_REFRESH;

        assertEquals("To request a new token, the token provided must be REFRESH_TOKEN. The token provided is :", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidEmailFormatType() {
        ErrorsResponses error = ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE;

        assertEquals("This email is in a invalid format.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidPasswordFormatType() {
        ErrorsResponses error = ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE;

        assertEquals("Invalid password format. The password must contain 8 to 12 characters, including numbers, special characters, and uppercase and lowercase letters.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForInvalidFormatOnRequest() {
        ErrorsResponses error = ErrorsResponses.INVALID_FORMAT_ON_REQUEST;

        assertEquals("Invalid email or password format. Email must be in the format email@email.com, and password must be 8 to 12 characters long, including numbers, special characters, uppercase and lowercase letters.", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForFailConvertToken() {
        ErrorsResponses error = ErrorsResponses.FAIL_CONVERT_TOKEN;

        assertEquals("Token sent, but not recognized by the server. Check the Spring Security configuration (oauth2ResourceServer().jwt()).", error.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForScopeTokenInvalid() {
        ErrorsResponses error = ErrorsResponses.SCOPE_TOKEN_INVALID;

        assertEquals("It is not permitted to change the password using a token of the following type: ", error.getMessage());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        ErrorsResponses error = ErrorsResponses.valueOf("INVALID_CREDENTIALS");

        assertEquals(ErrorsResponses.INVALID_CREDENTIALS, error);
    }

    @Test
    void shouldReturnAllEnumValues() {
        ErrorsResponses[] allErrors = ErrorsResponses.values();

        assertEquals(25, allErrors.length);
        assertNotNull(allErrors);
    }

    @Test
    void shouldHaveCorrectToStringRepresentation() {
        ErrorsResponses error = ErrorsResponses.INVALID_CREDENTIALS;

        String toString = error.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("INVALID_CREDENTIALS"));
        assertTrue(toString.contains("Invalid email or password"));
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            ErrorsResponses.valueOf("INVALID_ENUM_VALUE");
        });
    }
}


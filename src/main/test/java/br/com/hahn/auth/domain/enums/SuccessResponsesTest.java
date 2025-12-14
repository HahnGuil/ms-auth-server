package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuccessResponsesTest {

    @Test
    void shouldHaveCorrectMessageForSendRecoveryCodeToEmail() {
        SuccessResponses success = SuccessResponses.SEND_RECOVERY_CODE_TO_EMAIL;

        assertEquals("A validation code has been sent to the email address you provided. Please also check your spam folder. Expect an email from noreply@toxicbet.com.br.", success.getMessage());
    }

    @Test
    void shouldHaveCorrectMessageForPasswordSuccessfullyChange() {
        SuccessResponses success = SuccessResponses.PASSWORD_SUCCESSFULLY_CHANGE;

        assertEquals("Password changed successfully.", success.getMessage());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        SuccessResponses success = SuccessResponses.valueOf("SEND_RECOVERY_CODE_TO_EMAIL");

        assertEquals(SuccessResponses.SEND_RECOVERY_CODE_TO_EMAIL, success);
    }

    @Test
    void shouldReturnAllEnumValues() {
        SuccessResponses[] allSuccessResponses = SuccessResponses.values();

        assertEquals(2, allSuccessResponses.length);
        assertNotNull(allSuccessResponses);
    }

    @Test
    void shouldHaveCorrectToStringRepresentation() {
        SuccessResponses success = SuccessResponses.PASSWORD_SUCCESSFULLY_CHANGE;

        String toString = success.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("PASSWORD_SUCCESSFULLY_CHANGE"));
        assertTrue(toString.contains("Password changed successfully."));
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> SuccessResponses.valueOf("INVALID_SUCCESS_MESSAGE"));
    }

    @Test
    void shouldVerifyAllMessagesAreNotNull() {
        for (SuccessResponses success : SuccessResponses.values()) {
            assertNotNull(success.getMessage());
            assertFalse(success.getMessage().isEmpty());
        }
    }

    @Test
    void shouldVerifyMessagesAreUnique() {
        SuccessResponses[] allSuccessResponses = SuccessResponses.values();

        long uniqueMessagesCount = java.util.Arrays.stream(allSuccessResponses)
                .map(SuccessResponses::getMessage)
                .distinct()
                .count();

        assertEquals(allSuccessResponses.length, uniqueMessagesCount);
    }
}


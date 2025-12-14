package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScopeTokenTest {

    @Test
    void shouldHaveCorrectCodeAndValueForLoginToken() {
        ScopeToken scopeToken = ScopeToken.LOGIN_TOKEN;

        assertEquals(1, scopeToken.getCode());
        assertEquals("login_token", scopeToken.getValue());
    }

    @Test
    void shouldHaveCorrectCodeAndValueForRegisterToken() {
        ScopeToken scopeToken = ScopeToken.REGISTER_TOKEN;

        assertEquals(2, scopeToken.getCode());
        assertEquals("register_token", scopeToken.getValue());
    }

    @Test
    void shouldHaveCorrectCodeAndValueForRecoverCode() {
        ScopeToken scopeToken = ScopeToken.RECOVER_CODE;

        assertEquals(3, scopeToken.getCode());
        assertEquals("recoverCode", scopeToken.getValue());
    }

    @Test
    void shouldHaveCorrectCodeAndValueForRefreshToken() {
        ScopeToken scopeToken = ScopeToken.REFRESH_TOKEN;

        assertEquals(4, scopeToken.getCode());
        assertEquals("refresh_token", scopeToken.getValue());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        ScopeToken scopeToken = ScopeToken.valueOf("LOGIN_TOKEN");

        assertEquals(ScopeToken.LOGIN_TOKEN, scopeToken);
    }

    @Test
    void shouldReturnAllEnumValues() {
        ScopeToken[] allScopeTokens = ScopeToken.values();

        assertEquals(4, allScopeTokens.length);
        assertNotNull(allScopeTokens);
    }

    @Test
    void shouldHaveUniqueCodesForEachEnum() {
        ScopeToken[] allScopeTokens = ScopeToken.values();

        assertEquals(1, ScopeToken.LOGIN_TOKEN.getCode());
        assertEquals(2, ScopeToken.REGISTER_TOKEN.getCode());
        assertEquals(3, ScopeToken.RECOVER_CODE.getCode());
        assertEquals(4, ScopeToken.REFRESH_TOKEN.getCode());

        // Verify all codes are unique
        long uniqueCodesCount = java.util.Arrays.stream(allScopeTokens)
                .map(ScopeToken::getCode)
                .distinct()
                .count();

        assertEquals(4, uniqueCodesCount);
    }

    @Test
    void shouldHaveUniqueValuesForEachEnum() {
        ScopeToken[] allScopeTokens = ScopeToken.values();

        // Verify all values are unique
        long uniqueValuesCount = java.util.Arrays.stream(allScopeTokens)
                .map(ScopeToken::getValue)
                .distinct()
                .count();

        assertEquals(4, uniqueValuesCount);
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            ScopeToken.valueOf("INVALID_SCOPE_TOKEN");
        });
    }

    @Test
    void shouldReturnCorrectValueForEachScope() {
        assertEquals("login_token", ScopeToken.LOGIN_TOKEN.getValue());
        assertEquals("register_token", ScopeToken.REGISTER_TOKEN.getValue());
        assertEquals("recoverCode", ScopeToken.RECOVER_CODE.getValue());
        assertEquals("refresh_token", ScopeToken.REFRESH_TOKEN.getValue());
    }
}


package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeInvalidationTest {

    @Test
    void shouldContainExpirationTimeValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.EXPIRATION_TIME;

        assertNotNull(typeInvalidation);
        assertEquals("EXPIRATION_TIME", typeInvalidation.name());
    }

    @Test
    void shouldContainUserRefreshValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.USER_REFRESH;

        assertNotNull(typeInvalidation);
        assertEquals("USER_REFRESH", typeInvalidation.name());
    }

    @Test
    void shouldContainLogOffValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.LOG_OFF;

        assertNotNull(typeInvalidation);
        assertEquals("LOG_OFF", typeInvalidation.name());
    }

    @Test
    void shouldContainNewLoginValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.NEW_LOGIN;

        assertNotNull(typeInvalidation);
        assertEquals("NEW_LOGIN", typeInvalidation.name());
    }

    @Test
    void shouldContainChangePasswordValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.CHANGE_PASSWORD;

        assertNotNull(typeInvalidation);
        assertEquals("CHANGE_PASSWORD", typeInvalidation.name());
    }

    @Test
    void shouldContainResetPasswordValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.RESET_PASSWORD;

        assertNotNull(typeInvalidation);
        assertEquals("RESET_PASSWORD", typeInvalidation.name());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        TypeInvalidation typeInvalidation = TypeInvalidation.valueOf("EXPIRATION_TIME");

        assertEquals(TypeInvalidation.EXPIRATION_TIME, typeInvalidation);
    }

    @Test
    void shouldReturnAllEnumValues() {
        TypeInvalidation[] allTypeInvalidations = TypeInvalidation.values();

        assertEquals(6, allTypeInvalidations.length);
        assertNotNull(allTypeInvalidations);
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> TypeInvalidation.valueOf("INVALID_TYPE"));
    }

    @Test
    void shouldVerifyAllEnumValuesAreUnique() {
        TypeInvalidation[] allTypeInvalidations = TypeInvalidation.values();

        long uniqueValuesCount = java.util.Arrays.stream(allTypeInvalidations)
                .map(Enum::name)
                .distinct()
                .count();

        assertEquals(6, uniqueValuesCount);
    }

    @Test
    void shouldVerifyEnumOrder() {
        TypeInvalidation[] allTypeInvalidations = TypeInvalidation.values();

        assertEquals(TypeInvalidation.EXPIRATION_TIME, allTypeInvalidations[0]);
        assertEquals(TypeInvalidation.USER_REFRESH, allTypeInvalidations[1]);
        assertEquals(TypeInvalidation.LOG_OFF, allTypeInvalidations[2]);
        assertEquals(TypeInvalidation.NEW_LOGIN, allTypeInvalidations[3]);
        assertEquals(TypeInvalidation.CHANGE_PASSWORD, allTypeInvalidations[4]);
        assertEquals(TypeInvalidation.RESET_PASSWORD, allTypeInvalidations[5]);
    }

    @Test
    void shouldTestEnumEquality() {
        TypeInvalidation type1 = TypeInvalidation.LOG_OFF;
        TypeInvalidation type2 = TypeInvalidation.LOG_OFF;
        TypeInvalidation type3 = TypeInvalidation.NEW_LOGIN;

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
    }

    @Test
    void shouldTestEnumOrdinal() {
        assertEquals(0, TypeInvalidation.EXPIRATION_TIME.ordinal());
        assertEquals(1, TypeInvalidation.USER_REFRESH.ordinal());
        assertEquals(2, TypeInvalidation.LOG_OFF.ordinal());
        assertEquals(3, TypeInvalidation.NEW_LOGIN.ordinal());
        assertEquals(4, TypeInvalidation.CHANGE_PASSWORD.ordinal());
        assertEquals(5, TypeInvalidation.RESET_PASSWORD.ordinal());
    }
}


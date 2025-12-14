package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeUserTest {

    @Test
    void shouldContainOAuthUserValue() {
        TypeUser typeUser = TypeUser.OAUTH_USER;

        assertNotNull(typeUser);
        assertEquals("OAUTH_USER", typeUser.name());
    }

    @Test
    void shouldContainDirectUserValue() {
        TypeUser typeUser = TypeUser.DIRECT_USER;

        assertNotNull(typeUser);
        assertEquals("DIRECT_USER", typeUser.name());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        TypeUser typeUser = TypeUser.valueOf("OAUTH_USER");

        assertEquals(TypeUser.OAUTH_USER, typeUser);
    }

    @Test
    void shouldReturnAllEnumValues() {
        TypeUser[] allTypeUsers = TypeUser.values();

        assertEquals(2, allTypeUsers.length);
        assertNotNull(allTypeUsers);
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            TypeUser.valueOf("INVALID_USER_TYPE");
        });
    }

    @Test
    void shouldVerifyAllEnumValuesAreUnique() {
        TypeUser[] allTypeUsers = TypeUser.values();

        long uniqueValuesCount = java.util.Arrays.stream(allTypeUsers)
                .map(Enum::name)
                .distinct()
                .count();

        assertEquals(2, uniqueValuesCount);
    }

    @Test
    void shouldVerifyEnumOrder() {
        TypeUser[] allTypeUsers = TypeUser.values();

        assertEquals(TypeUser.OAUTH_USER, allTypeUsers[0]);
        assertEquals(TypeUser.DIRECT_USER, allTypeUsers[1]);
    }

    @Test
    void shouldTestEnumEquality() {
        TypeUser type1 = TypeUser.OAUTH_USER;
        TypeUser type2 = TypeUser.OAUTH_USER;
        TypeUser type3 = TypeUser.DIRECT_USER;

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
    }

    @Test
    void shouldTestEnumOrdinal() {
        assertEquals(0, TypeUser.OAUTH_USER.ordinal());
        assertEquals(1, TypeUser.DIRECT_USER.ordinal());
    }

    @Test
    void shouldDifferentiateBetweenUserTypes() {
        TypeUser oauthUser = TypeUser.OAUTH_USER;
        TypeUser directUser = TypeUser.DIRECT_USER;

        assertNotEquals(oauthUser, directUser);
        assertNotEquals(oauthUser.name(), directUser.name());
    }
}


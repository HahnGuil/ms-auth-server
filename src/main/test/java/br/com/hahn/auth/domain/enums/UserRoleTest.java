package br.com.hahn.auth.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    @Test
    void shouldContainUserAdminValue() {
        UserRole userRole = UserRole.USER_ADMIN;

        assertNotNull(userRole);
        assertEquals("USER_ADMIN", userRole.name());
    }

    @Test
    void shouldContainUserNormalValue() {
        UserRole userRole = UserRole.USER_NORMAL;

        assertNotNull(userRole);
        assertEquals("USER_NORMAL", userRole.name());
    }

    @Test
    void shouldReturnCorrectEnumValue() {
        UserRole userRole = UserRole.valueOf("USER_ADMIN");

        assertEquals(UserRole.USER_ADMIN, userRole);
    }

    @Test
    void shouldReturnAllEnumValues() {
        UserRole[] allUserRoles = UserRole.values();

        assertEquals(2, allUserRoles.length);
        assertNotNull(allUserRoles);
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            UserRole.valueOf("INVALID_ROLE");
        });
    }

    @Test
    void shouldVerifyAllEnumValuesAreUnique() {
        UserRole[] allUserRoles = UserRole.values();

        long uniqueValuesCount = java.util.Arrays.stream(allUserRoles)
                .map(Enum::name)
                .distinct()
                .count();

        assertEquals(2, uniqueValuesCount);
    }

    @Test
    void shouldVerifyEnumOrder() {
        UserRole[] allUserRoles = UserRole.values();

        assertEquals(UserRole.USER_ADMIN, allUserRoles[0]);
        assertEquals(UserRole.USER_NORMAL, allUserRoles[1]);
    }

    @Test
    void shouldTestEnumEquality() {
        UserRole role1 = UserRole.USER_ADMIN;
        UserRole role2 = UserRole.USER_ADMIN;
        UserRole role3 = UserRole.USER_NORMAL;

        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
    }

    @Test
    void shouldTestEnumOrdinal() {
        assertEquals(0, UserRole.USER_ADMIN.ordinal());
        assertEquals(1, UserRole.USER_NORMAL.ordinal());
    }

    @Test
    void shouldDifferentiateBetweenRoles() {
        UserRole adminRole = UserRole.USER_ADMIN;
        UserRole normalRole = UserRole.USER_NORMAL;

        assertNotEquals(adminRole, normalRole);
        assertNotEquals(adminRole.name(), normalRole.name());
    }
}


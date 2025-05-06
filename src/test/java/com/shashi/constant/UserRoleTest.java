package com.shashi.constant;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Epic("User Role Management")  // High-level category
@Feature("User Role Enum Tests")  // Specific feature under test
class UserRoleTest {

    @Test
    @Story("Initialization of Enum Values")
    @DisplayName("Verify UserRole Enum Initialization")
    @Description("Ensures that all expected UserRole enum values are initialized properly.")
    @Severity(SeverityLevel.CRITICAL)
    void testEnumValuesInitialization() {
        assertNotNull(UserRole.ADMIN, "ADMIN role should not be null");
        assertNotNull(UserRole.CUSTOMER, "CUSTOMER role should not be null");
    }

    @Test
    @Story("Enum Functionality")
    @DisplayName("Check UserRole Enum Values and Retrieval")
    @Description("Verifies the correct number of UserRole values and ensures retrieval by name works as expected.")
    @Severity(SeverityLevel.NORMAL)
    void testEnumValues() {
        UserRole[] roles = UserRole.values();
        assertEquals(2, roles.length, "There should be exactly 2 user roles");
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"), "ADMIN role retrieval should work");
        assertEquals(UserRole.CUSTOMER, UserRole.valueOf("CUSTOMER"), "CUSTOMER role retrieval should work");
    }
}


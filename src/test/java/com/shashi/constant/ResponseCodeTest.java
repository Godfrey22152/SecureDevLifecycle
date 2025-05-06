package com.shashi.constant;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

@Epic("Response Code Handling")
@Feature("Enum-based Response Code Management")
@Story("Ensure ResponseCode Enum values behave correctly")
class ResponseCodeTest {

    @Test
    @DisplayName("Test Enumeration Values Initialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the ResponseCode enum values are properly initialized.")
    void testEnumValuesInitialization() {
        assertNotNull(ResponseCode.SUCCESS, "SUCCESS should be initialized");
        assertNotNull(ResponseCode.FAILURE, "FAILURE should be initialized");
    }

    @Test
    @DisplayName("Verify getCode() and getMessage() Return Correct Values")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that the getCode() and getMessage() methods return the correct values.")
    void testGetCodeAndMessage() {
        assertEquals(200, ResponseCode.SUCCESS.getCode(), "Expected code for SUCCESS is 200");
        assertEquals("OK", ResponseCode.SUCCESS.getMessage(), "Expected message for SUCCESS is 'OK'");
    }

    @Test
    @DisplayName("Check getMessageByStatusCode() Returns Expected ResponseCode")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure ResponseCode.getMessageByStatusCode() returns the expected ResponseCode.")
    @Link(name = "Documentation", url = "https://example.com/api-docs#response-codes")
    @Issue("API-123")
    @TmsLink("TMS-456")
    void testGetMessageByStatusCode() {
        assertEquals(Optional.of(ResponseCode.SUCCESS), ResponseCode.getMessageByStatusCode(200), 
                     "Expected SUCCESS for status code 200");
        assertEquals(Optional.of(ResponseCode.FAILURE), ResponseCode.getMessageByStatusCode(422), 
                     "Expected FAILURE for status code 422");
        assertEquals(Optional.empty(), ResponseCode.getMessageByStatusCode(999), 
                     "Expected empty Optional for unknown status code 999");
    }
}

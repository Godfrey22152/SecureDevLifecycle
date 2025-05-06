package com.shashi.beans;

import com.shashi.constant.ResponseCode;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Exception Handling")
@Feature("Train Exception Functionality")
@Story("Ensure TrainException properly handles different error scenarios")
class TrainExceptionTest {

    @Test
    @DisplayName("Test Construction with ResponseCode")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that TrainException is properly constructed with ResponseCode")
    void testResponseCodeConstructor() {
        // Using a sample ResponseCode (assuming SUCCESS exists with code 200)
        ResponseCode responseCode = ResponseCode.SUCCESS;
        TrainException exception = new TrainException(responseCode);

        assertEquals(responseCode.name(), exception.getErrorCode(), "Error code should match ResponseCode name");
        assertEquals(responseCode.getMessage(), exception.getErrorMessage(), "Error message should match ResponseCode message");
        assertEquals(responseCode.getCode(), exception.getStatusCode(), "Status code should match ResponseCode code");
        assertEquals(responseCode.getMessage(), exception.getMessage(), "Exception message should match ResponseCode message");
    }

    @Test
    @DisplayName("Test Construction with Error Message")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that TrainException is properly constructed with error message")
    void testErrorMessageConstructor() {
        String errorMessage = "Custom error message";
        TrainException exception = new TrainException(errorMessage);

        assertEquals("BAD_REQUEST", exception.getErrorCode(), "Default error code should be BAD_REQUEST");
        assertEquals(400, exception.getStatusCode(), "Default status code should be 400");
        assertEquals(errorMessage, exception.getErrorMessage(), "Error message should match input");
        assertEquals(errorMessage, exception.getMessage(), "Exception message should match input");
    }

    @Test
    @DisplayName("Test Full Parameter Construction")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that TrainException is properly constructed with all parameters")
    void testFullConstructor() {
        int statusCode = 404;
        String errorCode = "NOT_FOUND";
        String errorMessage = "Resource not found";
        
        TrainException exception = new TrainException(statusCode, errorCode, errorMessage);

        assertEquals(statusCode, exception.getStatusCode(), "Status code should match input");
        assertEquals(errorCode, exception.getErrorCode(), "Error code should match input");
        assertEquals(errorMessage, exception.getErrorMessage(), "Error message should match input");
        assertEquals(errorMessage, exception.getMessage(), "Exception message should match input");
    }

    @Test
    @DisplayName("Test Getter and Setter Methods")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that all getter and setter methods work correctly")
    void testGettersAndSetters() {
        TrainException exception = new TrainException("Initial message");

        // Test error code
        String expectedErrorCode = "VALIDATION_ERROR";
        exception.setErrorCode(expectedErrorCode);
        assertEquals(expectedErrorCode, exception.getErrorCode(), "Error code getter/setter mismatch");

        // Test error message - only check custom field
        String expectedErrorMessage = "Updated error message";
        exception.setErrorMessage(expectedErrorMessage);
        assertEquals(expectedErrorMessage, exception.getErrorMessage(), "Error message getter/setter mismatch");

        // Test status code
        int expectedStatusCode = 422;
        exception.setStatusCode(expectedStatusCode);
        assertEquals(expectedStatusCode, exception.getStatusCode(), "Status code getter/setter mismatch");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = TrainException.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }

    @Test
    @DisplayName("Test Exception Chaining")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that TrainException properly chains with the cause exception")
    void testExceptionChaining() {
        Throwable cause = new RuntimeException("Root cause");
        String errorMessage = "Wrapper message";
        
        TrainException exception = new TrainException(errorMessage);
        exception.initCause(cause);

        assertEquals(cause, exception.getCause(), "Should properly chain the cause exception");
        assertEquals(errorMessage, exception.getMessage(), "Should maintain its own message");
    }

    @Test
    @DisplayName("Test Null ResponseCode Handling")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior when null ResponseCode is provided")
    void testNullResponseCode() {
        // This test assumes the constructor will throw NullPointerException when ResponseCode is null
        assertThrows(NullPointerException.class, () -> {
            new TrainException((ResponseCode) null);
        }, "Should throw NullPointerException for null ResponseCode");
    }

    @Test
    @DisplayName("Test Null Error Message Handling")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior when null error message is provided")
    void testNullErrorMessage() {
        TrainException exception = new TrainException((String) null);
        
        assertNull(exception.getErrorMessage(), "Error message should be null");
        assertNull(exception.getMessage(), "Exception message should be null");
        assertEquals("BAD_REQUEST", exception.getErrorCode(), "Error code should default to BAD_REQUEST");
        assertEquals(400, exception.getStatusCode(), "Status code should default to 400");
    }
}

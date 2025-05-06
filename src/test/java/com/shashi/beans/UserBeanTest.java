package com.shashi.beans;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;

@Epic("User Management")
@Feature("User Bean Functionality")
@Story("Ensure UserBean properly manages user data and serialization")
class UserBeanTest {

    @Test
    @DisplayName("Test UserBean Serialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that UserBean can be serialized and deserialized correctly")
    void testSerialization() throws IOException, ClassNotFoundException {
        // Create original object
        UserBean original = new UserBean();
        original.setFName("John");
        original.setLName("Doe");
        original.setPWord("secure123");
        original.setAddr("123 Main St");
        original.setMailId("john.doe@example.com");
        original.setPhNo(1234567890L);

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        UserBean deserialized = (UserBean) ois.readObject();

        // Verify all fields
        assertEquals(original.getFName(), deserialized.getFName(), "First name should match after serialization");
        assertEquals(original.getLName(), deserialized.getLName(), "Last name should match after serialization");
        assertEquals(original.getPWord(), deserialized.getPWord(), "Password should match after serialization");
        assertEquals(original.getAddr(), deserialized.getAddr(), "Address should match after serialization");
        assertEquals(original.getMailId(), deserialized.getMailId(), "Email should match after serialization");
        assertEquals(original.getPhNo(), deserialized.getPhNo(), "Phone number should match after serialization");
    }

    @Test
    @DisplayName("Test Getters and Setters for All Fields")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that all getters and setters work correctly for UserBean")
    void testGettersAndSetters() {
        UserBean user = new UserBean();

        // Test firstName
        String expectedFirstName = "Alice";
        user.setFName(expectedFirstName);
        assertEquals(expectedFirstName, user.getFName(), "First name getter/setter mismatch");

        // Test lastName
        String expectedLastName = "Smith";
        user.setLName(expectedLastName);
        assertEquals(expectedLastName, user.getLName(), "Last name getter/setter mismatch");

        // Test password
        String expectedPassword = "p@ssw0rd";
        user.setPWord(expectedPassword);
        assertEquals(expectedPassword, user.getPWord(), "Password getter/setter mismatch");

        // Test address
        String expectedAddress = "456 Oak Ave";
        user.setAddr(expectedAddress);
        assertEquals(expectedAddress, user.getAddr(), "Address getter/setter mismatch");

        // Test email
        String expectedEmail = "alice.smith@example.com";
        user.setMailId(expectedEmail);
        assertEquals(expectedEmail, user.getMailId(), "Email getter/setter mismatch");

        // Test phone number
        long expectedPhone = 9876543210L;
        user.setPhNo(expectedPhone);
        assertEquals(expectedPhone, user.getPhNo(), "Phone number getter/setter mismatch");
    }

    @Test
    @DisplayName("Test Field Initialization with Null Values")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that UserBean handles null values correctly")
    void testNullValues() {
        UserBean user = new UserBean();

        // Set all String fields to null
        user.setFName(null);
        user.setLName(null);
        user.setPWord(null);
        user.setAddr(null);
        user.setMailId(null);

        // Verify null values are handled
        assertNull(user.getFName(), "First name should handle null");
        assertNull(user.getLName(), "Last name should handle null");
        assertNull(user.getPWord(), "Password should handle null");
        assertNull(user.getAddr(), "Address should handle null");
        assertNull(user.getMailId(), "Email should handle null");
        
        // Phone number is primitive, so it defaults to 0
        assertEquals(0, user.getPhNo(), "Phone number should default to 0 when not set");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        Field field = UserBean.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }

    @Test
    @DisplayName("Test Phone Number Edge Cases")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior with edge case values for phone number")
    void testPhoneNumberEdgeCases() {
        UserBean user = new UserBean();

        // Test minimum value
        user.setPhNo(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, user.getPhNo(), "Should handle minimum phone number");

        // Test maximum value
        user.setPhNo(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, user.getPhNo(), "Should handle maximum phone number");

        // Test zero
        user.setPhNo(0);
        assertEquals(0, user.getPhNo(), "Should handle zero phone number");

        // Test negative value
        user.setPhNo(-1234567890L);
        assertEquals(-1234567890L, user.getPhNo(), "Should handle negative phone number");
    }

    @Test
    @DisplayName("Test String Field Edge Cases")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior with edge case values for string fields")
    void testStringFieldEdgeCases() {
        UserBean user = new UserBean();

        // Test empty string
        user.setFName("");
        assertEquals("", user.getFName(), "Should handle empty first name");

        // Test long string
        String longName = "A".repeat(1000);
        user.setLName(longName);
        assertEquals(longName, user.getLName(), "Should handle long last name");

        // Test special characters
        String specialChars = "User#123!@$%^&*()";
        user.setPWord(specialChars);
        assertEquals(specialChars, user.getPWord(), "Should handle special characters in password");
    }
}

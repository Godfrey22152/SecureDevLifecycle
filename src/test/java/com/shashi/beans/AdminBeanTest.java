package com.shashi.beans;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field; // Add this import

@Epic("Admin Management")
@Feature("Admin Bean Functionality")
@Story("Ensure AdminBean properly manages admin data and serialization")
class AdminBeanTest {
    
    @Test
    @DisplayName("Test AdminBean Serialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that AdminBean can be serialized and deserialized correctly")
    void testSerialization() throws IOException, ClassNotFoundException {
        // Create original object
        AdminBean original = new AdminBean();
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
        AdminBean deserialized = (AdminBean) ois.readObject();

        // Verify
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
    @Description("Verify that all getters and setters work correctly for AdminBean")
    void testGettersAndSetters() {
        AdminBean admin = new AdminBean();

        // Test firstName
        String expectedFirstName = "Alice";
        admin.setFName(expectedFirstName);
        assertEquals(expectedFirstName, admin.getFName(), "First name getter/setter mismatch");

        // Test lastName
        String expectedLastName = "Smith";
        admin.setLName(expectedLastName);
        assertEquals(expectedLastName, admin.getLName(), "Last name getter/setter mismatch");

        // Test password
        String expectedPassword = "p@ssw0rd";
        admin.setPWord(expectedPassword);
        assertEquals(expectedPassword, admin.getPWord(), "Password getter/setter mismatch");

        // Test address
        String expectedAddress = "456 Oak Ave";
        admin.setAddr(expectedAddress);
        assertEquals(expectedAddress, admin.getAddr(), "Address getter/setter mismatch");

        // Test email
        String expectedEmail = "alice.smith@example.com";
        admin.setMailId(expectedEmail);
        assertEquals(expectedEmail, admin.getMailId(), "Email getter/setter mismatch");

        // Test phone number
        long expectedPhone = 9876543210L;
        admin.setPhNo(expectedPhone);
        assertEquals(expectedPhone, admin.getPhNo(), "Phone number getter/setter mismatch");
    }

    @Test
    @DisplayName("Test Field Initialization with Null Values")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that AdminBean handles null values correctly")
    void testNullValues() {
        AdminBean admin = new AdminBean();

        // Set all fields to null (where applicable)
        admin.setFName(null);
        admin.setLName(null);
        admin.setPWord(null);
        admin.setAddr(null);
        admin.setMailId(null);

        // Verify null values are handled
        assertNull(admin.getFName(), "First name should handle null");
        assertNull(admin.getLName(), "Last name should handle null");
        assertNull(admin.getPWord(), "Password should handle null");
        assertNull(admin.getAddr(), "Address should handle null");
        assertNull(admin.getMailId(), "Email should handle null");
        
        // Phone number is primitive, so it can't be null - default to 0
        assertEquals(0, admin.getPhNo(), "Phone number should default to 0 when not set");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        Field field = AdminBean.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }
}

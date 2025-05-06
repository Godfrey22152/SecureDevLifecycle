package com.shashi.beans;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;

@Epic("Booking History Management")
@Feature("History Bean Functionality")
@Story("Ensure HistoryBean properly manages booking history information and extends BookingDetails correctly")
class HistoryBeanTest {

    @Test
    @DisplayName("Test HistoryBean Serialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that HistoryBean can be serialized and deserialized correctly with all fields")
    void testSerialization() throws IOException, ClassNotFoundException {
        // Create original object with all fields (inherited + new)
        HistoryBean original = new HistoryBean();
        original.setMailId("history@example.com");
        original.setTr_no("TR78901");
        original.setDate("2023-10-10");
        original.setFrom_stn("East Station");
        original.setTo_stn("West Station");
        original.setSeats(4);
        original.setAmount(320.25);
        original.setTransId("TXN123456789");

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        HistoryBean deserialized = (HistoryBean) ois.readObject();

        // Verify all fields (inherited + new)
        assertEquals(original.getMailId(), deserialized.getMailId(), "Email should match after serialization");
        assertEquals(original.getTr_no(), deserialized.getTr_no(), "Train number should match after serialization");
        assertEquals(original.getDate(), deserialized.getDate(), "Date should match after serialization");
        assertEquals(original.getFrom_stn(), deserialized.getFrom_stn(), "From station should match after serialization");
        assertEquals(original.getTo_stn(), deserialized.getTo_stn(), "To station should match after serialization");
        assertEquals(original.getSeats(), deserialized.getSeats(), "Seats should match after serialization");
        assertEquals(original.getAmount(), deserialized.getAmount(), "Amount should match after serialization");
        assertEquals(original.getTransId(), deserialized.getTransId(), "Transaction ID should match after serialization");
    }

    @Test
    @DisplayName("Test Transaction ID Getter and Setter")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that transId getter and setter work correctly")
    void testTransIdGetterSetter() {
        HistoryBean history = new HistoryBean();
        
        String expectedTransId = "TXN987654321";
        history.setTransId(expectedTransId);
        
        assertEquals(expectedTransId, history.getTransId(), "Transaction ID getter/setter mismatch");
    }

    @Test
    @DisplayName("Test Null Transaction ID Handling")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that transId handles null values correctly")
    void testNullTransId() {
        HistoryBean history = new HistoryBean();
        
        history.setTransId(null);
        assertNull(history.getTransId(), "Transaction ID should handle null value");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        Field field = HistoryBean.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }

    @Test
    @DisplayName("Test Inheritance from BookingDetails")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that HistoryBean properly inherits from BookingDetails")
    void testInheritance() {
        HistoryBean history = new HistoryBean();
        
        // Test inherited fields
        String expectedMail = "inheritance@test.com";
        history.setMailId(expectedMail);
        assertEquals(expectedMail, history.getMailId(), "Inherited mailId field should work");
        
        // Test that it's actually a BookingDetails subclass
        assertTrue(history instanceof BookingDetails, "HistoryBean should be instance of BookingDetails");
    }

    @Test
    @DisplayName("Test Combined Functionality")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that both inherited and new fields work together correctly")
    void testCombinedFunctionality() {
        HistoryBean history = new HistoryBean();
        
        // Set inherited fields
        history.setMailId("combined@test.com");
        history.setTr_no("TR11111");
        history.setDate("2023-09-01");
        history.setFrom_stn("Start Station");
        history.setTo_stn("End Station");
        history.setSeats(1);
        history.setAmount(99.99);
        
        // Set new field
        history.setTransId("TXN111222333");
        
        // Verify all fields together
        assertEquals("combined@test.com", history.getMailId(), "Inherited mail should be set");
        assertEquals("TR11111", history.getTr_no(), "Inherited train number should be set");
        assertEquals("TXN111222333", history.getTransId(), "Transaction ID should be set");
    }
}

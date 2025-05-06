package com.shashi.beans;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;

@Epic("Train Management")
@Feature("Train Bean Functionality")
@Story("Ensure TrainBean properly manages train information and serialization")
class TrainBeanTest {

    @Test
    @DisplayName("Test TrainBean Serialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that TrainBean can be serialized and deserialized correctly")
    void testSerialization() throws IOException, ClassNotFoundException {
        // Create original object
        TrainBean original = new TrainBean();
        original.setTr_no(12345L);
        original.setTr_name("Express One");
        original.setFrom_stn("Central Station");
        original.setTo_stn("North Station");
        original.setSeats(150);
        original.setFare(89.99);

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        TrainBean deserialized = (TrainBean) ois.readObject();

        // Verify all fields
        assertEquals(original.getTr_no(), deserialized.getTr_no(), "Train number should match after serialization");
        assertEquals(original.getTr_name(), deserialized.getTr_name(), "Train name should match after serialization");
        assertEquals(original.getFrom_stn(), deserialized.getFrom_stn(), "From station should match after serialization");
        assertEquals(original.getTo_stn(), deserialized.getTo_stn(), "To station should match after serialization");
        assertEquals(original.getSeats(), deserialized.getSeats(), "Seats should match after serialization");
        assertEquals(original.getFare(), deserialized.getFare(), "Fare should match after serialization");
    }

    @Test
    @DisplayName("Test Getters and Setters for All Fields")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that all getters and setters work correctly for TrainBean")
    void testGettersAndSetters() {
        TrainBean train = new TrainBean();

        // Test train number
        Long expectedTrNo = 98765L;
        train.setTr_no(expectedTrNo);
        assertEquals(expectedTrNo, train.getTr_no(), "Train number getter/setter mismatch");

        // Test train name
        String expectedTrName = "Super Fast";
        train.setTr_name(expectedTrName);
        assertEquals(expectedTrName, train.getTr_name(), "Train name getter/setter mismatch");

        // Test from station
        String expectedFromStn = "East Terminal";
        train.setFrom_stn(expectedFromStn);
        assertEquals(expectedFromStn, train.getFrom_stn(), "From station getter/setter mismatch");

        // Test to station
        String expectedToStn = "West Terminal";
        train.setTo_stn(expectedToStn);
        assertEquals(expectedToStn, train.getTo_stn(), "To station getter/setter mismatch");

        // Test seats
        Integer expectedSeats = 200;
        train.setSeats(expectedSeats);
        assertEquals(expectedSeats, train.getSeats(), "Seats getter/setter mismatch");

        // Test fare
        Double expectedFare = 125.50;
        train.setFare(expectedFare);
        assertEquals(expectedFare, train.getFare(), "Fare getter/setter mismatch");
    }

    @Test
    @DisplayName("Test Field Initialization with Null Values")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that TrainBean handles null values correctly")
    void testNullValues() {
        TrainBean train = new TrainBean();

        // Set all nullable fields to null
        train.setTr_no(null);
        train.setTr_name(null);
        train.setFrom_stn(null);
        train.setTo_stn(null);
        train.setSeats(null);
        train.setFare(null);

        // Verify null values are handled
        assertNull(train.getTr_no(), "Train number should handle null");
        assertNull(train.getTr_name(), "Train name should handle null");
        assertNull(train.getFrom_stn(), "From station should handle null");
        assertNull(train.getTo_stn(), "To station should handle null");
        assertNull(train.getSeats(), "Seats should handle null");
        assertNull(train.getFare(), "Fare should handle null");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        Field field = TrainBean.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }

    @Test
    @DisplayName("Test Numeric Field Edge Cases")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior with edge case values for numeric fields")
    void testNumericEdgeCases() {
        TrainBean train = new TrainBean();

        // Test train number boundaries
        train.setTr_no(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, train.getTr_no(), "Should handle minimum train number");

        train.setTr_no(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, train.getTr_no(), "Should handle maximum train number");

        // Test seats boundaries
        train.setSeats(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, train.getSeats(), "Should handle minimum seats");

        train.setSeats(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, train.getSeats(), "Should handle maximum seats");

        // Test fare boundaries
        train.setFare(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, train.getFare(), "Should handle minimum fare");

        train.setFare(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, train.getFare(), "Should handle maximum fare");

        train.setFare(-100.0);
        assertEquals(-100.0, train.getFare(), "Should handle negative fare");
    }

    @Test
    @DisplayName("Test String Field Edge Cases")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior with edge case values for string fields")
    void testStringEdgeCases() {
        TrainBean train = new TrainBean();

        // Test empty string
        train.setTr_name("");
        assertEquals("", train.getTr_name(), "Should handle empty train name");

        // Test long string
        String longName = "A".repeat(1000);
        train.setTr_name(longName);
        assertEquals(longName, train.getTr_name(), "Should handle long train name");

        // Test special characters
        String specialChars = "Train#123!@$%^&*()";
        train.setTr_name(specialChars);
        assertEquals(specialChars, train.getTr_name(), "Should handle special characters in train name");
    }
}

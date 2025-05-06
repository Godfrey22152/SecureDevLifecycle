package com.shashi.beans;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;

@Epic("Booking Management")
@Feature("Booking Details Functionality")
@Story("Ensure BookingDetails properly manages booking information and serialization")
class BookingDetailsTest {

    @Test
    @DisplayName("Test BookingDetails Serialization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that BookingDetails can be serialized and deserialized correctly")
    void testSerialization() throws IOException, ClassNotFoundException {
        // Create original object
        BookingDetails original = new BookingDetails();
        original.setMailId("user@example.com");
        original.setTr_no("TR12345");
        original.setDate("2023-12-25");
        original.setFrom_stn("Station A");
        original.setTo_stn("Station B");
        original.setSeats(2);
        original.setAmount(150.50);

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        BookingDetails deserialized = (BookingDetails) ois.readObject();

        // Verify all fields
        assertEquals(original.getMailId(), deserialized.getMailId(), "Email should match after serialization");
        assertEquals(original.getTr_no(), deserialized.getTr_no(), "Train number should match after serialization");
        assertEquals(original.getDate(), deserialized.getDate(), "Date should match after serialization");
        assertEquals(original.getFrom_stn(), deserialized.getFrom_stn(), "From station should match after serialization");
        assertEquals(original.getTo_stn(), deserialized.getTo_stn(), "To station should match after serialization");
        assertEquals(original.getSeats(), deserialized.getSeats(), "Seats should match after serialization");
        assertEquals(original.getAmount(), deserialized.getAmount(), "Amount should match after serialization");
    }

    @Test
    @DisplayName("Test Getters and Setters for All Fields")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that all getters and setters work correctly for BookingDetails")
    void testGettersAndSetters() {
        BookingDetails booking = new BookingDetails();

        // Test email
        String expectedEmail = "test.user@example.com";
        booking.setMailId(expectedEmail);
        assertEquals(expectedEmail, booking.getMailId(), "Email getter/setter mismatch");

        // Test train number
        String expectedTrainNo = "TR98765";
        booking.setTr_no(expectedTrainNo);
        assertEquals(expectedTrainNo, booking.getTr_no(), "Train number getter/setter mismatch");

        // Test date
        String expectedDate = "2023-11-15";
        booking.setDate(expectedDate);
        assertEquals(expectedDate, booking.getDate(), "Date getter/setter mismatch");

        // Test from station
        String expectedFromStation = "Central Station";
        booking.setFrom_stn(expectedFromStation);
        assertEquals(expectedFromStation, booking.getFrom_stn(), "From station getter/setter mismatch");

        // Test to station
        String expectedToStation = "North Station";
        booking.setTo_stn(expectedToStation);
        assertEquals(expectedToStation, booking.getTo_stn(), "To station getter/setter mismatch");

        // Test seats
        int expectedSeats = 3;
        booking.setSeats(expectedSeats);
        assertEquals(expectedSeats, booking.getSeats(), "Seats getter/setter mismatch");

        // Test amount
        Double expectedAmount = 225.75;
        booking.setAmount(expectedAmount);
        assertEquals(expectedAmount, booking.getAmount(), "Amount getter/setter mismatch");
    }

    @Test
    @DisplayName("Test Field Initialization with Null Values")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that BookingDetails handles null values correctly")
    void testNullValues() {
        BookingDetails booking = new BookingDetails();

        // Set all nullable fields to null
        booking.setMailId(null);
        booking.setTr_no(null);
        booking.setDate(null);
        booking.setFrom_stn(null);
        booking.setTo_stn(null);
        booking.setAmount(null);

        // Verify null values are handled
        assertNull(booking.getMailId(), "Email should handle null");
        assertNull(booking.getTr_no(), "Train number should handle null");
        assertNull(booking.getDate(), "Date should handle null");
        assertNull(booking.getFrom_stn(), "From station should handle null");
        assertNull(booking.getTo_stn(), "To station should handle null");
        assertNull(booking.getAmount(), "Amount should handle null");
        
        // seats is primitive, so it defaults to 0
        assertEquals(0, booking.getSeats(), "Seats should default to 0 when not set");
    }

    @Test
    @DisplayName("Verify serialVersionUID")
    @Severity(SeverityLevel.MINOR)
    @Description("Check that serialVersionUID is correctly defined")
    void testSerialVersionUID() throws NoSuchFieldException, IllegalAccessException {
        Field field = BookingDetails.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        
        assertEquals(1L, serialVersionUID, "serialVersionUID should be 1L");
    }

    @Test
    @DisplayName("Test Edge Cases for Numeric Fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify behavior with edge case values for seats and amount")
    void testNumericEdgeCases() {
        BookingDetails booking = new BookingDetails();

        // Test minimum seats
        booking.setSeats(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, booking.getSeats(), "Should handle minimum seats value");

        // Test maximum seats
        booking.setSeats(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, booking.getSeats(), "Should handle maximum seats value");

        // Test negative amount
        booking.setAmount(-100.0);
        assertEquals(-100.0, booking.getAmount(), "Should handle negative amount");

        // Test zero amount
        booking.setAmount(0.0);
        assertEquals(0.0, booking.getAmount(), "Should handle zero amount");

        // Test large amount
        booking.setAmount(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, booking.getAmount(), "Should handle maximum amount value");
    }
}

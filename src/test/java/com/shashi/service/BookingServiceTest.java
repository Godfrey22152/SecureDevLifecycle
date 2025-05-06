package com.shashi.service;

import com.shashi.beans.HistoryBean;
import com.shashi.beans.TrainException;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Booking Management")
@Feature("Booking Service Operations")
@Story("Customer Booking Lifecycle Management")
class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    @Step("Initialize mock booking service")
    void setUp() {
        bookingService = Mockito.mock(BookingService.class);
    }

    @Test
    @DisplayName("Retrieve Customer Bookings Successfully")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify successful retrieval of all bookings for a valid customer ID")
    void testGetAllBookingsByCustomerId_success() throws TrainException {
        // Arrange
        String customerEmailId = "test@example.com";
        HistoryBean booking1 = new HistoryBean();
        HistoryBean booking2 = new HistoryBean();
        List<HistoryBean> expectedBookings = Arrays.asList(booking1, booking2);

        when(bookingService.getAllBookingsByCustomerId(customerEmailId)).thenReturn(expectedBookings);

        // Act
        List<HistoryBean> actualBookings = bookingService.getAllBookingsByCustomerId(customerEmailId);

        // Assert
        assertAll(
            () -> assertThat(actualBookings).isNotNull(),
            () -> assertThat(actualBookings).hasSize(2)
        );
        verify(bookingService, times(1)).getAllBookingsByCustomerId(customerEmailId);
    }

    @Test
    @DisplayName("Handle Missing Customer Bookings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify proper exception handling when customer has no bookings")
    void testGetAllBookingsByCustomerId_throwsException() throws TrainException {
        // Arrange
        String customerEmailId = "error@example.com";
        when(bookingService.getAllBookingsByCustomerId(customerEmailId))
                .thenThrow(new TrainException("Customer not found"));

        // Act & Assert
        assertThrows(TrainException.class, () -> bookingService.getAllBookingsByCustomerId(customerEmailId));
        verify(bookingService, times(1)).getAllBookingsByCustomerId(customerEmailId);
    }

    @Test
    @DisplayName("Create Booking History Successfully")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify successful creation of booking history record")
    @Issue("BOOK-101")
    @TmsLink("TMS-501")
    void testCreateHistory_success() throws TrainException {
        // Arrange
        HistoryBean bookingDetails = mock(HistoryBean.class);

        // Configure mock behavior
        when(bookingDetails.getTransId()).thenReturn("TXN123");
        when(bookingService.createHistory(any(HistoryBean.class))).thenReturn(bookingDetails);

        // Act
        HistoryBean createdBooking = bookingService.createHistory(new HistoryBean());

        // Assert
        assertAll(
            () -> assertThat(createdBooking).isNotNull(),
            () -> assertThat(createdBooking.getTransId()).isEqualTo("TXN123")
        );
        verify(bookingService, times(1)).createHistory(any(HistoryBean.class));
    }

    @Test
    @DisplayName("Handle Booking Creation Failure")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify proper exception handling when booking creation fails")
    void testCreateHistory_throwsException() throws TrainException {
        // Arrange
        HistoryBean bookingDetails = new HistoryBean();
        when(bookingService.createHistory(bookingDetails))
                .thenThrow(new TrainException("Unable to create booking"));

        // Act & Assert
        assertThrows(TrainException.class, () -> bookingService.createHistory(bookingDetails));
        verify(bookingService, times(1)).createHistory(bookingDetails);
    }
}

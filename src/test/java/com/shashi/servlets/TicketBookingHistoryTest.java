package com.shashi.servlets;

import com.shashi.beans.HistoryBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.service.BookingService;
import com.shashi.service.impl.BookingServiceImpl;
import com.shashi.utility.TrainUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Ticket Booking Feature")
@Feature("View Ticket Booking History")
class TicketBookingHistoryTest {

    private TicketBookingHistory servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private BookingService bookingService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock service construction
        try (MockedConstruction<BookingServiceImpl> mocked = mockConstruction(BookingServiceImpl.class)) {
            servlet = new TicketBookingHistory();
            bookingService = mocked.constructed().get(0);
        }

        // Setup standard mocks
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Test GET request with bookings")
    @Story("User Ticket History - Display Bookings")
    @Description("This test verifies that if the user has bookings, the history is displayed correctly.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_WithBookings() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Create test data
            List<HistoryBean> bookings = Arrays.asList(
                createHistory("TX123", 123L, "StationA", "StationB", "2023-10-01", 2, 100.0),
                createHistory("TX456", 456L, "StationC", "StationD", "2023-10-02", 1, 50.0)
            );

            // Configure mocks
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserEmail(request))
                   .thenReturn("user@test.com");
            when(bookingService.getAllBookingsByCustomerId(anyString())).thenReturn(bookings);

            // Execute
            servlet.doGet(request, response);

            // Verify
            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("UserViewTrains.html");
            
            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Booked Ticket History")),
                () -> assertTrue(output.contains("TX123")),
                () -> assertTrue(output.contains("StationA")),
                () -> assertTrue(output.contains("&#8358;100.0")),
                () -> assertTrue(output.contains("2023-10-01"))
            );
        }
    }

    @Test
    @DisplayName("Test GET request with no bookings")
    @Story("User Ticket History - No Bookings")
    @Description("This test verifies that when the user has no bookings, the appropriate message is displayed.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_NoBookings() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Configure mocks
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserEmail(request))
                   .thenReturn("user@test.com");
            when(bookingService.getAllBookingsByCustomerId(anyString())).thenReturn(Collections.emptyList());

            // Execute
            servlet.doGet(request, response);

            // Verify
            assertTrue(stringWriter.toString().contains("No any ticket booked"));
        }
    }

    @Test
    @DisplayName("Test GET request with authorization failure")
    @Story("User Ticket History - Authorization Failure")
    @Description("This test verifies that when user authorization fails, the correct exception is thrown.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @DisplayName("Test GET request with service exception")
    @Story("User Ticket History - Service Exception")
    @Description("This test verifies that when a service exception occurs, the correct exception is thrown.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_ServiceException() throws TrainException {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserEmail(request))
                   .thenReturn("user@test.com");
            when(bookingService.getAllBookingsByCustomerId(anyString()))
                .thenThrow(new RuntimeException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
        }
    }

    private HistoryBean createHistory(String transId, long trainNo, String from, String to, 
                                    String date, int seats, double amount) {
        HistoryBean history = new HistoryBean();
        history.setTransId(transId);
        history.setTr_no(String.valueOf(trainNo));  // Convert long to String
        history.setFrom_stn(from);
        history.setTo_stn(to);
        history.setDate(date);
        history.setSeats(seats);
        history.setAmount(amount);
        return history;
    }

}

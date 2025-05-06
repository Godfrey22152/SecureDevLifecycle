package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Train Reservation System")
@Feature("Book Train Ticket by Reference")
class BookTrainByRefTest {

    private BookTrainByRef servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BookTrainByRef();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Story("Customer ticket booking")
    @DisplayName("Successful Booking by Reference")
    @Description("Validates successful GET request for booking train tickets using reference with all correct parameters")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainNo")).thenReturn("12345");
            when(request.getParameter("fromStn")).thenReturn("StationA");
            when(request.getParameter("toStn")).thenReturn("StationB");

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserEmail(request))
                   .thenReturn("user@test.com");

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Your Ticket Booking Information")),
                () -> assertTrue(output.contains("user@test.com")),
                () -> assertTrue(output.contains("12345")),
                () -> assertTrue(output.contains("StationA")),
                () -> assertTrue(output.contains("StationB")),
                () -> assertTrue(output.contains(LocalDate.now().toString())),
                () -> assertTrue(output.contains("payment")),
                () -> assertTrue(output.contains("Sleeper(SL)")),
                () -> assertTrue(output.contains("Second Sitting(2S)"))
            );
        }
    }

    @Test
    @Story("Customer ticket booking")
    @DisplayName("Unauthorized Access Attempt")
    @Description("Throws RuntimeException when user fails authorization for booking via reference")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @Story("Customer ticket booking")
    @DisplayName("Invalid Train Number Parameter")
    @Description("Throws NumberFormatException when 'trainNo' is not a valid number")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_InvalidTrainNumber() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainNo")).thenReturn("invalid");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            assertThrows(NumberFormatException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @Story("Customer ticket booking")
    @DisplayName("Missing Parameters")
    @Description("Throws NumberFormatException when required query parameters are missing")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_MissingParameters() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                .thenAnswer(inv -> null);

            assertThrows(NumberFormatException.class, () -> servlet.doGet(request, response));
        }
    }
}

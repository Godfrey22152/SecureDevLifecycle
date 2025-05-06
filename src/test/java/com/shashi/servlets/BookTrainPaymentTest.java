package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Train Booking System")
@Feature("Booking Payment Processing")
@Story("Customer proceeds to payment after selecting train and seats")
class BookTrainPaymentTest {

    private BookTrainPayment servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext context;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new BookTrainPayment();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        context = mock(ServletContext.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getServletContext()).thenReturn(context);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Successful booking payment forwarding")
    @Description("Checks that user input is processed correctly and user is forwarded for payment when authorization is successful")
    @Severity(SeverityLevel.CRITICAL)
    void doPost_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Setup valid parameters
            when(request.getParameter("seats")).thenReturn("2");
            when(request.getParameter("trainnumber")).thenReturn("123");
            when(request.getParameter("journeydate")).thenReturn("2023-10-10");
            when(request.getParameter("class")).thenReturn("AC");

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            // Execute
            servlet.doPost(request, response);

            // Verify
            verify(response).setContentType("text/html");
            verify(context).setAttribute("seats", 2);
            verify(context).setAttribute("trainnumber", "123");
            verify(context).setAttribute("journeydate", "2023-10-10");
            verify(context).setAttribute("class", "AC");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("Handle unauthorized access to booking payment")
    @Description("Verifies that unauthorized users are blocked from booking and a RuntimeException is thrown")
    @Severity(SeverityLevel.NORMAL)
    void doPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
            
            verify(response).setContentType("text/html");
        }
    }

    @Test
    @DisplayName("Handle invalid number of seats input")
    @Description("Ensures that when a non-numeric seat value is provided, a NumberFormatException is thrown")
    @Severity(SeverityLevel.MINOR)
    void doPost_InvalidSeats() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("seats")).thenReturn("invalid");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            assertThrows(NumberFormatException.class, () -> servlet.doPost(request, response));
            
            verify(response).setContentType("text/html");
        }
    }
}

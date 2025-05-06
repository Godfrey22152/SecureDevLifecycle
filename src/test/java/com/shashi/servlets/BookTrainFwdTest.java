package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Epic("Train Booking System")
@Feature("Booking Flow Navigation")
@Story("Forward user to booking page after authorization")
class BookTrainFwdTest {

    private BookTrainFwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new BookTrainFwd();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        // Configure default successful path
        when(request.getRequestDispatcher("BookTrains.html"))
            .thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Forwarding to booking page upon successful authorization")
    @Description("Validates that a CUSTOMER user is successfully authorized and forwarded to the booking page")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulForward() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("Authorization failure handling")
    @Description("Checks that the servlet throws a RuntimeException when user is unauthorized")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @DisplayName("Verify response content type is set to HTML")
    @Description("Ensures the response content type is correctly set when authorization passes")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_VerifyContentType() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
        }
    }
}

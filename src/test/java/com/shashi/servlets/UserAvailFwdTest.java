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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("User Interface Flow")
@Feature("Availability Check Access")
class UserAvailFwdTest {

    private UserAvailFwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new UserAvailFwd();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Story("Successful Page Forwarding")
    @DisplayName("Forward Authorized User to Availability Page")
    @Description("Test verifies that authenticated CUSTOMER role users are properly forwarded to Availability.html")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulForward() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("Availability.html");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @Story("Authorization Failure Handling")
    @DisplayName("Block Unauthorized Access to Availability Check")
    @Description("Test ensures unauthorized users get proper error handling and cannot access availability page")
    @Severity(SeverityLevel.BLOCKER)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
            verify(response).setContentType("text/html");
        }
    }
}

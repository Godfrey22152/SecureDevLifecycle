package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Epic("Train Booking Flow")
@Feature("Train Between Stations Search")
class TrainBwStnFwdTest {

    private TrainBwStnFwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new TrainBwStnFwd();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Story("Successful Authorization Redirect")
    @DisplayName("Forward to Train Search Page on Valid Authorization")
    @Description("Verifies that users with valid CUSTOMER role are redirected to TrainBwStn.html")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulForward() throws ServletException, IOException {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(eq(request), eq(UserRole.CUSTOMER)))
                   .thenAnswer(invocation -> null);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("TrainBwStn.html");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @Story("Authorization Failure Handling")
    @DisplayName("Block Unauthorized Access with Exception")
    @Description("Ensures unauthorized users trigger a TrainException and prevent page forwarding")
    @Severity(SeverityLevel.BLOCKER)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(eq(request), eq(UserRole.CUSTOMER)))
                   .thenThrow(new TrainException("Authorization failed"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
            
            verify(response).setContentType("text/html");
            verify(request, never()).getRequestDispatcher(anyString());
            verifyNoInteractions(dispatcher);
        }
    }
}

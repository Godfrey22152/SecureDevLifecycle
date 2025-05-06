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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("User Authentication")
@Feature("Logout Functionality")
class UserLogoutServletTest {

    private UserLogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher requestDispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new UserLogoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher("UserLogin.html")).thenReturn(requestDispatcher);
    }

    @Test
    @Story("Logout Process")
    @DisplayName("Successful Logout Redirects to Login")
    @Description("Verify logged-in users are properly logged out and shown confirmation message")
    @Severity(SeverityLevel.CRITICAL)
    void doGet_WhenLoggedIn_ShouldLogoutAndShowSuccess() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.isLoggedIn(eq(request), eq(UserRole.CUSTOMER)))
                   .thenReturn(true);
            
            servlet.doGet(request, response);

            utilMock.verify(() -> TrainUtil.logout(eq(response)));
            
            String result = stringWriter.toString();
            assertAll(
                () -> verify(response).setContentType("text/html"),
                () -> verify(requestDispatcher).include(request, response),
                () -> assertTrue(result.contains("successfully logged out")),
                () -> assertFalse(result.contains("Already Logged Out"))
            );
        }
    }

    @Test
    @Story("Session Management")
    @DisplayName("Handle Already Logged Out State")
    @Description("Ensure proper handling when logout is attempted without active session")
    @Severity(SeverityLevel.NORMAL)
    void doGet_WhenNotLoggedIn_ShouldShowAlreadyLoggedOut() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.isLoggedIn(eq(request), eq(UserRole.CUSTOMER)))
                   .thenReturn(false);
            
            servlet.doGet(request, response);

            utilMock.verify(() -> TrainUtil.logout(any()), never());
            
            String result = stringWriter.toString();
            assertAll(
                () -> verify(response).setContentType("text/html"),
                () -> verify(requestDispatcher).include(request, response),
                () -> assertTrue(result.contains("Already Logged Out")),
                () -> assertFalse(result.contains("successfully logged out"))
            );
        }
    }

    @Test
    @Story("Page Rendering")
    @DisplayName("Always Include Login Page Content")
    @Description("Verify login page elements are always rendered regardless of session state")
    @Severity(SeverityLevel.MINOR)
    void doGet_ShouldAlwaysIncludeLoginPage() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.isLoggedIn(any(), any()))
                   .thenReturn(true)
                   .thenReturn(false);
            
            servlet.doGet(request, response);
            verify(requestDispatcher).include(request, response);
            
            servlet.doGet(request, response);
            verify(requestDispatcher, times(2)).include(request, response);
        }
    }
}

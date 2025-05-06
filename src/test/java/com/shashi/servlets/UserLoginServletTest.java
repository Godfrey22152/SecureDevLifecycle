package com.shashi.servlets;

import com.shashi.constant.ResponseCode;
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
@Feature("Login Functionality")
class UserLoginServletTest {

    private UserLoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher requestDispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new UserLoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getParameter("uname")).thenReturn("testuser");
        when(request.getParameter("pword")).thenReturn("testpass");
    }

    @Test
    @Story("Successful Authentication")
    @DisplayName("Successful User Login Redirects to Home")
    @Description("Verify valid credentials redirect to home page with welcome message and user-specific content")
    @Severity(SeverityLevel.CRITICAL)
    void doPost_SuccessfulLogin_ShouldDisplayWelcome() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.login(
                eq(request), 
                eq(response),
                eq(UserRole.CUSTOMER),
                eq("testuser"),
                eq("testpass")
            )).thenReturn(ResponseCode.SUCCESS.toString());
            
            when(request.getRequestDispatcher("UserHome.html")).thenReturn(requestDispatcher);

            servlet.doPost(request, response);

            String result = stringWriter.toString();
            assertAll(
                () -> verify(response).setContentType("text/html"),
                () -> verify(requestDispatcher).include(request, response),
                () -> assertTrue(result.contains("Welcome to our new NITRTC Website")),
                () -> assertTrue(result.contains("Hello testuser")),
                () -> assertTrue(result.contains("train schedule,fare Enquiry"))
            );
        }
    }

    @Test
    @Story("Authentication Failure")
    @DisplayName("Handle Invalid Credentials Gracefully")
    @Description("Ensure invalid credentials display appropriate error message and remain on login page")
    @Severity(SeverityLevel.NORMAL)
    void doPost_FailedLogin_ShouldDisplayErrorMessage() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            String errorMessage = "Invalid credentials";
            utilMock.when(() -> TrainUtil.login(any(), any(), any(), any(), any()))
                   .thenReturn(errorMessage);
            
            when(request.getRequestDispatcher("UserLogin.html")).thenReturn(requestDispatcher);

            servlet.doPost(request, response);

            String result = stringWriter.toString();
            assertAll(
                () -> verify(request).getRequestDispatcher("UserLogin.html"),
                () -> assertTrue(result.contains(errorMessage)),
                () -> assertFalse(result.contains("UserHome.html"))
            );
        }
    }

    @Test
    @Story("Input Validation")
    @DisplayName("Handle Missing Login Credentials")
    @Description("Verify proper handling of null credentials with error message display")
    @Severity(SeverityLevel.BLOCKER)
    void doPost_MissingCredentials_ShouldHandleNulls() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("uname")).thenReturn(null);
            when(request.getParameter("pword")).thenReturn(null);

            String errorMessage = "Missing credentials";
            utilMock.when(() -> TrainUtil.login(any(), any(), any(), isNull(), isNull()))
                .thenReturn(errorMessage);

            RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
            when(request.getRequestDispatcher(anyString())).thenReturn(mockDispatcher);

            servlet.doPost(request, response);

            verify(mockDispatcher).include(request, response);
            String result = stringWriter.toString();
            assertTrue(result.contains(errorMessage));
        }
    }

    @Test
    @Story("Error Handling")
    @DisplayName("Handle Server Errors During Login")
    @Description("Ensure proper exception propagation for backend service failures")
    @Severity(SeverityLevel.CRITICAL)
    void doPost_ServerError_ShouldHandleExceptions() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.login(any(), any(), any(), any(), any()))
                   .thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }
}

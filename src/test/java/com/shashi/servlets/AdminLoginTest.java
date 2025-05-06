package com.shashi.servlets;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import com.shashi.beans.TrainException;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;

@Epic("Servlet Authentication Tests")
@Feature("Admin Login")
@DisplayName("Unit Tests for AdminLogin Servlet")
public class AdminLoginTest {

    private AdminLogin adminLogin;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        adminLogin = new AdminLogin();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter, true);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    @Story("Successful Admin Login")
    @DisplayName("Should authenticate and redirect on successful admin login")
    @Description("Tests admin login success scenario by mocking TrainUtil.login to return SUCCESS")
    @Severity(SeverityLevel.CRITICAL)
    public void testDoPost_Success() throws Exception {
        when(request.getParameter("uname")).thenReturn("admin");
        when(request.getParameter("pword")).thenReturn("password");
        when(request.getRequestDispatcher("AdminHome.html")).thenReturn(requestDispatcher);

        try (MockedStatic<TrainUtil> mocked = mockStatic(TrainUtil.class)) {
            mocked.when(() -> TrainUtil.login(request, response, UserRole.ADMIN, "admin", "password"))
                  .thenReturn("SUCCESS");

            adminLogin.doPost(request, response);
        }

        verify(response).setContentType("text/html");
        verify(requestDispatcher).include(request, response);

        String output = stringWriter.toString();
        assertTrue(output.contains("Hello, admin ! Welcome"));
        assertTrue(output.contains("Manage Train Information"));
    }

    @Test
    @Story("Failed Admin Login")
    @DisplayName("Should display error message on failed login attempt")
    @Description("Tests login failure by returning invalid credentials from TrainUtil.login")
    @Severity(SeverityLevel.NORMAL)
    public void testDoPost_Failure() throws Exception {
        when(request.getParameter("uname")).thenReturn("admin");
        when(request.getParameter("pword")).thenReturn("wrongpassword");
        when(request.getRequestDispatcher("AdminLogin.html")).thenReturn(requestDispatcher);

        try (MockedStatic<TrainUtil> mocked = mockStatic(TrainUtil.class)) {
            mocked.when(() -> TrainUtil.login(request, response, UserRole.ADMIN, "admin", "wrongpassword"))
                  .thenReturn("Invalid credentials");

            adminLogin.doPost(request, response);
        }

        verify(response).setContentType("text/html");
        verify(requestDispatcher).include(request, response);

        String output = stringWriter.toString();
        assertTrue(output.contains("Invalid credentials"));
    }

    @Test
    @Story("Exception Handling in Login")
    @DisplayName("Should throw TrainException on unexpected error")
    @Description("Simulates exception during login process and verifies TrainException is thrown")
    @Severity(SeverityLevel.BLOCKER)
    public void testDoPost_ExceptionThrown() throws Exception {
        when(request.getParameter("uname")).thenReturn("admin");
        when(request.getParameter("pword")).thenReturn("password");

        try (MockedStatic<TrainUtil> mocked = mockStatic(TrainUtil.class)) {
            mocked.when(() -> TrainUtil.login(request, response, UserRole.ADMIN, "admin", "password"))
                  .thenThrow(new RuntimeException("Database error"));

            TrainException thrown = assertThrows(TrainException.class, () -> {
                adminLogin.doPost(request, response);
            });

            assertTrue(thrown.getMessage().contains("Database error"));
        }
    }
}

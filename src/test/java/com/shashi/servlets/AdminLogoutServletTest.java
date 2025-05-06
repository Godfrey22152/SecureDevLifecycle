package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;

@Epic("Servlet Authentication Tests")
@Feature("Admin Logout")
@DisplayName("Unit Tests for AdminLogoutServlet")
class AdminLogoutServletTest {

    private AdminLogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminLogoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher("AdminLogin.html")).thenReturn(dispatcher);
    }

    @Test
    @Story("Successful Logout")
    @DisplayName("Should log out admin and display confirmation message")
    @Description("Simulates a successful logout scenario when the admin is logged in.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGetWhenAdminIsLoggedIn() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.isLoggedIn(request, UserRole.ADMIN)).thenReturn(true);
            utilMock.when(() -> TrainUtil.logout(response)).thenAnswer(invocation -> null);

            servlet.doGet(request, response);

            verify(dispatcher).include(request, response);
            printWriter.flush();
            String output = stringWriter.toString();
            assertTrue(output.contains("You have been successfully logged out"));
        }
    }

    @Test
    @Story("Accessing Logout Page Without Login")
    @DisplayName("Should notify user that they are already logged out")
    @Description("Handles the scenario where an unauthenticated admin accesses the logout servlet.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGetWhenAdminIsNotLoggedIn() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.isLoggedIn(request, UserRole.ADMIN)).thenReturn(false);

            servlet.doGet(request, response);

            verify(dispatcher).include(request, response);
            printWriter.flush();
            String output = stringWriter.toString();
            assertTrue(output.contains("You are Already Logged Out"));
        }
    }
}

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
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("User Management")
@Feature("Password Management")
@Story("Display change password form for customers")
class ChangeUserPasswordTest {

    private ChangeUserPassword servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    @DisplayName("Setup mocks for ChangeUserPassword servlet")
    void setUp() throws Exception {
        servlet = new ChangeUserPassword();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Successfully load change password form")
    @Description("Verifies that the change password form is rendered correctly when user is authorized")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserName(request))
                   .thenReturn("John Doe");

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Hello John Doe"), "Should contain user greeting"),
                () -> assertTrue(output.contains("View Profile</a>"), "Should contain profile links"),
                () -> assertTrue(output.contains("Change Password</a>"), "Should contain password change link"),
                () -> assertTrue(output.contains("Password Change</div>"), "Should contain title"),
                () -> assertTrue(output.contains("name='username'"), "Should have username field"),
                () -> assertTrue(output.contains("name='oldpassword'"), "Should have old password field"),
                () -> assertTrue(output.contains("name='newpassword'"), "Should have new password field"),
                () -> assertTrue(output.contains("action='changeuserpwd'"), "Should have correct form action")
            );
        }
    }

    @Test
    @DisplayName("Authorization failure when accessing change password")
    @Description("Throws an exception if the user is not authorized to access the change password form")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
            verify(response).setContentType("text/html");
        }
    }

    @Test
    @DisplayName("Verify structure of change password HTML form")
    @Description("Ensures that the expected HTML structure exists in the servlet output")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_VerifyStructure() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserName(request))
                .thenReturn("Test User");

            servlet.doGet(request, response);

            String output = stringWriter.toString().toLowerCase();

            assertAll(
                () -> assertTrue(output.contains("div class='main'") || output.contains("div class=\"main\""), "Should contain main div"),
                () -> assertTrue(output.contains("div class='tab'") || output.contains("div class=\"tab\""), "Should contain tab div"),
                () -> assertTrue(output.contains("<form"), "Should contain form element")
            );
        }
    }
}

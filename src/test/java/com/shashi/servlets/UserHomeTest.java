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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("User Interface")
@Feature("User Home Page")
class UserHomeTest {

    private UserHome servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher requestDispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new UserHome();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    @Story("Home Page Rendering")
    @DisplayName("Render Home Page for Authorized User")
    @Description("Verifies that authenticated users are shown the home page with proper content and personalized greeting")
    @Severity(SeverityLevel.CRITICAL)
    void doGet_ValidUser_ShouldRenderHomePage() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(eq(request), eq(UserRole.CUSTOMER)))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserName(eq(request)))
                   .thenReturn("Test User");

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(requestDispatcher).include(request, response);
            
            String result = stringWriter.toString();
            assertAll(
                () -> assertTrue(result.contains("Welcome to our new NITRTC Website")),
                () -> assertTrue(result.contains("User Home")),
                () -> assertTrue(result.contains("Hello Test User")),
                () -> assertTrue(result.contains("train details")),
                () -> assertTrue(result.contains("fare Enquiry"))
            );
        }
    }

    @Test
    @Story("Authorization Handling")
    @DisplayName("Prevent Access for Unauthorized Users")
    @Description("Ensures unauthorized users cannot access the home page and receive an exception")
    @Severity(SeverityLevel.BLOCKER)
    void doGet_UnauthorizedUser_ShouldThrowException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(eq(request), eq(UserRole.CUSTOMER)))
                   .thenThrow(new TrainException("Unauthorized access"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
            
            verify(request, never()).getRequestDispatcher(anyString());
            assertEquals("", stringWriter.toString());
        }
    }

    @Test
    @Story("User Session Handling")
    @DisplayName("Gracefully Handle Missing User Name")
    @Description("Validates proper handling of null user names in the session to maintain page functionality")
    @Severity(SeverityLevel.NORMAL)
    void doGet_MissingUserName_ShouldHandleNullGracefully() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(eq(request), eq(UserRole.CUSTOMER)))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserName(eq(request)))
                   .thenReturn(null);

            servlet.doGet(request, response);

            String result = stringWriter.toString();
            assertAll(
                () -> assertTrue(result.contains("Hello null")),
                () -> assertTrue(result.contains("Good to See You here"))
            );
        }
    }
}

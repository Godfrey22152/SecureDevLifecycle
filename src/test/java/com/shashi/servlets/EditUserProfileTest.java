package com.shashi.servlets;

import com.shashi.beans.UserBean;
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
@Feature("Profile Management")
@Story("Edit user profile for customers")
class EditUserProfileTest {

    private EditUserProfile servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private UserBean mockUser;

    @BeforeEach
    @DisplayName("Setup mocks for EditUserProfile servlet")
    void setUp() throws Exception {
        servlet = new EditUserProfile();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        // Set up mock user with correct values
        mockUser = mock(UserBean.class);
        when(mockUser.getMailId()).thenReturn("user@test.com");
        when(mockUser.getFName()).thenReturn("John");
        when(mockUser.getLName()).thenReturn("Doe");
        when(mockUser.getAddr()).thenReturn("123 Main St");
        when(mockUser.getPhNo()).thenReturn(555-1234L);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Successfully load edit user profile form")
    @Description("Verifies that the edit profile form is rendered correctly when user is authorized")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);
            utilMock.when(() -> TrainUtil.getCurrentUserName(request)).thenReturn("John Doe");

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Hello John Doe"), "User greeting"),
                () -> assertTrue(output.contains("Profile Update"), "Title"),
                () -> assertTrue(output.contains("value='user@test.com'"), "Username field"),
                () -> assertTrue(output.contains("value='John'"), "First name"),
                () -> assertTrue(output.contains("value='Doe'"), "Last name"),
                () -> assertTrue(output.contains("value='123 Main St'"), "Address"),
                () -> assertFalse(output.contains("value='555-1234L'"), "Phone number not present"),
                () -> assertTrue(output.contains("action='updateuserprofile'"), "Form action"),
                () -> assertTrue(output.contains("Update Profile"), "Submit button")
            );
        }
    }

    @Test
    @DisplayName("Authorization failure when accessing edit profile")
    @Description("Throws an exception if the user is not authorized to access the edit profile form")
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
    @DisplayName("Verify that username field is disabled in profile form")
    @Description("Ensures that the username field is disabled during the profile update")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_VerifyDisabledUsername() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);

            servlet.doGet(request, response);

            String output = stringWriter.toString();
            assertTrue(output.contains("disabled"), "Username should be disabled");
        }
    }

    @Test
    @DisplayName("Verify navigation links on the edit profile page")
    @Description("Checks that navigation links such as View Profile, Edit Profile, and Change Password exist")
    @Severity(SeverityLevel.TRIVIAL)
    void testDoGet_VerifyNavigationLinks() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);

            servlet.doGet(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("View Profile</a>"), "View Profile link"),
                () -> assertTrue(output.contains("Edit Profile</a>"), "Edit Profile link"),
                () -> assertTrue(output.contains("Change Password</a>"), "Change Password link")
            );
        }
    }
}

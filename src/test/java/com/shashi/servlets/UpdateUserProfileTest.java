package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.beans.UserBean;
import com.shashi.constant.UserRole;
import com.shashi.service.UserService;
import com.shashi.service.impl.UserServiceImpl;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("User Management")
@Feature("Profile Update")
class UpdateUserProfileTest {

    private UpdateUserProfile servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private UserService userService;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private UserBean mockUser;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<UserServiceImpl> mocked = mockConstruction(UserServiceImpl.class)) {
            servlet = new UpdateUserProfile();
            userService = mocked.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        mockUser = new UserBean();
        mockUser.setFName("John");
        mockUser.setMailId("user@test.com");

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher("UserHome.html")).thenReturn(dispatcher);
    }

    private void setupValidParams() {
        when(request.getParameter("firstname")).thenReturn("John");
        when(request.getParameter("lastname")).thenReturn("Doe");
        when(request.getParameter("address")).thenReturn("123 Main St");
        when(request.getParameter("phone")).thenReturn("5551234567");
        when(request.getParameter("mail")).thenReturn("john.doe@test.com");
    }

    @Test
    @Story("Successful Profile Update")
    @DisplayName("Update User Profile with Valid Data")
    @Description("Test verifies successful profile update with valid parameters and checks response content")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);
            when(userService.updateUser(any(UserBean.class))).thenReturn("SUCCESS");
            setupValidParams();

            servlet.doPost(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);
            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Successfully Updated")),
                () -> assertTrue(output.contains("John")),
                () -> assertTrue(output.contains("view Profile"))
            );
        }
    }

    @Test
    @Story("Profile Update Failure")
    @DisplayName("Handle Update Failure from Service")
    @Description("Test ensures proper error handling when service layer returns update failure")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_UpdateFailure() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);
            when(userService.updateUser(any(UserBean.class))).thenReturn("FAILURE");
            setupValidParams();

            servlet.doPost(request, response);

            String output = stringWriter.toString();
            assertTrue(output.contains("valid Information"));
        }
    }

    @Test
    @Story("Input Validation")
    @DisplayName("Reject Profile Update with Invalid Phone Number")
    @Description("Test validates proper exception handling for invalid phone number format")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_InvalidPhoneNumber() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
    
            when(request.getParameter("phone")).thenReturn("invalid");
    
            assertThrows(NumberFormatException.class, () -> servlet.doPost(request, response));
        }
    }
    
    @Test
    @Story("Authorization Handling")
    @DisplayName("Block Unauthorized Profile Updates")
    @Description("Test ensures unauthorized users cannot access profile update functionality")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @Story("Error Handling")
    @DisplayName("Handle Service Layer Exceptions")
    @Description("Test verifies proper exception propagation when service layer throws errors")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_ServiceException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(mockUser);
            setupValidParams();
            when(userService.updateUser(any(UserBean.class))).thenThrow(new RuntimeException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}

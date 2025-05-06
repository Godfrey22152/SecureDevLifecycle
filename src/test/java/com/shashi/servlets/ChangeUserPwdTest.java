package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.beans.UserBean;
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
@Feature("Change Password Feature")
@Story("As a customer, I want to change my password securely")
class ChangeUserPwdTest {

    private ChangeUserPwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private UserService userService;
    private UserBean currentUser;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<UserServiceImpl> userMock = mockConstruction(UserServiceImpl.class)) {
            servlet = new ChangeUserPwd();
            userService = userMock.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        currentUser = new UserBean();
        currentUser.setMailId("user@test.com");
        currentUser.setPWord("oldPass");
        currentUser.setFName("Test User");

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Change password successfully")
    @Description("Test that a user with valid credentials can change their password successfully.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(currentUser);
            when(request.getParameter("username")).thenReturn("user@test.com");
            when(request.getParameter("oldpassword")).thenReturn("oldPass");
            when(request.getParameter("newpassword")).thenReturn("newPass");
            when(userService.updateUser(any(UserBean.class))).thenReturn("SUCCESS");

            servlet.doPost(request, response);

            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("UserLogin.html");
            utilMock.verify(() -> TrainUtil.logout(response));

            assertTrue(stringWriter.toString().contains("Updated Successfully"));
        }
    }

    @Test
    @DisplayName("Change password with invalid username")
    @Description("Test that password change fails when the username does not match the session user.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_InvalidUsername() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(currentUser);
            when(request.getParameter("username")).thenReturn("wrong@test.com");

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("UserHome.html");
            assertTrue(stringWriter.toString().contains("Invalid UserName"));
        }
    }

    @Test
    @DisplayName("Change password with wrong old password")
    @Description("Test that password change fails when the old password is incorrect.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_WrongOldPassword() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(currentUser);
            when(request.getParameter("username")).thenReturn("user@test.com");
            when(request.getParameter("oldpassword")).thenReturn("wrongPass");

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("UserHome.html");
            assertTrue(stringWriter.toString().contains("Wrong Old PassWord!"));
        }
    }

    @Test
    @DisplayName("Change password update failure")
    @Description("Test that password change fails when the update service returns 'FAILURE'.")
    @Severity(SeverityLevel.MINOR)
    void testDoPost_UpdateFailure() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(currentUser);
            when(request.getParameter("username")).thenReturn("user@test.com");
            when(request.getParameter("oldpassword")).thenReturn("oldPass");
            when(userService.updateUser(any(UserBean.class))).thenReturn("FAILURE");

            servlet.doPost(request, response);

            assertTrue(stringWriter.toString().contains("Invalid Username and Old Password !"));
        }
    }

    @Test
    @DisplayName("Change password throws TrainException on service failure")
    @Description("Test that a TrainException is thrown when the update service fails internally.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_ServiceException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.getCurrentCustomer(request)).thenReturn(currentUser);
            when(request.getParameter("username")).thenReturn("user@test.com");
            when(request.getParameter("oldpassword")).thenReturn("oldPass");
            when(userService.updateUser(any(UserBean.class))).thenThrow(new RuntimeException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}

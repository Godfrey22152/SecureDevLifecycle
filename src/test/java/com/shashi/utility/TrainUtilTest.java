package com.shashi.utility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shashi.beans.TrainException;
import com.shashi.beans.UserBean;
import com.shashi.constant.ResponseCode;
import com.shashi.constant.UserRole;
import com.shashi.service.impl.UserServiceImpl;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedConstruction;

@Epic("Train Management Module")
@Feature("Train Utility Functions")
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainUtil Full Functional Test Suite")
public class TrainUtilTest {

    @Test
    @Story("Read cookie value for user session")
    @DisplayName("Read Cookie - Cookie Exists")
    @Description("Verifies that a cookie with the expected name returns a valid value.")
    @Severity(SeverityLevel.CRITICAL)
    void testReadCookie_whenCookieExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("sessionIdForADMIN", "abc123");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        Optional<String> result = TrainUtil.readCookie(request, "sessionIdForADMIN");
        assertTrue(result.isPresent());
        assertEquals("abc123", result.get());
    }

    @Test
    @Story("Read cookie value for user session")
    @DisplayName("Read Cookie - No Cookies Present")
    @Description("Verifies that when no cookies exist, an empty Optional is returned.")
    @Severity(SeverityLevel.NORMAL)
    void testReadCookie_whenNoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        Optional<String> result = TrainUtil.readCookie(request, "sessionIdForADMIN");
        assertFalse(result.isPresent());
    }

    @Test
    @Story("Login using correct credentials")
    @DisplayName("Login - Successful")
    @Description("Verifies successful login returns a success response.")
    @Severity(SeverityLevel.BLOCKER)
    void testLogin_successful() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);

        UserBean user = new UserBean();
        user.setFName("John");
        user.setMailId("john@example.com");

        when(request.getServletContext()).thenReturn(context);
        when(request.getSession()).thenReturn(session);

        try (MockedConstruction<UserServiceImpl> mocked =
                     mockConstruction(UserServiceImpl.class, (mock, contextMock) -> {
                         when(mock.loginUser("john", "pass")).thenReturn(user);
                     })) {

            String result = TrainUtil.login(request, response, UserRole.ADMIN, "john", "pass");
            assertEquals(ResponseCode.SUCCESS.toString(), result);
        }
    }

    @Test
    @Story("Login using incorrect credentials")
    @DisplayName("Login - Failure")
    @Description("Verifies failed login returns an unauthorized response with error details.")
    @Severity(SeverityLevel.CRITICAL)
    void testLogin_failure() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        try (MockedConstruction<UserServiceImpl> mocked =
                     mockConstruction(UserServiceImpl.class, (mock, contextMock) -> {
                         when(mock.loginUser("john", "wrong")).thenThrow(new TrainException("Invalid credentials"));
                     })) {

            String result = TrainUtil.login(request, response, UserRole.ADMIN, "john", "wrong");
            assertTrue(result.contains(ResponseCode.UNAUTHORIZED.toString()));
            assertTrue(result.contains("Invalid credentials"));
        }
    }

    @Test
    @Story("User login session check")
    @DisplayName("Is Logged In - True")
    @Description("Verifies isLoggedIn returns true when cookie is present.")
    @Severity(SeverityLevel.CRITICAL)
    void testIsLoggedIn_true() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("sessionIdForADMIN", "token123");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        assertTrue(TrainUtil.isLoggedIn(request, UserRole.ADMIN));
    }

    @Test
    @Story("User login session check")
    @DisplayName("Is Logged In - False")
    @Description("Verifies isLoggedIn returns false when no cookies are present.")
    @Severity(SeverityLevel.NORMAL)
    void testIsLoggedIn_false() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        assertFalse(TrainUtil.isLoggedIn(request, UserRole.ADMIN));
    }

    @Test
    @Story("Validate user session for authorization")
    @DisplayName("Validate Authorization - Logged In")
    @Description("Checks authorization does not throw exception for valid session cookie.")
    @Severity(SeverityLevel.CRITICAL)
    void testValidateUserAuthorization_loggedIn() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("sessionIdForCUSTOMER", "token123");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertDoesNotThrow(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER));
    }

    @Test
    @Story("Validate user session for authorization")
    @DisplayName("Validate Authorization - Session Expired")
    @Description("Checks authorization throws TrainException when session has expired.")
    @Severity(SeverityLevel.BLOCKER)
    void testValidateUserAuthorization_notLoggedIn() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        TrainException ex = assertThrows(TrainException.class,
                () -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER));
        assertEquals("Session Expired, Login Again to Continue", ex.getMessage());
    }

    @Test
    @Story("User logout functionality")
    @DisplayName("Logout")
    @Description("Verifies logout method adds cookies to expire sessions.")
    @Severity(SeverityLevel.CRITICAL)
    void testLogout() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertTrue(TrainUtil.logout(response));
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    @Story("Get currently logged in user's name")
    @DisplayName("Get Current User Name")
    @Description("Checks if the username is retrieved correctly from the session.")
    @Severity(SeverityLevel.MINOR)
    void testGetCurrentUserName() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uName")).thenReturn("Jane");

        assertEquals("Jane", TrainUtil.getCurrentUserName(request));
    }

    @Test
    @Story("Get currently logged in user's email")
    @DisplayName("Get Current User Email")
    @Description("Checks if the user email is retrieved correctly from the session.")
    @Severity(SeverityLevel.MINOR)
    void testGetCurrentUserEmail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("mailid")).thenReturn("jane@example.com");

        assertEquals("jane@example.com", TrainUtil.getCurrentUserEmail(request));
    }

    @Test
    @Story("Fetch current user object from context")
    @DisplayName("Get Current Customer - Exists")
    @Description("Checks if the current user object is fetched successfully from the servlet context.")
    @Severity(SeverityLevel.MINOR)
    void testGetCurrentCustomer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletContext context = mock(ServletContext.class);
        UserBean customer = new UserBean();
        when(request.getServletContext()).thenReturn(context);
        when(context.getAttribute(UserRole.CUSTOMER.toString())).thenReturn(customer);

        assertEquals(customer, TrainUtil.getCurrentCustomer(request));
    }

    @Test
    @Story("Fetch current user object from context")
    @DisplayName("Get Current Customer - Not Found")
    @Description("Verifies null is returned when no user object exists in the servlet context.")
    @Severity(SeverityLevel.NORMAL)
    void testGetCurrentCustomer_whenCustomerIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletContext context = mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(context);
        when(context.getAttribute(UserRole.CUSTOMER.toString())).thenReturn(null);

        assertNull(TrainUtil.getCurrentCustomer(request));
    }
}

package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Fare Enquiry Feature")
@Feature("User Authorization and Forwarding")
class FareEnqFwdTest {

    private FareEnqFwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new FareEnqFwd();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Test successful GET request with valid authorization")
    @Story("Fare Enquiry - Success Scenario")
    @Description("This test verifies that when the user is successfully authorized, the servlet forwards the request to the 'Fare.html' page.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Configure successful authorization
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            // Execute
            servlet.doGet(request, response);

            // Verify
            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("Fare.html");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("Test GET request with authorization failure")
    @Story("Fare Enquiry - Authorization Failure")
    @Description("This test verifies that the servlet throws a RuntimeException when user authorization fails.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Force authorization failure
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            // Verify exception and basic setup
            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
            verify(response).setContentType("text/html");
        }
    }

    @Test
    @DisplayName("Test GET request to verify forwarding to Fare.html")
    @Story("Fare Enquiry - Request Forwarding Verification")
    @Description("This test verifies that the request is correctly forwarded to the 'Fare.html' page after a successful authorization.")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_VerifyForwarding() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            servlet.doGet(request, response);

            // Verify exact forwarding
            verify(request).getRequestDispatcher("Fare.html");
            verify(dispatcher).forward(request, response);
        }
    }
}
